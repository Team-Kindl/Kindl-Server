package proov.storage.db.room.adapter

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import proov.domain.room.Room
import proov.domain.room.port.RoomRepositoryPort
import proov.storage.db.room.entity.RoomEntity
import proov.storage.db.room.repository.RoomJpaRepository

@Repository
class RoomRepositoryAdapter(
    private val roomJpaRepository: RoomJpaRepository,
) : RoomRepositoryPort {

    override fun create(room: Room): Room = roomJpaRepository.save(RoomEntity.from(room)).toDomain()

    override fun update(room: Room): Room = roomJpaRepository.getReferenceById(room.id).apply { update(room) }.toDomain()

    override fun delete(roomId: String) = roomJpaRepository.deleteById(roomId)

    override fun findById(roomId: String): Room? = roomJpaRepository.findByIdOrNull(roomId)?.toDomain()

    override fun findByIdForUpdate(roomId: String): Room? = roomJpaRepository.findByIdForUpdate(roomId)?.toDomain()

    override fun findByInviteCode(inviteCode: String): Room? = roomJpaRepository.findByInviteCode(inviteCode)?.toDomain()

    override fun findByInviteCodeForUpdate(inviteCode: String): Room? =
        roomJpaRepository.findByInviteCodeForUpdate(inviteCode)?.toDomain()

    override fun findByClientRequestId(clientRequestId: String): Room? =
        roomJpaRepository.findByClientRequestId(clientRequestId)?.toDomain()

    override fun existsByInviteCode(inviteCode: String): Boolean = roomJpaRepository.existsByInviteCode(inviteCode)
}
