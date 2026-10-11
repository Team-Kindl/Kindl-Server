package proov.domain.auth.port

import proov.domain.auth.SocialIdentity
import java.time.Instant

interface SignupTokenPort {
    fun issue(identity: SocialIdentity, now: Instant): String

    fun decode(token: String): SocialIdentity
}
