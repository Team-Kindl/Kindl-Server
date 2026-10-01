package kindl.domain.user.error

import kindl.core.error.ErrorCode
import kindl.core.error.ErrorType

enum class UserError(override val type: ErrorType) : ErrorCode {
    NICKNAME_LENGTH(ErrorType.INVALID_INPUT),
    NICKNAME_INVALID_CHAR(ErrorType.INVALID_INPUT),
    NICKNAME_BLOCKED(ErrorType.INVALID_INPUT),
    NICKNAME_TAKEN(ErrorType.CONFLICT),
    INVALID_TIMEZONE(ErrorType.INVALID_INPUT),
    INVALID_LOCALE(ErrorType.INVALID_INPUT),

    // 탈퇴한 유저의 토큰. 앱은 로그인 화면으로 보낸다
    USER_NOT_FOUND(ErrorType.UNAUTHENTICATED),
    ;

    override val code: String get() = name
}
