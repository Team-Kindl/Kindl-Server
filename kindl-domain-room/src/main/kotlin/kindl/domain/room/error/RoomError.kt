package kindl.domain.room.error

import kindl.core.error.ErrorCode
import kindl.core.error.ErrorType

enum class RoomError(override val type: ErrorType) : ErrorCode {
    ROOM_FULL(ErrorType.CONFLICT),
    ROOM_ENDED(ErrorType.CONFLICT),
    ALREADY_JOINED(ErrorType.CONFLICT),
    ;

    override val code: String get() = name
}
