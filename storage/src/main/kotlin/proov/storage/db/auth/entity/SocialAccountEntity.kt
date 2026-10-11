package proov.storage.db.auth.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import proov.core.type.SocialProvider
import proov.domain.auth.SocialAccount
import proov.storage.db.common.BaseEntity

@Entity
@Table(name = "social_accounts", uniqueConstraints = [UniqueConstraint(columnNames = ["provider", "provider_user_id"])])
class SocialAccountEntity(
    id: Long,
    userId: String,
    provider: SocialProvider,
    providerUserId: String,
    email: String?,
) : BaseEntity<Long>() {

    @Id
    private val id: Long = id

    @Column(nullable = false, length = 13)
    val userId: String = userId

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val provider: SocialProvider = provider

    @Column(nullable = false)
    val providerUserId: String = providerUserId

    @Column(length = 320)
    val email: String? = email

    override fun getId(): Long = id

    fun toDomain() = SocialAccount(id, userId, provider, providerUserId, email)

    companion object {
        fun from(account: SocialAccount) =
            SocialAccountEntity(account.id, account.userId, account.provider, account.providerUserId, account.email)
    }
}
