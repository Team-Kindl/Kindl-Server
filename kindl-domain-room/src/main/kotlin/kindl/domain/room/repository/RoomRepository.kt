package kindl.domain.room.repository

import jakarta.persistence.LockModeType
import kindl.domain.room.entity.Room
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query

interface RoomRepository : JpaRepository<Room, String> {

    // 입장·나가기·내보내기·종료는 모임 행을 잠그고 정원·상태를 확인한다
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Room r where r.id = :roomId")
    fun findByIdForUpdate(roomId: String): Room?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Room r where r.inviteCode = :inviteCode")
    fun findByInviteCodeForUpdate(inviteCode: String): Room?

    fun findByInviteCode(inviteCode: String): Room?

    fun findByClientRequestId(clientRequestId: String): Room?

    // 지워진 모임의 코드도 재사용하지 않으므로 soft delete 조건 없이 확인한다
    @Query(value = "select count(*) > 0 from rooms where invite_code = :inviteCode", nativeQuery = true)
    fun existsByInviteCodeIncludingDeleted(inviteCode: String): Boolean
}
