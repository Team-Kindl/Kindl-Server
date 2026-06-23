package kindl.global.exception

import kindl.global.response.FailureResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    // 비즈니스 로직에서 발생한 커스텀 예외
    @ExceptionHandler(CustomException::class)
    fun handleCustomException(
        e: CustomException,
    ) = handle(e.errorCode, e)

    // @Valid 요청 본문 검증 실패 (위반 메시지를 그대로 노출)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(
        e: MethodArgumentNotValidException,
    ) = handle(
        errorCode = ErrorCode.BAD_REQUEST,
        e = e,
        message = e.bindingResult.fieldErrors.firstOrNull()?.defaultMessage
            ?: e.bindingResult.allErrors.firstOrNull()?.defaultMessage,
    )

    // 경로 변수/요청 파라미터 타입 변환 실패
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(
        e: MethodArgumentTypeMismatchException,
    ) = handle(ErrorCode.INVALID_PARAMETER, e)

    // 필수 요청 파라미터 누락
    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParameter(
        e: MissingServletRequestParameterException,
    ) = handle(ErrorCode.BAD_REQUEST, e)

    // 파싱 불가능한 요청 본문 (깨진 JSON 등)
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleNotReadable(
        e: HttpMessageNotReadableException,
    ) = handle(ErrorCode.BAD_REQUEST, e)

    // 매핑되지 않은 HTTP 메서드
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(
        e: HttpRequestMethodNotSupportedException,
    ) = handle(ErrorCode.METHOD_NOT_ALLOWED, e)

    // 존재하지 않는 API/리소스 경로
    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFound(
        e: NoResourceFoundException,
    ) = handle(ErrorCode.NOT_FOUND, e)

    // 그 외 처리되지 않은 모든 예외
    @ExceptionHandler(Exception::class)
    fun handleException(
        e: Exception,
    ) = handle(ErrorCode.INTERNAL_SERVER_ERROR, e)

    // 4xx(클라이언트 책임)는 warn, 5xx(서버 책임)는 스택 트레이스와 함께 error 로 로깅
    private fun handle(
        errorCode: ErrorCode,
        e: Exception,
        message: String? = null,
    ): ResponseEntity<FailureResponse> {
        if (errorCode.httpStatus.is5xxServerError()) {
            log.error("[{}] {}", errorCode.code, e.message, e)
        } else {
            log.warn("[{}] {}", errorCode.code, e.message)
        }
        return FailureResponse.of(errorCode, message ?: errorCode.message)
    }
}
