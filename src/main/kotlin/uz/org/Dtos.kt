package uz.org

import jakarta.validation.constraints.NotNull
import org.hibernate.validator.internal.util.privilegedactions.GetResource
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
    val parent: Organization?,
) {
    fun toEntity(region: Region) = Organization(name, region, parent)
}

data class UpdateOrganizationDto(
    val name: String?,
    val regionId: Long?,
    val parent: Organization?,
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
    @field:NotNull val organizationId: Long,
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