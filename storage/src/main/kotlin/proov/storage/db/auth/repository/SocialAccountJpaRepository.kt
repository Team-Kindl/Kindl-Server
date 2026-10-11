package proov.storage.db.auth.repository

import org.springframework.data.jpa.repository.JpaRepository
import proov.core.type.SocialProvider
import proov.storage.db.auth.entity.SocialAccountEntity

interface SocialAccountJpaRepository : JpaRepository<SocialAccountEntity, Long> {
    fun findByProviderAndProviderUserId(provider: SocialProvider, providerUserId: String): SocialAccountEntity?
}
