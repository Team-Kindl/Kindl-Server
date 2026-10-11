package proov.clients.oauth

import com.nimbusds.jose.KeySourceException
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.web.client.RestClientException
import proov.core.exception.ProovException
import proov.core.type.SocialProvider
import proov.domain.auth.SocialIdentity
import proov.domain.auth.exception.AuthFailure
import proov.domain.auth.port.SocialTokenVerifierPort
import java.io.IOException

class OidcSocialTokenVerifier(
    override val provider: SocialProvider,
    private val decoder: JwtDecoder?,
) : SocialTokenVerifierPort {

    override fun verify(token: String): SocialIdentity {
        val decoder = decoder ?: throw ProovException(AuthFailure.SOCIAL_PROVIDER_UNSUPPORTED, "provider=$provider")
        val jwt = try {
            decoder.decode(token)
        } catch (e: JwtException) {
            if (e.isProviderFailure()) {
                throw ProovException(AuthFailure.SOCIAL_PROVIDER_UNAVAILABLE, "provider=$provider", e)
            }
            throw ProovException(AuthFailure.SOCIAL_TOKEN_INVALID, "provider=$provider", e)
        }

        val subject = jwt.subject?.takeIf { it.isNotBlank() }
            ?: throw ProovException(AuthFailure.SOCIAL_TOKEN_INVALID, "provider=$provider, sub 없음")

        return SocialIdentity(provider, subject, jwt.getClaimAsString("email"))
    }

    private fun Throwable.isProviderFailure(): Boolean =
        generateSequence(this) { it.cause }.any { it is IOException || it is RestClientException || it is KeySourceException }
}
