package proov.domain.auth.port

import proov.core.type.SocialProvider
import proov.domain.auth.SocialAccount

interface SocialAccountRepositoryPort {
    fun create(account: SocialAccount): SocialAccount

    fun findByProviderAndProviderUserId(provider: SocialProvider, providerUserId: String): SocialAccount?
}
