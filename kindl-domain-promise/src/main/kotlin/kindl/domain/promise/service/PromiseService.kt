package kindl.domain.promise.service

import kindl.domain.promise.enums.PromiseStatus
import kindl.domain.promise.repository.PromiseRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate

@Service
class PromiseService(
    private val promises: PromiseRepository,
) {
    /** 모임을 나가거나 내보내질 때 그 모임의 그 사람 공약을 지운다. 지운 공약 ID를 돌려줘 인증 삭제로 이어 간다 */
    @Transactional
    fun deleteAllOf(roomId: String, userId: String, now: Instant): List<String> {
        val ids = promises.findIdsByRoomIdAndUserId(roomId, userId)
        if (ids.isNotEmpty()) promises.softDeleteAllByIds(ids, now)
        return ids
    }

    @Transactional
    fun completeAllOf(roomId: String, userId: String, today: LocalDate, now: Instant): Int =
        promises.completeAllActive(roomId, userId, today, now)

    /** 모임별·멤버별 진행 중 공약 수. 모임 목록·멤버 목록이 쿼리 한 번으로 그린다 */
    @Transactional(readOnly = true)
    fun activeCounts(roomIds: Collection<String>): ActivePromiseCounts {
        if (roomIds.isEmpty()) return ActivePromiseCounts(emptyMap())
        val rows = promises.countByRoomAndUser(roomIds, PromiseStatus.ACTIVE)
        return ActivePromiseCounts(rows.groupBy { it.roomId }.mapValues { (_, byUser) -> byUser.associate { it.userId to it.count.toInt() } })
    }
}

data class ActivePromiseCounts(
    private val byRoomAndUser: Map<String, Map<String, Int>>,
) {
    fun ofRoom(roomId: String): Int = byRoomAndUser[roomId]?.values?.sum() ?: 0

    fun of(roomId: String, userId: String): Int = byRoomAndUser[roomId]?.get(userId) ?: 0
}
