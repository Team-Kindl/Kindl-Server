package kindl.domain.verification.service

import kindl.domain.verification.repository.CheerRepository
import kindl.domain.verification.repository.VerificationItemRepository
import kindl.domain.verification.repository.VerificationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class VerificationService(
    private val verifications: VerificationRepository,
    private val items: VerificationItemRepository,
    private val cheers: CheerRepository,
) {
    /**
     * 모임을 나간 사람의 그 모임 기록을 지운다: 인증 → 인증 항목 → 받은 응원, 그리고 보낸 응원.
     * 모두 같은 now로 지워 "이 나가기로 지워진 행"을 운영에서 묶어 볼 수 있다.
     */
    @Transactional
    fun deleteAllOf(roomId: String, userId: String, promiseIds: Collection<String>, now: Instant) {
        if (promiseIds.isNotEmpty()) {
            val verificationIds = verifications.findIdsByPromiseIds(promiseIds)
            if (verificationIds.isNotEmpty()) {
                items.softDeleteAllByVerificationIds(verificationIds, now)
                cheers.softDeleteAllByVerificationIds(verificationIds, now)
                verifications.softDeleteAllByIds(verificationIds, now)
            }
        }
        cheers.softDeleteAllSentInRoom(roomId, userId, now)
    }

    /** 나가기 시트 안내 숫자: 이 모임의 내 인증 수 · 받은 응원 수 */
    @Transactional(readOnly = true)
    fun recordOf(roomId: String, userId: String): MemberRecord = MemberRecord(
        verificationCount = verifications.countByRoomIdAndUserId(roomId, userId).toInt(),
        receivedCheerCount = cheers.countReceivedInRoom(roomId, userId).toInt(),
    )
}

data class MemberRecord(
    val verificationCount: Int,
    val receivedCheerCount: Int,
)
