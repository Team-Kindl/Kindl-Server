package kindl.domain.verification.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import kindl.domain.verification.enums.ReportReason
import kindl.domain.verification.enums.ReportStatus
import kindl.support.entity.BaseSoftDeleteEntity
import kindl.support.id.TsidId
import org.hibernate.annotations.SQLRestriction

@Entity
@Table(name = "reports")
@SQLRestriction("deleted_at IS NULL")
class Report private constructor(
    verificationId: String,
    reporterUserId: String,
    reason: ReportReason,
) : BaseSoftDeleteEntity() {

    @Id @TsidId
    var id: Long? = null
        protected set

    @Column(nullable = false, length = 13, updatable = false)
    val verificationId: String = verificationId

    @Column(nullable = false, length = 13, updatable = false)
    val reporterUserId: String = reporterUserId

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    val reason: ReportReason = reason

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: ReportStatus = ReportStatus.RECEIVED
        protected set

    var handledAt: Instant? = null
        protected set

    // 처리한 운영자
    @Column(length = 64)
    var handledBy: String? = null
        protected set

    fun handle(result: ReportStatus, operator: String, now: Instant) {
        require(result != ReportStatus.RECEIVED) { "처리 결과는 KEPT 또는 REMOVED여야 합니다." }
        status = result
        handledBy = operator
        handledAt = now
    }

    companion object {
        fun file(verificationId: String, reporterUserId: String, reason: ReportReason) =
            Report(verificationId, reporterUserId, reason)
    }
}
