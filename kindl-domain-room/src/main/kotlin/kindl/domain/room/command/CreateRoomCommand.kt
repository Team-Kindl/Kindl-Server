package kindl.domain.room.command

import kindl.domain.room.vo.RoomName

data class CreateRoomCommand(
    val userId: String,
    val name: RoomName,
    val clientRequestId: String,
)
