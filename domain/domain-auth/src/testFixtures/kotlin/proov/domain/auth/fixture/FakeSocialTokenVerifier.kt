package proov.domain.auth.fixture

import proov.core.exception.ProovException
import proov.core.type.SocialProvider
import proov.domain.auth.SocialIdentity
import proov.domain.auth.exception.AuthFailure
import proov.domain.auth.port.SocialTokenVerifierPort

class FakeSocialTokenVerifier(
    override val provider: SocialProvider,
) : SocialTokenVerifierPort {

    override fun verify(token: String): SocialIdentity = when (token) {
        INVALID -> throw ProovException(AuthFailure.SOCIAL_TOKEN_INVALID)
        UNAVAILABLE -> throw ProovException(AuthFailure.SOCIAL_PROVIDER_UNAVAILABLE)
        else -> SocialIdentity(provider, providerUserId = token, email = "$token@example.com")
    }

    companion object {
        const val INVALID = "invalid"
        const val UNAVAILABLE = "unavailable"
    }
}
