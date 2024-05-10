package uz.org

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler


@ControllerAdvice
class ExceptionControllerAdvice(
//    private val errorMessageSource: ResourceBundleMessageSource
) {

    @ExceptionHandler(BaseException::class)
    fun handleBaseException(e: BaseException): ResponseEntity<*> {
        return ResponseEntity.badRequest().body(e.toResponse(e.getErrorMessageArguments()))
    }
}


sealed class BaseException : RuntimeException() {
    abstract fun errorCode(): ErrorCode
    open fun getErrorMessageArguments(): String? = null
    fun toResponse(messageSource: String?): BaseMessage {
        return BaseMessage(
            errorCode().code,
            messageSource
        )
    }
}

class RegionExistException(private val msg: String?) : BaseException() {
    override fun errorCode(): ErrorCode = ErrorCode.REGION_EXIST
    override fun getErrorMessageArguments(): String? = msg
}

class RegionNotFoundException(private val msg: String) : BaseException() {
    override fun errorCode(): ErrorCode = ErrorCode.REGION_NOT_FOUND
    override fun getErrorMessageArguments(): String = msg
}

class RegionNameInvalidException(private val msg: String) : BaseException() {
    override fun errorCode(): ErrorCode = ErrorCode.REGION_NAME_INVALID
    override fun getErrorMessageArguments(): String = msg
}

class OrganizationNotFoundException(private val msg: String) : BaseException() {
    override fun errorCode(): ErrorCode = ErrorCode.ORGANIZATION_NOT_FOUND
    override fun getErrorMessageArguments(): String = msg
}

class OrganizationNotConnectedEmployeeException(private val msg: String) : BaseException() {
    override fun errorCode(): ErrorCode = ErrorCode.ORGANIZATION_NOT_CONNECTED_EMPLOYEE
    override fun getErrorMessageArguments(): String = msg
}

class EmployeeNotFoundException(private val msg: String) : BaseException() {
    override fun errorCode(): ErrorCode = ErrorCode.EMPLOYEE_NOT_FOUND
    override fun getErrorMessageArguments(): String = msg
}

class PinflOrganizationExistException(private val msg: String) : BaseException() {
    override fun errorCode(): ErrorCode = ErrorCode.PINFL_ORGANIZATION_EXIST
    override fun getErrorMessageArguments(): String = msg
}

class CalculationTableException(private val msg: String) : BaseException() {
    override fun errorCode(): ErrorCode = ErrorCode.CALCULATION_TABLE_EXIST
    override fun getErrorMessageArguments(): String = msg
}
