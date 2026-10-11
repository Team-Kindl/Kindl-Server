package proov.domain.auth.port

import proov.core.type.SocialProvider
import proov.domain.auth.SocialIdentity

interface SocialTokenVerifierPort {
    val provider: SocialProvider

    fun verify(token: String): SocialIdentity
}
