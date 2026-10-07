package kindl.domain.verification.error

import kindl.core.error.ErrorCode
import kindl.core.error.ErrorType

enum class VerificationError(override val type: ErrorType) : ErrorCode {
    CHEER_SELF(ErrorType.INVALID_INPUT),
    ALREADY_REPORTED(ErrorType.CONFLICT),
    ;

    override val code: String get() = name
}
