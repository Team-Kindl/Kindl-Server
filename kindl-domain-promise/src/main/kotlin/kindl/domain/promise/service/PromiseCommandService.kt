package kindl.domain.promise.service

import kindl.domain.promise.repository.PromiseRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate

@Service
class PromiseCommandService(
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
}
