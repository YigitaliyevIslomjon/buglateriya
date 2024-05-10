package uz.org

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

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
        return GetOrganizationDto.toResponse(organizationRepository.save(dto.toEntity(region)))
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
        dto.parent?.let {
            organization.parent = it
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
    private val organizationRepository: OrganizationRepository
) : CalculationTableService {
    override fun create(dto: CreateCalculationTableDto): GetCalculationTableDto {
        val employee = employeeRepository.findByIdNotDeleted(dto.employeeId)
            ?: throw EmployeeNotFoundException("employee id ${dto.employeeId} is not found")
        val organization = organizationRepository.findByIdNotDeleted(dto.organizationId)
            ?: throw OrganizationNotFoundException("Organization id ${dto.organizationId} is not found")
        if(!employeeRepository.existsByIdAndOrganization(dto.employeeId,organization)){
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
}