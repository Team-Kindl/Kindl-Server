package proov.domain.room.port

import proov.domain.room.RoomBan

interface RoomBanRepositoryPort {
    fun create(ban: RoomBan): RoomBan

    fun existsByRoomIdAndUserId(roomId: String, userId: String): Boolean

    fun deleteAllByRoomId(roomId: String)
}
