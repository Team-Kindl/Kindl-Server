package kindl.api.common.exception

import jakarta.servlet.http.HttpServletRequest
import kindl.api.common.response.FailureResponse
import kindl.core.error.CommonError
import kindl.core.error.ErrorCode
import kindl.core.error.ErrorType
import kindl.core.error.KindlException
import kindl.domain.auth.error.AuthError
import kindl.domain.room.error.RoomError
import kindl.domain.user.error.UserError
import kindl.domain.verification.error.VerificationError
import org.hibernate.exception.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.ServletRequestBindingException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalExceptionHandler(
    private val responder: ErrorResponder,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(KindlException::class)
    fun handleDomain(e: KindlException, request: HttpServletRequest): ResponseEntity<FailureResponse> {
        when (e.errorCode.type) {
            ErrorType.INTERNAL -> log.error("[{}] {}", e.errorCode.code, e.message, e)
            ErrorType.EXTERNAL -> log.warn("[{}] {}", e.errorCode.code, e.message, e)
            // 4xx는 스택 없이 INFO. ERROR로 찍으면 사용자 입력 실수로 알림이 도배된다
            else -> log.info("[{}] {}", e.errorCode.code, e.message)
        }
        return responder.respond(e.errorCode, request)
    }

    // 사전 확인을 통과해도 경합으로 유니크 위반이 날 수 있다. 제약 이름으로 도메인 오류로 되돌린다
    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleConstraint(e: DataIntegrityViolationException, request: HttpServletRequest): ResponseEntity<FailureResponse> {
        val constraint = constraintNameOf(e)
        val errorCode = UNIQUE_KEY_CODES[constraint] ?: CommonError.CONFLICT_STATE
        log.info("[{}] constraint={}", errorCode.code, constraint)
        return responder.respond(errorCode, request)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleInvalidBody(e: MethodArgumentNotValidException, request: HttpServletRequest): ResponseEntity<FailureResponse> =
        invalidInput(request, e.bindingResult.fieldErrors.map { FailureResponse.FieldError(it.field, it.defaultMessage ?: "invalid") })

    @ExceptionHandler(HandlerMethodValidationException::class)
    fun handleInvalidParameter(e: HandlerMethodValidationException, request: HttpServletRequest): ResponseEntity<FailureResponse> =
        invalidInput(
            request,
            e.parameterValidationResults.flatMap { result ->
                result.resolvableErrors.map {
                    FailureResponse.FieldError(result.methodParameter.parameterName ?: "parameter", it.defaultMessage ?: "invalid")
                }
            },
        )

    @ExceptionHandler(
        MethodArgumentTypeMismatchException::class,
        MissingServletRequestParameterException::class,
        ServletRequestBindingException::class,
        HttpMessageNotReadableException::class,
    )
    fun handleBadRequest(e: Exception, request: HttpServletRequest): ResponseEntity<FailureResponse> =
        invalidInput(request, null).also { log.info("[{}] {}", CommonError.INVALID_INPUT.code, e.javaClass.simpleName) }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(request: HttpServletRequest): ResponseEntity<FailureResponse> =
        responder.respond(CommonError.METHOD_NOT_ALLOWED, request)

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResource(request: HttpServletRequest): ResponseEntity<FailureResponse> =
        responder.respond(CommonError.RESOURCE_NOT_FOUND, request)

    @ExceptionHandler(Exception::class)
    fun handleUnknown(e: Exception, request: HttpServletRequest): ResponseEntity<FailureResponse> {
        log.error("[{}] unhandled", CommonError.INTERNAL_ERROR.code, e)
        return responder.respond(CommonError.INTERNAL_ERROR, request)
    }

    private fun invalidInput(request: HttpServletRequest, errors: List<FailureResponse.FieldError>?) =
        responder.respond(CommonError.INVALID_INPUT, request, errors?.takeIf { it.isNotEmpty() })

    private fun constraintNameOf(e: Throwable): String? =
        generateSequence(e) { it.cause }
            .filterIsInstance<ConstraintViolationException>()
            .firstNotNullOfOrNull { it.constraintName }
            ?.substringAfterLast('.')

    companion object {
        private val UNIQUE_KEY_CODES: Map<String, ErrorCode> = mapOf(
            "uk_users_nickname" to UserError.NICKNAME_TAKEN,
            "uk_social" to AuthError.ALREADY_SIGNED_UP,
            "uk_member" to RoomError.ALREADY_JOINED,
            "uk_report" to VerificationError.ALREADY_REPORTED,
        )
    }
}
