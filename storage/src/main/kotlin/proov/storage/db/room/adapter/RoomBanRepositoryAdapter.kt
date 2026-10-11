package proov.storage.db.room.adapter

import org.springframework.stereotype.Repository
import proov.domain.room.RoomBan
import proov.domain.room.port.RoomBanRepositoryPort
import proov.storage.db.room.entity.RoomBanEntity
import proov.storage.db.room.repository.RoomBanJpaRepository

@Repository
class RoomBanRepositoryAdapter(
    private val roomBanJpaRepository: RoomBanJpaRepository,
) : RoomBanRepositoryPort {

    override fun create(ban: RoomBan): RoomBan = roomBanJpaRepository.save(RoomBanEntity.from(ban)).toDomain()

    override fun existsByRoomIdAndUserId(roomId: String, userId: String): Boolean =
        roomBanJpaRepository.existsByRoomIdAndUserId(roomId, userId)

    override fun deleteAllByRoomId(roomId: String) {
        roomBanJpaRepository.deleteAllByRoomId(roomId)
    }
}
