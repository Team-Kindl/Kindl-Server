package kindl.global.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val httpStatus: HttpStatus,
    val code: String,
    val message: String,
) {
    /* ========== 공통 ========== */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "ISE", "서버 내부 오류입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FBD", "권한이 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNA", "인증되지 않았습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "MNA", "지원하지 않는 HTTP 메서드입니다."),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "IVP", "요청 파라미터 타입/형식이 올바르지 않습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "NF", "존재하지 않는 리소스입니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "BR", "잘못된 요청입니다."),

    /* ========== 인증/토큰 ========== */
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "ETK", "만료된 토큰입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "ITK", "유효하지 않은 토큰입니다."),
    INVALID_TOKEN_TYPE(HttpStatus.UNAUTHORIZED, "ITT", "토큰 타입이 올바르지 않습니다."),

    /* ========== OAuth ========== */
    INVALID_OAUTH_TOKEN(HttpStatus.UNAUTHORIZED, "IOT", "유효하지 않은 소셜 로그인 토큰입니다."),
    UNSUPPORTED_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST, "UOP", "지원하지 않는 소셜 로그인 제공자입니다."),
    REGISTRATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "RNF", "가입 정보가 만료되었거나 유효하지 않습니다. 다시 로그인해 주세요."),
}
