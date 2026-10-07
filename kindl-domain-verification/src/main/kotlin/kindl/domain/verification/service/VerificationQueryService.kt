package kindl.domain.verification.service

import kindl.domain.verification.dto.result.MemberRecord
import kindl.domain.verification.repository.CheerRepository
import kindl.domain.verification.repository.VerificationRepository
import org.springframework.stereotype.Service

@Service
class VerificationQueryService(
    private val verifications: VerificationRepository,
    private val cheers: CheerRepository,
) {
    /** 나가기 시트 안내 숫자: 이 모임의 내 인증 수 · 받은 응원 수 */
    fun recordOf(roomId: String, userId: String): MemberRecord = MemberRecord(
        verificationCount = verifications.countByRoomIdAndUserId(roomId, userId).toInt(),
        receivedCheerCount = cheers.countReceivedInRoom(roomId, userId).toInt(),
    )
}
