package proov.domain.room

import proov.core.id.Tsids
import proov.domain.room.enums.MemberRole
import java.time.Instant

data class RoomMember(
    val id: Long,
    val roomId: String,
    val userId: String,
    val role: MemberRole,
    val joinedAt: Instant,
) {
    fun promoteToOwner() = copy(role = MemberRole.OWNER)

    companion object {
        fun owner(roomId: String, userId: String, now: Instant) = RoomMember(Tsids.nextLong(), roomId, userId, MemberRole.OWNER, now)

        fun member(roomId: String, userId: String, now: Instant) = RoomMember(Tsids.nextLong(), roomId, userId, MemberRole.MEMBER, now)
    }
}
