package kindl.infra.social

import com.nimbusds.jose.KeySourceException
import kindl.core.error.KindlException
import kindl.core.type.SocialProvider
import kindl.domain.auth.error.AuthError
import kindl.domain.auth.port.SocialIdentity
import kindl.domain.auth.port.SocialTokenVerifier
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.web.client.RestClientException
import java.io.IOException

/**
 * OIDC ID 토큰을 제공자 공개키(JWK)로 검증한다. Google·Kakao·Apple 모두 이 방식이다.
 * 서명·만료·iss·aud가 맞지 않으면 SOCIAL_TOKEN_INVALID, 공개키를 못 받아 오면 SOCIAL_PROVIDER_UNAVAILABLE.
 */
class OidcSocialTokenVerifier(
    override val provider: SocialProvider,
    // client ID가 설정되지 않은 제공자는 null → SOCIAL_PROVIDER_UNSUPPORTED
    private val decoder: JwtDecoder?,
) : SocialTokenVerifier {

    override fun verify(token: String): SocialIdentity {
        val decoder = decoder ?: throw KindlException(AuthError.SOCIAL_PROVIDER_UNSUPPORTED, "provider=$provider")
        val jwt = try {
            decoder.decode(token)
        } catch (e: JwtException) {
            if (e.isProviderFailure()) {
                throw KindlException(AuthError.SOCIAL_PROVIDER_UNAVAILABLE, "provider=$provider", e)
            }
            throw KindlException(AuthError.SOCIAL_TOKEN_INVALID, "provider=$provider", e)
        }

        val subject = jwt.subject?.takeIf { it.isNotBlank() }
            ?: throw KindlException(AuthError.SOCIAL_TOKEN_INVALID, "provider=$provider, sub 없음")

        return SocialIdentity(provider, subject, jwt.getClaimAsString("email"))
    }

    private fun Throwable.isProviderFailure(): Boolean =
        generateSequence(this) { it.cause }.any { it is IOException || it is RestClientException || it is KeySourceException }
}
