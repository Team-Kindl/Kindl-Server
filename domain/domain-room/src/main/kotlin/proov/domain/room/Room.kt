package proov.domain.room

import proov.core.exception.ProovException
import proov.core.id.Tsids
import proov.domain.room.enums.RoomStatus
import proov.domain.room.exception.RoomFailure
import java.time.Instant

data class Room(
    val id: String,
    val name: RoomName,
    val inviteCode: InviteCode,
    val ownerUserId: String,
    val memberCount: Int,
    val status: RoomStatus,
    val endedAt: Instant?,
    val clientRequestId: String,
) {
    val isEnded: Boolean get() = status == RoomStatus.ENDED

    val isFull: Boolean get() = memberCount >= CAPACITY

    fun requireOwner(userId: String) {
        if (ownerUserId != userId) throw ProovException(RoomFailure.OWNER_ONLY)
    }

    fun requireActive() {
        if (isEnded) throw ProovException(RoomFailure.ROOM_ENDED)
    }

    fun addMember(): Room {
        requireActive()
        if (isFull) throw ProovException(RoomFailure.ROOM_FULL)
        return copy(memberCount = memberCount + 1)
    }

    fun removeMember(): Room {
        check(memberCount > 0) { "멤버 수가 0보다 작아질 수 없습니다." }
        return copy(memberCount = memberCount - 1)
    }

    fun handOverTo(userId: String) = copy(ownerUserId = userId)

    fun end(now: Instant): Room {
        requireActive()
        return copy(status = RoomStatus.ENDED, endedAt = now)
    }

    companion object {
        const val CAPACITY = 10
        const val MAX_ROOMS_PER_USER = 10L

        fun create(name: RoomName, ownerUserId: String, inviteCode: InviteCode, clientRequestId: String) = Room(
            id = Tsids.nextString(),
            name = name,
            inviteCode = inviteCode,
            ownerUserId = ownerUserId,
            memberCount = 1,
            status = RoomStatus.ACTIVE,
            endedAt = null,
            clientRequestId = clientRequestId,
        )
    }
}
