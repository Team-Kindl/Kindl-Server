package kindl.domain.verification.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import kindl.core.error.KindlException
import kindl.domain.verification.error.VerificationError
import kindl.support.entity.BaseSoftDeleteEntity
import kindl.support.id.TsidId
import org.hibernate.annotations.SQLRestriction

/** 인증 한 건에 한 사람이 응원 하나. 바꾸기 = type UPDATE. */
@Entity
@Table(name = "cheers")
@SQLRestriction("deleted_at IS NULL")
class Cheer private constructor(
    verificationId: String,
    senderUserId: String,
    receiverUserId: String,
    type: CheerType,
) : BaseSoftDeleteEntity() {

    @Id @TsidId
    var id: Long? = null
        protected set

    @Column(nullable = false, length = 13, updatable = false)
    val verificationId: String = verificationId

    @Column(nullable = false, length = 13, updatable = false)
    val senderUserId: String = senderUserId

    // 받은 응원 집계·알림용 비정규화
    @Column(nullable = false, length = 13, updatable = false)
    val receiverUserId: String = receiverUserId

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var type: CheerType = type
        protected set

    fun change(type: CheerType) {
        this.type = type
    }

    companion object {
        fun send(verificationId: String, sender: String, receiver: String, type: CheerType): Cheer {
            if (sender == receiver) throw KindlException(VerificationError.CHEER_SELF)
            return Cheer(verificationId, sender, receiver, type)
        }
    }
}
