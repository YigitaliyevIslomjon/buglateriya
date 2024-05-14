package uz.org

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import java.time.LocalDate

interface RegionService {
    fun create(dto: CreateRegionDto): GetRegionDto
    fun update(id: Long, dto: UpdateRegionDto): GetRegionDto
    fun delete(id: Long)
    fun getById(id: Long): GetRegionDto
    fun getAll(pageable: Pageable): Page<GetRegionDto>
}

interface OrganizationService {
    fun create(dto: CreateOrganizationDto): GetOrganizationDto
    fun update(id: Long, dto: UpdateOrganizationDto): GetOrganizationDto
    fun delete(id: Long)
    fun getById(id: Long): GetOrganizationDto
    fun getAll(pageable: Pageable): Page<GetOrganizationDto>
}


interface EmployeeService {
    fun create(dto: CreateEmployeeDto): GetEmployeeDto
    fun update(id: Long, dto: UpdateEmployeeDto): GetEmployeeDto
    fun delete(id: Long)
    fun getById(id: Long): GetEmployeeDto
    fun getAll(pageable: Pageable): Page<GetEmployeeDto>
}

interface CalculationTableService {
    fun create(dto: CreateCalculationTableDto): GetCalculationTableDto
    fun update(id: Long, dto: UpdateCalculationTableDto): GetCalculationTableDto
    fun delete(id: Long)
    fun getById(id: Long): GetCalculationTableDto
    fun getAll(pageable: Pageable): Page<GetCalculationTableDto>

    fun getAllRate(pageable: Pageable, date: LocalDate, rate: Double): Page<GetAllRateDto>
    fun getAllDifferentRegion(date: LocalDate): List<GetDifferentRegionDto>
    fun getAllChildOrganization(
        date: LocalDate,
        organizationId: Long,
    ): List<GetChildOrganizationsDto>

    fun getAllEmployeeInfo(
        date: LocalDate,
    ): List<GetEmployeeInfoDto>


}

@Service
class RegionServiceImpl(
    private val regionRepository: RegionRepository,
) : RegionService {
    override fun create(dto: CreateRegionDto): GetRegionDto {
        val region = regionRepository.findByName(dto.name) ?: run {
            return GetRegionDto.toResponse(regionRepository.save(dto.toEntity()))
        }

        if (!region.deleted) {
            throw RegionExistException("region ${dto.name} already exist")
        } else {
            region.deleted = false
            return GetRegionDto.toResponse(regionRepository.save(region))
        }
    }

    override fun update(id: Long, dto: UpdateRegionDto): GetRegionDto {
        val region =
            regionRepository.findByIdNotDeleted(id) ?: throw RegionNotFoundException("region id $id is not found")

        dto.name?.let {
            val existRegion = regionRepository.findByName(dto.name)

            if (region.name != it && existRegion != null && !existRegion.deleted) {
                throw RegionExistException("region ${dto.name} already exist")
            } else if (existRegion != null && existRegion.id != region.id) {
                throw RegionNameInvalidException("region  ${dto.name}  is invalid, choose another one")
            }
            region.name = dto.name
        }
        return GetRegionDto.toResponse(regionRepository.save(region))
    }

    override fun delete(id: Long) {
        regionRepository.findByIdNotDeleted(id) ?: throw RegionNotFoundException("region id $id is not found")
        regionRepository.trash(id)
    }

    override fun getById(id: Long): GetRegionDto {
        val region =
            regionRepository.findByIdNotDeleted(id) ?: throw RegionNotFoundException("region id $id is not found")
        return GetRegionDto.toResponse(region)
    }

    override fun getAll(pageable: Pageable): Page<GetRegionDto> {
        return regionRepository.findAllNotDeleted(pageable).map(GetRegionDto.Companion::toResponse)
    }
}

