package kindl.domain.verification.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import kindl.support.entity.BaseSoftDeleteEntity
import kindl.support.id.TsidId
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.time.LocalDate

/**
 * 인증 한 건. 항목(텍스트·사진)은 verification_items에 따로 둔다.
 * (user_id, client_request_id) 유니크가 앱 재시도의 중복 등록을 막는다.
 */
@Entity
@Table(name = "verifications")
@SQLRestriction("deleted_at IS NULL")
class Verification private constructor(
    promiseId: String,
    roomId: String,
    userId: String,
    clientRequestId: String,
    requestedAt: Instant,
    note: String?,
) : BaseSoftDeleteEntity() {

    @Id @TsidId
    @Column(length = 13, updatable = false)
    var id: String? = null
        protected set

    @Column(nullable = false, length = 13, updatable = false)
    val promiseId: String = promiseId

    // 모임 화면 조회용 비정규화
    @Column(nullable = false, length = 13, updatable = false)
    val roomId: String = roomId

    @Column(nullable = false, length = 13, updatable = false)
    val userId: String = userId

    // 앱이 만든 Idempotency-Key
    @Column(nullable = false, length = 36, updatable = false)
    val clientRequestId: String = clientRequestId

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: VerificationStatus = VerificationStatus.PENDING
        protected set

    // 요청 시각 기준, 유저 시간대. 승인될 때 정해진다
    var serviceDate: LocalDate? = null
        protected set

    @Column(nullable = false, updatable = false)
    val requestedAt: Instant = requestedAt

    var reviewedAt: Instant? = null
        protected set

    // 한 줄 기록, 규칙은 그래핌 40개
    @Column(length = 120)
    var note: String? = note
        protected set

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(nullable = false)
    var submitCount: Int = 1
        protected set

    // 4단계
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var appealStatus: AppealStatus = AppealStatus.NONE
        protected set

    fun approve(serviceDate: LocalDate, now: Instant) {
        status = VerificationStatus.APPROVED
        this.serviceDate = serviceDate
        reviewedAt = now
    }

    fun reject(now: Instant) {
        status = VerificationStatus.REJECTED
        reviewedAt = now
    }

    companion object {
        fun request(
            promiseId: String,
            roomId: String,
            userId: String,
            clientRequestId: String,
            note: String?,
            now: Instant,
        ) = Verification(promiseId, roomId, userId, clientRequestId, now, note)
    }
}
