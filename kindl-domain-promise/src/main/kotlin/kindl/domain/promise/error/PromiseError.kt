package kindl.domain.promise.error

import kindl.core.error.ErrorCode
import kindl.core.error.ErrorType

enum class PromiseError(override val type: ErrorType) : ErrorCode {
    STOP_TOO_EARLY(ErrorType.CONFLICT),
    NOT_ACTIVE(ErrorType.CONFLICT),
    ;

    override val code: String get() = name
}
