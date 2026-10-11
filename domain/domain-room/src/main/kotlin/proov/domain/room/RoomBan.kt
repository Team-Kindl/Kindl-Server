package proov.domain.room

import proov.core.id.Tsids

data class RoomBan(
    val id: Long,
    val roomId: String,
    val userId: String,
    val bannedByUserId: String,
) {
    companion object {
        fun of(roomId: String, userId: String, bannedByUserId: String) = RoomBan(Tsids.nextLong(), roomId, userId, bannedByUserId)
    }
}
