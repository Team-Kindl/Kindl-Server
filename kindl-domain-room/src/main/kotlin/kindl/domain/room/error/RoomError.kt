package kindl.domain.room.error

import kindl.core.error.ErrorCode
import kindl.core.error.ErrorType

enum class RoomError(override val type: ErrorType) : ErrorCode {
    ROOM_NAME_LENGTH(ErrorType.INVALID_INPUT),
    ROOM_NAME_INVALID_CHAR(ErrorType.INVALID_INPUT),
    ROOM_LIMIT_EXCEEDED(ErrorType.CONFLICT),
    ROOM_FULL(ErrorType.CONFLICT),
    ROOM_ENDED(ErrorType.CONFLICT),
    ALREADY_JOINED(ErrorType.CONFLICT),
    INVITE_NOT_FOUND(ErrorType.NOT_FOUND),
    BANNED_FROM_ROOM(ErrorType.FORBIDDEN),
    OWNER_ONLY(ErrorType.FORBIDDEN),
    CANNOT_KICK_SELF(ErrorType.INVALID_INPUT),
    ;

    override val code: String get() = name
}
