package uz.org

import java.math.BigDecimal
import java.time.LocalDate

data class BaseMessage(
    val code: Int,
    val message: String?,
)

data class CreateRegionDto(
    val name: String,
) {
    fun toEntity() = Region(name)
}

data class UpdateRegionDto(
    val name: String?,
)

data class GetRegionDto(
    val id: Long,
    val name: String,
) {
    companion object {
        fun toResponse(region: Region) = region.run {
            GetRegionDto(id!!, name)
        }
    }
}

data class CreateOrganizationDto(
    val name: String,
    val regionId: Long,
    val parentId: Long?,
) {
    fun toEntity(region: Region, organization: Organization?) = Organization(name, region, organization)
}

data class UpdateOrganizationDto(
    val name: String?,
    val regionId: Long?,
    val parentId: Long?,
)

data class GetOrganizationDto(
    val id: Long,
    val name: String,
    val regionId: Long,
    val parent: Long?,
) {
    companion object {
        fun toResponse(organization: Organization) = organization.run {
            GetOrganizationDto(
                id!!,
                name,
                region.id!!,
                parent?.id
            )
        }
    }
}

data class CreateEmployeeDto(
    val firstName: String,
    val lastName: String,
    val pinfl: Long,
    val hireDate: LocalDate,
    val organizationId: Long,
) {
    fun toEntity(organization: Organization) = Employee(firstName, lastName, pinfl, hireDate, organization)
}

data class UpdateEmployeeDto(
    val firstName: String?,
    val lastName: String?,
    val pinfl: Long?,
    val hireDate: LocalDate?,
    val organizationId: Long?,
)

data class GetEmployeeDto(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val pinfl: Long,
    val hireDate: LocalDate,
    val organizationId: Long,
) {
    companion object {
        fun toResponse(employee: Employee) = employee.run {
            GetEmployeeDto(id!!, firstName, lastName, pinfl, hireDate, organization.id!!)
        }
    }
}

data class CreateCalculationTableDto(
    val employeeId: Long,
    val amount: BigDecimal,
    val rate: Double,
    val date: LocalDate,
    val organizationId: Long,
    val calculationType: CalculationType,
) {
    fun toEntity(employee: Employee, organization: Organization) =
        CalculationTable(employee, amount, rate, date, organization, calculationType)
}

data class UpdateCalculationTableDto(
    val employeeId: Long?,
    val amount: BigDecimal?,
    val rate: Double?,
    val date: LocalDate?,
    val organizationId: Long?,
    val calculationType: CalculationType?,
)

data class GetCalculationTableDto(
    val id: Long,
    val employeeId: Long,
    val amount: BigDecimal,
    val rate: Double,
    val date: LocalDate,
    val organizationId: Long,
    val calculationType: CalculationType,
) {
    companion object {
        fun toResponse(calculationTable: CalculationTable) = calculationTable.run {
            GetCalculationTableDto(id!!, employee.id!!, amount, rate, date, employee.organization.id!!, calculationType)
        }
    }
}

data class GetAllRateDto(
    var pinfl: Long? = null,
    var allRate: Double? = null
)

data class GetDifferentRegionDto(
    var allOrganization: Long? = null,
    var allAmount: Double? = null
)

data class GetChildOrganizationsDto(
    var employeeId: Long? = null,
    var fullName: String? = null,
    var organizationId: Long? = null,
    var organizationName: String? = null,
    var parentId: Long? = null,
    var amount: Double? = null,
    var date: LocalDate? = null
)

data class GetEmployeeInfoDto(
    var fullName: String? = null,
    var orgnizationName: String? = null,
    var amount: Double? = null,
    var calculationType: CalculationType? = null,
    var date: LocalDate? = null,
)


