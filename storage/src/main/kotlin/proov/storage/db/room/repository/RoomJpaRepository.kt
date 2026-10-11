package proov.storage.db.room.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import proov.storage.db.room.entity.RoomEntity

interface RoomJpaRepository : JpaRepository<RoomEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RoomEntity r where r.id = :roomId")
    fun findByIdForUpdate(roomId: String): RoomEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RoomEntity r where r.inviteCode = :inviteCode")
    fun findByInviteCodeForUpdate(inviteCode: String): RoomEntity?

    fun findByInviteCode(inviteCode: String): RoomEntity?

    fun findByClientRequestId(clientRequestId: String): RoomEntity?

    fun existsByInviteCode(inviteCode: String): Boolean
}
