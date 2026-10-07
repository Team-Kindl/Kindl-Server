package kindl.domain.promise.repository

import kindl.domain.promise.dto.projection.PromiseCountProjection
import kindl.domain.promise.entity.Promise
import kindl.domain.promise.enums.PromiseStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.time.LocalDate

interface PromiseRepository : JpaRepository<Promise, String> {

    @Query("select p.id from Promise p where p.roomId = :roomId and p.userId = :userId")
    fun findIdsByRoomIdAndUserId(roomId: String, userId: String): List<String>

    // 벌크 UPDATE는 Auditing을 거치지 않으므로 updated_at을 직접 넣는다
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        """
        update Promise p set p.deletedAt = :now, p.updatedAt = :now
        where p.id in :ids and p.deletedAt is null
        """,
    )
    fun softDeleteAllByIds(ids: Collection<String>, now: Instant): Int

    /** 모임 종료: 진행 중 공약을 오늘로 끝낸다. 아직 시작 전인 공약은 종료일을 바꾸지 않는다 */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        """
        update Promise p
        set p.status = :completed, p.completedAt = :now, p.updatedAt = :now,
            p.endDate = case when p.startDate <= :today then :today else p.endDate end
        where p.roomId = :roomId and p.userId = :userId and p.status = :active and p.deletedAt is null
        """,
    )
    fun completeAllActive(
        roomId: String,
        userId: String,
        today: LocalDate,
        now: Instant,
        active: PromiseStatus = PromiseStatus.ACTIVE,
        completed: PromiseStatus = PromiseStatus.COMPLETED,
    ): Int

    @Query(
        """
        select p.roomId as roomId, p.userId as userId, count(p) as count from Promise p
        where p.roomId in :roomIds and p.status = :status
        group by p.roomId, p.userId
        """,
    )
    fun countByRoomAndUser(roomIds: Collection<String>, status: PromiseStatus): List<PromiseCountProjection>
}