@Service
class OrganizationServiceImpl(
    private val regionRepository: RegionRepository,
    private val organizationRepository: OrganizationRepository,
) : OrganizationService {
    override fun create(dto: CreateOrganizationDto): GetOrganizationDto {
        val region = regionRepository.findByIdNotDeleted(dto.regionId)
            ?: throw RegionNotFoundException("region id ${dto.regionId} is not found")

        var organization: Organization? = null
        dto.parentId?.let {
            organization = organizationRepository.findByIdNotDeleted(it)
                ?: throw OrganizationNotFoundException("Organization id $it is not found")
        }

        return GetOrganizationDto.toResponse(organizationRepository.save(dto.toEntity(region, organization)))
    }

    override fun update(id: Long, dto: UpdateOrganizationDto): GetOrganizationDto {

        val organization = organizationRepository.findByIdNotDeleted(id)
            ?: throw OrganizationNotFoundException("Organization id $id is not found")
        dto.regionId?.let {
            val region = regionRepository.findByIdNotDeleted(it)
                ?: throw RegionNotFoundException("region id $it is not found")
            organization.region = region
        }
        dto.name?.let {
            organization.name = it
        }
        dto.parentId?.let {
            val existOrganization = organizationRepository.findByIdNotDeleted(it)
                ?: throw OrganizationNotFoundException("Organization id $it is not found")
            organization.parent = existOrganization
        }
        return GetOrganizationDto.toResponse(organizationRepository.save(organization))
    }

    override fun delete(id: Long) {
        organizationRepository.findByIdNotDeleted(id)
            ?: throw OrganizationNotFoundException("Organization id $id is not found")
        organizationRepository.trash(id)
    }

    override fun getById(id: Long): GetOrganizationDto {
        val organization = organizationRepository.findByIdNotDeleted(id)
            ?: throw OrganizationNotFoundException("Organization id $id is not found")
        return GetOrganizationDto.toResponse(organization)
    }

    override fun getAll(pageable: Pageable): Page<GetOrganizationDto> =
        organizationRepository.findAllNotDeleted(pageable).map(GetOrganizationDto.Companion::toResponse)

}

@Service
class EmployeeServiceImpl(
    private val employeeRepository: EmployeeRepository,
    private val organizationRepository: OrganizationRepository,
) : EmployeeService {
    override fun create(dto: CreateEmployeeDto): GetEmployeeDto {
        val organization = organizationRepository.findByIdNotDeleted(dto.organizationId)
            ?: throw OrganizationNotFoundException("Organization id ${dto.organizationId} is not found")

        val employee = employeeRepository.findByPinflAndOrganization(dto.pinfl, organization)
        if (employee != null && !employee.deleted) {
            throw PinflOrganizationExistException("pinfl ${dto.pinfl} and organization ${dto.organizationId} is exist already")
        } else if (employee != null) {
            employee.deleted = false
            employee.firstName = dto.firstName
            employee.lastName = dto.lastName
            employee.hireDate = dto.hireDate
            return GetEmployeeDto.toResponse(employeeRepository.save(employee))
        }
        return GetEmployeeDto.toResponse(employeeRepository.save(dto.toEntity(organization)))
    }

    override fun update(id: Long, dto: UpdateEmployeeDto): GetEmployeeDto {

        val employee = employeeRepository.findByIdNotDeleted(id)
            ?: throw EmployeeNotFoundException("employee id $id is not found")

        val organization: Organization

        if (dto.organizationId != null && dto.pinfl != null) {
            if (dto.organizationId != employee.organization.id!! || dto.pinfl != employee.pinfl) {
                organization = organizationRepository.findByIdNotDeleted(dto.organizationId)
                    ?: throw OrganizationNotFoundException("Organization id ${dto.organizationId} is not found")
                val existEmployee = employeeRepository.findByPinflAndOrganization(dto.pinfl, organization)
                if (existEmployee != null && existEmployee.deleted) {
                    throw PinflOrganizationExistException("pinfl ${dto.pinfl} and organization ${dto.organizationId} is invalid, choose another valid ones")
                } else if (existEmployee != null) {
                    throw PinflOrganizationExistException("pinfl ${dto.pinfl} and organization ${dto.organizationId} is exist already")
                } else {
                    employee.organization = organization
                    employee.pinfl = dto.pinfl
                }
            }
        }

        dto.lastName?.let {
            employee.lastName = it
        }
        dto.firstName?.let {
            employee.firstName = it
        }
        dto.hireDate?.let {
            employee.hireDate = it
        }

        return GetEmployeeDto.toResponse(employeeRepository.save(employee))
    }

    override fun delete(id: Long) {
        employeeRepository.findByIdNotDeleted(id)
            ?: throw EmployeeNotFoundException("employee id $id is not found")
        employeeRepository.trash(id)
    }

    override fun getById(id: Long): GetEmployeeDto {
        val employee = employeeRepository.findByIdNotDeleted(id)
            ?: throw EmployeeNotFoundException("employee id $id is not found")
        return GetEmployeeDto.toResponse(employee)
    }

    override fun getAll(pageable: Pageable): Page<GetEmployeeDto> =
        employeeRepository.findAllNotDeleted(pageable).map(GetEmployeeDto.Companion::toResponse)
}


