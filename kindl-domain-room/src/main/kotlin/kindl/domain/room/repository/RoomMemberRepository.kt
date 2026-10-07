package kindl.domain.room.repository

import kindl.domain.room.entity.RoomMember
import kindl.domain.room.enums.RoomStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface RoomMemberRepository : JpaRepository<RoomMember, Long>, RoomMemberRepositoryCustom {

    fun findByRoomIdAndUserId(roomId: String, userId: String): RoomMember?

    fun existsByRoomIdAndUserId(roomId: String, userId: String): Boolean

    fun findAllByRoomIdOrderByJoinedAtAsc(roomId: String): List<RoomMember>

    // 참여 모임 10개 제한은 진행 중 모임만 센다
    @Query(
        """
        select count(m) from RoomMember m join Room r on r.id = m.roomId
        where m.userId = :userId and r.status = :status
        """,
    )
    fun countByUserIdAndRoomStatus(userId: String, status: RoomStatus): Long

    // 모임장 승계: 나 말고 가장 먼저 들어온 멤버
    fun findFirstByRoomIdAndUserIdNotOrderByJoinedAtAsc(roomId: String, userId: String): RoomMember?
}
