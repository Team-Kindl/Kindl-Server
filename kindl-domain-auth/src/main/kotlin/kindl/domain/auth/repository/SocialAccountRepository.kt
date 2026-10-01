package kindl.domain.auth.repository

import kindl.core.type.SocialProvider
import kindl.domain.auth.entity.SocialAccount
import org.springframework.data.jpa.repository.JpaRepository

interface SocialAccountRepository : JpaRepository<SocialAccount, Long> {
    fun findByProviderAndProviderUserId(provider: SocialProvider, providerUserId: String): SocialAccount?
}
