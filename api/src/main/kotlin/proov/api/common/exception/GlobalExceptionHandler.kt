package proov.api.common.exception

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException
import proov.api.common.response.FailureResponse
import proov.api.common.response.ResponseFactory
import proov.core.exception.ProovException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(ProovException::class)
    fun handleProov(e: ProovException): ResponseEntity<FailureResponse> {
        when {
            e.failure.status == 500 -> log.error("[{}] {}", e.failure.code, e.message, e)
            e.failure.status > 500 -> log.warn("[{}] {}", e.failure.code, e.message, e)
            else -> log.info("[{}] {}", e.failure.code, e.message)
        }
        return ResponseFactory.failure(e.failure)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleInvalidBody(e: MethodArgumentNotValidException): ResponseEntity<FailureResponse> =
        invalidInput(e.bindingResult.fieldErrors.map { FailureResponse.FieldError(it.field, it.defaultMessage ?: "invalid") })

    @ExceptionHandler(HandlerMethodValidationException::class)
    fun handleInvalidParameter(e: HandlerMethodValidationException): ResponseEntity<FailureResponse> =
        invalidInput(
            e.parameterValidationResults.flatMap { result ->
                result.resolvableErrors.map {
                    FailureResponse.FieldError(result.methodParameter.parameterName ?: "parameter", it.defaultMessage ?: "invalid")
                }
            },
        )

    @ExceptionHandler(
        MethodArgumentTypeMismatchException::class,
        MissingServletRequestParameterException::class,
        HttpMessageNotReadableException::class,
    )
    fun handleBadRequest(e: Exception): ResponseEntity<FailureResponse> {
        log.info("[{}] {}", CommonFailure.INVALID_INPUT.code, e.javaClass.simpleName)
        return invalidInput(null)
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(): ResponseEntity<FailureResponse> = ResponseFactory.failure(CommonFailure.METHOD_NOT_ALLOWED)

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResource(): ResponseEntity<FailureResponse> = ResponseFactory.failure(CommonFailure.NOT_FOUND)

    @ExceptionHandler(Exception::class)
    fun handleUnknown(e: Exception): ResponseEntity<FailureResponse> {
        log.error("[{}] unhandled", CommonFailure.INTERNAL_ERROR.code, e)
        return ResponseFactory.failure(CommonFailure.INTERNAL_ERROR)
    }

    private fun invalidInput(errors: List<FailureResponse.FieldError>?) =
        ResponseFactory.failure(CommonFailure.INVALID_INPUT, errors?.takeIf { it.isNotEmpty() })
}
