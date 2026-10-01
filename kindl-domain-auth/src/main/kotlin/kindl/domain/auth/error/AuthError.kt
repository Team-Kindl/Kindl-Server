package kindl.domain.auth.error

import kindl.core.error.ErrorCode
import kindl.core.error.ErrorType

enum class AuthError(override val type: ErrorType) : ErrorCode {
    // access token
    AUTH_REQUIRED(ErrorType.UNAUTHENTICATED),
    TOKEN_EXPIRED(ErrorType.UNAUTHENTICATED),
    TOKEN_INVALID(ErrorType.UNAUTHENTICATED),

    // refresh token
    REFRESH_INVALID(ErrorType.UNAUTHENTICATED),
    REFRESH_REUSED(ErrorType.UNAUTHENTICATED),
    // 회전 직후 유예 시간 안의 정상 경합. 앱은 저장소에서 새 토큰을 다시 읽어 재시도한다
    REFRESH_RACE(ErrorType.CONFLICT),

    // 소셜 로그인·가입
    SIGNUP_TOKEN_INVALID(ErrorType.UNAUTHENTICATED),
    SOCIAL_PROVIDER_UNSUPPORTED(ErrorType.INVALID_INPUT),
    SOCIAL_TOKEN_INVALID(ErrorType.UNAUTHENTICATED),
    SOCIAL_PROVIDER_UNAVAILABLE(ErrorType.EXTERNAL),
    ALREADY_SIGNED_UP(ErrorType.CONFLICT),
    ;

    override val code: String get() = name
}
