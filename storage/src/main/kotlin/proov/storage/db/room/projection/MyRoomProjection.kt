package proov.storage.db.room.projection

import proov.domain.room.enums.MemberRole
import proov.domain.room.enums.RoomStatus
import java.time.Instant

data class MyRoomProjection(
    val roomId: String,
    val name: String,
    val inviteCode: String,
    val ownerUserId: String,
    val memberCount: Int,
    val status: RoomStatus,
    val endedAt: Instant?,
    val clientRequestId: String,
    val myRole: MemberRole,
    val joinedAt: Instant,
)
