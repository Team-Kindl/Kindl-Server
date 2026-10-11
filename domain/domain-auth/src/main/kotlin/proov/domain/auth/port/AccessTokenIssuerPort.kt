package proov.domain.auth.port

import proov.domain.auth.IssuedToken
import java.time.Instant

interface AccessTokenIssuerPort {
    fun issue(userId: String, now: Instant): IssuedToken
}
