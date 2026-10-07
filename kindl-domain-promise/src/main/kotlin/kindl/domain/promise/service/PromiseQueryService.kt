package kindl.domain.promise.service

import kindl.domain.promise.dto.result.ActivePromiseCounts
import kindl.domain.promise.enums.PromiseStatus
import kindl.domain.promise.repository.PromiseRepository
import org.springframework.stereotype.Service

@Service
class PromiseQueryService(
    private val promises: PromiseRepository,
) {
    /** 모임별·멤버별 진행 중 공약 수. 모임 목록·멤버 목록이 쿼리 한 번으로 그린다 */
    fun activeCounts(roomIds: Collection<String>): ActivePromiseCounts {
        if (roomIds.isEmpty()) return ActivePromiseCounts(emptyMap())
        val rows = promises.countByRoomAndUser(roomIds, PromiseStatus.ACTIVE)
        return ActivePromiseCounts(rows.groupBy { it.roomId }.mapValues { (_, byUser) -> byUser.associate { it.userId to it.count.toInt() } })
    }
}
