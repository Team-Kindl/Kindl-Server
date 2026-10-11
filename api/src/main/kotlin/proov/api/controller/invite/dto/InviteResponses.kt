package proov.api.controller.invite.dto

import proov.domain.room.Room
import proov.domain.room.enums.JoinStatus
import proov.domain.room.facade.InvitePreviewResult

data class InvitePreviewResponse(
    val joinStatus: JoinStatus,
    val roomId: String,
    val name: String,
    val memberCount: Int,
    val capacity: Int,
    val ownerNickname: String?,
    val memberNicknames: List<String>,
) {
    companion object {
        fun from(result: InvitePreviewResult) = with(result.preview.room) {
            InvitePreviewResponse(
                joinStatus = result.preview.joinStatus,
                roomId = id,
                name = name.value,
                memberCount = memberCount,
                capacity = Room.CAPACITY,
                ownerNickname = result.owner?.nickname?.value,
                memberNicknames = result.previewMembers.map { it.nickname.value },
            )
        }
    }
}

data class JoinRoomResponse(
    val roomId: String,
    val name: String,
) {
    companion object {
        fun from(room: Room) = JoinRoomResponse(room.id, room.name.value)
    }
}
