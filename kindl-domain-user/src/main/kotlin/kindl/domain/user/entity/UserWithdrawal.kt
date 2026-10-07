package kindl.domain.user.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import kindl.domain.user.enums.WithdrawalReason
import kindl.support.entity.BaseCreatedEntity
import kindl.support.id.TsidId

/**
 * 탈퇴 사유 기록. 개인정보를 담지 않고 지우지도 않는다.
 */
@Entity
@Table(name = "user_withdrawals")
class UserWithdrawal private constructor(
    userId: String,
    reason: WithdrawalReason,
    reasonDetail: String?,
) : BaseCreatedEntity() {

    @Id @TsidId
    var id: Long? = null
        protected set

    @Column(nullable = false, length = 13, updatable = false)
    val userId: String = userId

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    val reason: WithdrawalReason = reason

    // OTHER일 때만
    @Column(length = 300, updatable = false)
    val reasonDetail: String? = reasonDetail

    companion object {
        fun record(userId: String, reason: WithdrawalReason, reasonDetail: String?): UserWithdrawal =
            UserWithdrawal(userId, reason, reasonDetail?.takeIf { reason == WithdrawalReason.OTHER })
    }
}
