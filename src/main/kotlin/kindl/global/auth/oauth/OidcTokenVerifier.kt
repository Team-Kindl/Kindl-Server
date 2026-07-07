package kindl.global.auth.oauth

import kindl.global.exception.CustomException
import kindl.global.exception.ErrorCode
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.stereotype.Component

@Component
class OidcTokenVerifier(
    private val oidcDecoders: Map<OauthProvider, JwtDecoder>,
) {
    fun verify(provider: OauthProvider, idToken: String): OauthUserInfo {
        val oidcDecoder = oidcDecoders[provider]
            ?: throw CustomException(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER)

        val decodedJwt = try {
            oidcDecoder.decode(idToken)
        } catch (exception: JwtException) {
            throw CustomException(ErrorCode.INVALID_OAUTH_TOKEN)
        }

        val providerId = decodedJwt.subject?.takeIf { subject -> subject.isNotBlank() }
            ?: throw CustomException(ErrorCode.INVALID_OAUTH_TOKEN)

        return OauthUserInfo.of(
            provider = provider,
            providerId = providerId,
            email = decodedJwt.getClaimAsString("email"),
        )
    }
}
