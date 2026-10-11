package proov.domain.room

import proov.domain.room.enums.JoinStatus
import proov.domain.room.enums.MemberRole
import java.time.Instant

data class MyRoom(
    val room: Room,
    val myRole: MemberRole,
    val joinedAt: Instant,
)

data class RoomDetail(
    val room: Room,
    val myRole: MemberRole,
    val nextOwnerUserId: String?,
)

data class InvitePreview(
    val joinStatus: JoinStatus,
    val room: Room,
    val previewMemberIds: List<String>,
)

data class LeaveResult(
    val roomDeleted: Boolean,
    val newOwnerUserId: String?,
)
