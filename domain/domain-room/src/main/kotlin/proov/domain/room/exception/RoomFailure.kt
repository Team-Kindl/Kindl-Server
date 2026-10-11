package proov.domain.room.exception

import proov.core.code.FailureCode

enum class RoomFailure(
    override val status: Int,
    override val message: String,
) : FailureCode {
    ROOM_NAME_LENGTH(400, "모임 이름은 1자 이상 15자까지 쓸 수 있어요"),
    ROOM_NAME_INVALID_CHAR(400, "모임 이름에는 이모지를 쓸 수 없어요"),
    CANNOT_KICK_SELF(400, "나 자신은 내보낼 수 없어요"),
    BANNED_FROM_ROOM(403, "모임장이 내보낸 모임이에요"),
    OWNER_ONLY(403, "모임장만 할 수 있어요"),
    ROOM_NOT_FOUND(404, "찾을 수 없는 모임이에요"),
    INVITE_NOT_FOUND(404, "존재하지 않는 초대 코드예요"),
    ROOM_LIMIT_EXCEEDED(409, "모임은 최대 10개까지 참여할 수 있어요"),
    ROOM_FULL(409, "정원이 가득 찬 모임이에요"),
    ROOM_ENDED(409, "종료된 모임이에요"),
    ALREADY_JOINED(409, "이미 참여 중인 모임이에요"),
    INVITE_CODE_EXHAUSTED(500, "잠시 후 다시 시도해 주세요"),
    ;

    override val code: String get() = name
}
