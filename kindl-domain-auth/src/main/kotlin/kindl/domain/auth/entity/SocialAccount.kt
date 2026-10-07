package kindl.domain.auth.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import kindl.core.type.SocialProvider
import kindl.domain.auth.port.SocialIdentity
import kindl.support.entity.BaseSoftDeleteEntity
import kindl.support.id.TsidId
import org.hibernate.annotations.SQLRestriction

/**
 * 유저와 소셜 계정의 연결. 같은 사람이라도 다른 제공자로 로그인하면 다른 계정이다(통합하지 않음).
 */
@Entity
@Table(name = "social_accounts")
@SQLRestriction("deleted_at IS NULL")
class SocialAccount private constructor(
    userId: String,
    provider: SocialProvider,
    providerUserId: String,
    email: String?,
) : BaseSoftDeleteEntity() {

    @Id @TsidId
    var id: Long? = null
        protected set

    @Column(nullable = false, length = 13, updatable = false)
    val userId: String = userId

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    val provider: SocialProvider = provider

    // Kakao 회원번호 / Apple·Google sub
    @Column(nullable = false, length = 255, updatable = false)
    val providerUserId: String = providerUserId

    // 받은 경우만. API로 내보내지 않는다
    @Column(length = 320)
    var email: String? = email
        protected set

    // 탈퇴 시 연결 해제용, 암호화 (Apple refresh token 등)
    @Column(length = 1024)
    var revokeCredential: ByteArray? = null
        protected set

    companion object {
        fun link(userId: String, identity: SocialIdentity) =
            SocialAccount(userId, identity.provider, identity.providerUserId, identity.email)
    }
}
