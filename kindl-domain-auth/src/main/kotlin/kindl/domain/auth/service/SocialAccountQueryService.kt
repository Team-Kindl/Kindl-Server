package kindl.domain.auth.service

import kindl.domain.auth.dto.result.SocialAccountResult
import kindl.domain.auth.port.SocialIdentity
import kindl.domain.auth.repository.SocialAccountRepository
import org.springframework.stereotype.Service

@Service
class SocialAccountQueryService(
    private val socialAccounts: SocialAccountRepository,
) {
    fun findActive(identity: SocialIdentity): SocialAccountResult? =
        socialAccounts.findByProviderAndProviderUserId(identity.provider, identity.providerUserId)
            ?.let(SocialAccountResult::from)
}
