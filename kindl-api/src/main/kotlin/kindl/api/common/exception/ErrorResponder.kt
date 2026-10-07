package kindl.api.common.exception

import jakarta.servlet.http.HttpServletRequest
import kindl.api.common.response.FailureResponse
import kindl.core.error.ErrorCode
import kindl.core.error.ErrorType
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.servlet.LocaleResolver

/**
 * ErrorCode → HTTP 상태 + 요청 언어 문구. 이 번역은 여기 한 곳만 안다.
 * Security 필터 단계(401·403)도 같은 곳을 거쳐 응답 모양이 같다.
 */
@Component
class ErrorResponder(
    private val messages: MessageSource,
    private val localeResolver: LocaleResolver,
) {
    fun respond(
        errorCode: ErrorCode,
        request: HttpServletRequest,
        errors: List<FailureResponse.FieldError>? = null,
    ): ResponseEntity<FailureResponse> {
        val status = statusOf(errorCode)
        // 번역이 없으면 코드라도 보여 준다
        val message = messages.getMessage(errorCode.messageKey, null, errorCode.code, localeResolver.resolveLocale(request))!!
        return ResponseEntity.status(status).body(FailureResponse(status.value(), errorCode.code, message, errors))
    }

    companion object {
        fun statusOf(errorCode: ErrorCode): HttpStatus = when (errorCode.type) {
            ErrorType.INVALID_INPUT -> HttpStatus.BAD_REQUEST
            ErrorType.UNAUTHENTICATED -> HttpStatus.UNAUTHORIZED
            ErrorType.FORBIDDEN -> HttpStatus.FORBIDDEN
            ErrorType.NOT_FOUND -> HttpStatus.NOT_FOUND
            ErrorType.METHOD_NOT_ALLOWED -> HttpStatus.METHOD_NOT_ALLOWED
            ErrorType.CONFLICT -> HttpStatus.CONFLICT
            ErrorType.RATE_LIMITED -> HttpStatus.TOO_MANY_REQUESTS
            ErrorType.EXTERNAL -> HttpStatus.SERVICE_UNAVAILABLE
            ErrorType.INTERNAL -> HttpStatus.INTERNAL_SERVER_ERROR
        }
    }
}
