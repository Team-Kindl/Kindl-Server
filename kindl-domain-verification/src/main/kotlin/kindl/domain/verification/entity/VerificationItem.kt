package kindl.domain.verification.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import kindl.core.type.VerificationMethod
import kindl.domain.verification.enums.ItemResult
import kindl.support.entity.BaseSoftDeleteEntity
import kindl.support.id.TsidId
import org.hibernate.annotations.SQLRestriction

@Entity
@Table(name = "verification_items")
@SQLRestriction("deleted_at IS NULL")
class VerificationItem private constructor(
    verificationId: String,
    method: VerificationMethod,
    textContent: String?,
    fileId: String?,
) : BaseSoftDeleteEntity() {

    @Id @TsidId
    var id: Long? = null
        protected set

    @Column(nullable = false, length = 13, updatable = false)
    val verificationId: String = verificationId

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    val method: VerificationMethod = method

    // 규칙은 그래핌 300개
    @Column(length = 900)
    var textContent: String? = textContent
        protected set

    // 2단계
    @Column(length = 13)
    var fileId: String? = fileId
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var result: ItemResult = ItemResult.PENDING
        protected set

    @Column(length = 300)
    var rejectReason: String? = null
        protected set

    fun pass() {
        result = ItemResult.PASSED
        rejectReason = null
    }

    fun fail(reason: String) {
        result = ItemResult.FAILED
        rejectReason = reason
    }

    companion object {
        fun text(verificationId: String, content: String) =
            VerificationItem(verificationId, VerificationMethod.TEXT, content, null)

        fun photo(verificationId: String, fileId: String) =
            VerificationItem(verificationId, VerificationMethod.PHOTO, null, fileId)
    }
}
