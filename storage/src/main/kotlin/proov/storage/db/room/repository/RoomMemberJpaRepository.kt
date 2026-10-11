package proov.storage.db.room.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import proov.domain.room.enums.RoomStatus
import proov.storage.db.room.entity.RoomMemberEntity

interface RoomMemberJpaRepository : JpaRepository<RoomMemberEntity, Long> {

    fun findByRoomIdAndUserId(roomId: String, userId: String): RoomMemberEntity?

    fun existsByRoomIdAndUserId(roomId: String, userId: String): Boolean

    fun findAllByRoomIdOrderByJoinedAtAsc(roomId: String): List<RoomMemberEntity>

    fun findFirstByRoomIdAndUserIdNotOrderByJoinedAtAsc(roomId: String, userId: String): RoomMemberEntity?

    @Query(
        """
        select count(m) from RoomMemberEntity m join RoomEntity r on r.id = m.roomId
        where m.userId = :userId and r.status = :status
        """,
    )
    fun countByUserIdAndRoomStatus(userId: String, status: RoomStatus): Long
}
