package uz.org

import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

const val API_PREFIX = "api/v1"

@RestController
@RequestMapping("$API_PREFIX/region")
class RegionController(
    private val regionService: RegionService,
) {
    @PostMapping
    fun create(@RequestBody  @Valid dto: CreateRegionDto) = regionService.create(dto)

    @PutMapping("{id}")
    fun update(@PathVariable id: Long, @RequestBody  @Valid dto: UpdateRegionDto) = regionService.update(id, dto)

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long) = regionService.delete(id)

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long) = regionService.getById(id)

    @GetMapping()
    fun getAll(pageable: Pageable) = regionService.getAll(pageable)
}


@RestController
@RequestMapping("$API_PREFIX/organization")
class OrganizationController(
    private val organizationService: OrganizationService,
) {
    @PostMapping
    fun create(@RequestBody dto: CreateOrganizationDto) = organizationService.create(dto)

    @PutMapping("{id}")
    fun update(@PathVariable id: Long, @RequestBody  @Valid dto: UpdateOrganizationDto) = organizationService.update(id, dto)

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long) = organizationService.delete(id)

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long) = organizationService.getById(id)

    @GetMapping()
    fun getAll(pageable: Pageable) = organizationService.getAll(pageable)
}


@RestController
@RequestMapping("$API_PREFIX/employee")
class EmployeeController(
    private val employeeService: EmployeeService,
) {
    @PostMapping
    fun create(@RequestBody @Valid dto: CreateEmployeeDto) = employeeService.create(dto)

    @PutMapping("{id}")
    fun update(@PathVariable id: Long, @RequestBody @Valid dto: UpdateEmployeeDto) = employeeService.update(id, dto)

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long) = employeeService.delete(id)

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long) = employeeService.getById(id)

    @GetMapping()
    fun getAll(pageable: Pageable) = employeeService.getAll(pageable)
}

@RestController
@RequestMapping("$API_PREFIX/calculation-table")
class CalculationTableController(
    private val calculationTableService: CalculationTableService,
) {
    @PostMapping
    fun create(@RequestBody dto: CreateCalculationTableDto) = calculationTableService.create(dto)

    @PutMapping("{id}")
    fun update(@PathVariable id: Long, @RequestBody @Valid dto: UpdateCalculationTableDto) =
        calculationTableService.update(id, dto)

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long) = calculationTableService.delete(id)

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long) = calculationTableService.getById(id)

    @GetMapping()
    fun getAll(pageable: Pageable) = calculationTableService.getAll(pageable)

    @GetMapping("all-rate")
    fun getAllRate(pageable: Pageable, @RequestParam("date") @DateTimeFormat(pattern = "yyyy.MM.dd") date: LocalDate, @RequestParam("rate") rate: Double) = calculationTableService.getAllRate(pageable,date, rate)

    @GetMapping("diffrent-region")
    fun getAllDifferentRegion(pageable: Pageable, @RequestParam("date") @DateTimeFormat(pattern = "yyyy.MM.dd") date: LocalDate) = calculationTableService.getAllDifferentRegion(date)

    @GetMapping("child-organization")
    fun getAllChildOrganization(pageable: Pageable, @RequestParam("date") @DateTimeFormat(pattern = "yyyy.MM.dd") date: LocalDate, @RequestParam("organizationId") organizationId: Long) = calculationTableService.getAllChildOrganization(date, organizationId)

    @GetMapping("employee-info")
    fun getAllEmployeeInfo(pageable: Pageable, @RequestParam("date") @DateTimeFormat(pattern = "yyyy.MM.dd") date: LocalDate) = calculationTableService.getAllEmployeeInfo(date)

}