@Service
class CalculationTableServiceImpl(
    private val calculationTableRepository: CalculationTableRepository,
    private val employeeRepository: EmployeeRepository,
    private val organizationRepository: OrganizationRepository,
    private val jdbcTemplate: JdbcTemplate,
) : CalculationTableService {
    override fun create(dto: CreateCalculationTableDto): GetCalculationTableDto {
        val employee = employeeRepository.findByIdNotDeleted(dto.employeeId)
            ?: throw EmployeeNotFoundException("employee id ${dto.employeeId} is not found")
        val organization = organizationRepository.findByIdNotDeleted(dto.organizationId)
            ?: throw OrganizationNotFoundException("Organization id ${dto.organizationId} is not found")
        if (!employeeRepository.existsByIdAndOrganization(dto.employeeId, organization)) {
            throw OrganizationNotConnectedEmployeeException("Organization id ${dto.organizationId} not connected to employee id ${dto.employeeId}")
        }
        return GetCalculationTableDto.toResponse(calculationTableRepository.save(dto.toEntity(employee, organization)))
    }

    override fun update(id: Long, dto: UpdateCalculationTableDto): GetCalculationTableDto {
        val calculationTable = calculationTableRepository.findByIdNotDeleted(id)
            ?: throw CalculationTableException("calculationTable id $id is not found")

        dto.employeeId?.let {
            val employee = employeeRepository.findByIdNotDeleted(dto.employeeId)
                ?: throw EmployeeNotFoundException("employee id ${dto.employeeId} is not found")
            calculationTable.employee = employee
        }
        dto.organizationId?.let {
            val organization = organizationRepository.findByIdNotDeleted(id)
                ?: throw OrganizationNotFoundException("Organization id $id is not found")
            calculationTable.organization = organization
        }

        dto.amount?.let {
            calculationTable.amount = it
        }
        dto.date?.let {
            calculationTable.date = it
        }
        dto.rate?.let {
            calculationTable.rate = it
        }
        dto.calculationType?.let {
            calculationTable.calculationType = it
        }
        return GetCalculationTableDto.toResponse(calculationTableRepository.save(calculationTable))
    }

    override fun delete(id: Long) {
        calculationTableRepository.findByIdNotDeleted(id)
            ?: throw CalculationTableException("calculationTable id $id is not found")
        calculationTableRepository.trash(id)
    }

    override fun getById(id: Long): GetCalculationTableDto {
        val calculationTable = calculationTableRepository.findByIdNotDeleted(id)
            ?: throw CalculationTableException("calculationTable id $id is not found")
        return GetCalculationTableDto.toResponse(calculationTable)
    }

    override fun getAll(pageable: Pageable): Page<GetCalculationTableDto> =
        calculationTableRepository.findAllNotDeleted(pageable).map(GetCalculationTableDto.Companion::toResponse)

    override fun getAllRate(pageable: Pageable, date: LocalDate, rate: Double): PageImpl<GetAllRateDto> {
        val filter = mutableListOf<String>()

        filter.add("to_char('$date'::DATE, 'YYYY-MM')= to_char(c.date, 'YYYY-MM')")
        filter.add("c.rate > $rate")
        val filterString = "where  ${filter.joinToString(" and ")}"

        val commonQuery = """
            from 
            calculation_table c
            left join employee e on e.id = c.employee_id
            $filterString
            group by e.pinfl
        """.trimIndent()

        val query = """ 
            select e.pinfl, sum(c.rate) as all_rate $commonQuery  
        """.trimIndent()

        val countQuery = """
            select count(*) $commonQuery  
        """.trimIndent()

        val result = jdbcTemplate.query(query, BeanPropertyRowMapper(GetAllRateDto::class.java))
        val count = jdbcTemplate.queryForObject(countQuery, Long::class.java)!!
        return PageImpl(result, pageable, count)
    }


    override fun getAllDifferentRegion(date: LocalDate): List<GetDifferentRegionDto> {
        val filter = mutableListOf<String>()

        filter.add("to_char('$date'::DATE, 'YYYY-MM') = to_char(c.date, 'YYYY-MM')")
        val filterString = "where  ${filter.joinToString("")}"


        val query = """ 
        with result as (
            select SUM(c.amount) AS amount, e.pinfl, c.organization_id, r.name 
            from calculation_table c
            left join employee e on e.id = c.employee_id
            left join organization o on o.id = c.organization_id
            left join region r on r.id = o.region_id
            $filterString
            group by e.pinfl, r.name, c.organization_id
        ),
        result2 as (
            select count(r.organization_id) as count_organization, sum(r.amount) as amount, r.name, r.pinfl 
            from result r 
            group by r.pinfl, r.name
        ),
        result3 as (
            select * 
            from result2 r2 
            where r2.pinfl in (
                select r2.pinfl 
                from result2 r2 
                group by r2.pinfl 
                having count(r2.pinfl) > 1
            )
        )
        select count(r3.count_organization) as all_organization, sum(r3.amount) as all_amount 
        from result3 r3
        """.trimIndent()

        return jdbcTemplate.query(query, BeanPropertyRowMapper(GetDifferentRegionDto::class.java))
    }

    override fun getAllChildOrganization(
        date: LocalDate,
        organizationId: Long,
    ): List<GetChildOrganizationsDto> {
        val filter = mutableListOf<String>()

        filter.add("to_char('$date'::DATE, 'YYYY-MM') = to_char(c.date, 'YYYY-MM')")
        val filterString = "where  ${filter.joinToString("")}"


        val query = """ 
            WITH recursive result AS (
                SELECT e.id AS employee_id, e.first_name || ' ' || e.last_name as full_name, o.id AS organization_id,o.name as organization_name, o.parent_id, SUM(c.amount) AS amount, c.date
                FROM organization o
                LEFT JOIN calculation_table c ON c.organization_id = o.id
                LEFT JOIN employee e ON e.organization_id = o.id AND e.id = c.employee_id
                $filterString
                GROUP BY o.id, e.id, c.date
            ),      
           
           childOrganizations AS (
                SELECT distinct r.employee_id, r.full_name, r.organization_id, r.organization_name, r.parent_id, r.amount, r.date
                FROM result r
                WHERE r.parent_id = $organizationId
            
                UNION ALL
            
                SELECT distinct r.employee_id, r.full_name, r.organization_id, r.organization_name, r.parent_id, r.amount, r.date
                FROM result r
                JOIN childOrganizations co ON r.parent_id = co.organization_id
            )
           
           
           select * from childOrganizations
        """.trimIndent()

        return jdbcTemplate.query(query, BeanPropertyRowMapper(GetChildOrganizationsDto::class.java))
    }

    override fun getAllEmployeeInfo(date: LocalDate): List<GetEmployeeInfoDto> {
        val filter = mutableListOf<String>()

        filter.add("to_char('$date'::DATE, 'YYYY-MM') = to_char(c.date, 'YYYY-MM')")
        val filterString = "where  ${filter.joinToString("")}"


        val query = """ 
             with result as (SELECT c.employee_id,  o.name as orgnization_name, sum(c.amount) as amount, c.calculation_type, c.date from calculation_table c
             left join organization o on c.organization_id = o.id
             $filterString
             group by o.id, c.calculation_type, c.date, c.employee_id)
             
             select e.first_name || ' ' || e.first_name as full_name, r.orgnization_name, r.amount, r.calculation_type, r.date from result r
             left join employee e on r.employee_id = e.id
        """.trimIndent()



        return jdbcTemplate.query(query, BeanPropertyRowMapper(GetEmployeeInfoDto::class.java))
    }
}