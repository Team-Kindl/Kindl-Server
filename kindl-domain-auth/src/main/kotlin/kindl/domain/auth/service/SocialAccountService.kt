package kindl.domain.auth.service

import kindl.domain.auth.dto.result.SocialAccountResult
import kindl.domain.auth.entity.SocialAccount
import kindl.domain.auth.port.SocialIdentity
import kindl.domain.auth.repository.SocialAccountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SocialAccountService(
    private val socialAccounts: SocialAccountRepository,
) {
    @Transactional(readOnly = true)
    fun findActive(identity: SocialIdentity): SocialAccountResult? =
        socialAccounts.findByProviderAndProviderUserId(identity.provider, identity.providerUserId)
            ?.let(SocialAccountResult::from)

    /** 가입 버튼을 두 번 누르면 uk_social 위반 → 전역 핸들러가 ALREADY_SIGNED_UP으로 바꾼다. */
    @Transactional
    fun link(userId: String, identity: SocialIdentity): SocialAccountResult =
        SocialAccountResult.from(socialAccounts.saveAndFlush(SocialAccount.link(userId, identity)))
}
