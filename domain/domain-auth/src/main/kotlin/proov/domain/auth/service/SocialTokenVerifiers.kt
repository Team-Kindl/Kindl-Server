package proov.domain.auth.service

import org.springframework.stereotype.Component
import proov.core.exception.ProovException
import proov.core.type.SocialProvider
import proov.domain.auth.SocialIdentity
import proov.domain.auth.exception.AuthFailure
import proov.domain.auth.port.SocialTokenVerifierPort

@Component
class SocialTokenVerifiers(
    verifiers: List<SocialTokenVerifierPort>,
) {
    private val byProvider = verifiers.associateBy { it.provider }

    fun verify(provider: SocialProvider, token: String): SocialIdentity =
        (byProvider[provider] ?: throw ProovException(AuthFailure.SOCIAL_PROVIDER_UNSUPPORTED)).verify(token)
}
