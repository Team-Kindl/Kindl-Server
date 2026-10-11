package proov.domain.auth.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import proov.domain.auth.SocialAccount
import proov.domain.auth.SocialIdentity
import proov.domain.auth.port.SocialAccountRepositoryPort

@Service
class SocialAccountService(
    private val socialAccountRepositoryPort: SocialAccountRepositoryPort,
) {
    fun findByIdentity(identity: SocialIdentity): SocialAccount? =
        socialAccountRepositoryPort.findByProviderAndProviderUserId(identity.provider, identity.providerUserId)

    @Transactional
    fun link(userId: String, identity: SocialIdentity): SocialAccount =
        socialAccountRepositoryPort.create(SocialAccount.link(userId, identity))
}
