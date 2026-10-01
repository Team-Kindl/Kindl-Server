package kindl.domain.auth.fixture

import kindl.core.error.KindlException
import kindl.core.type.SocialProvider
import kindl.domain.auth.error.AuthError
import kindl.domain.auth.port.SocialIdentity
import kindl.domain.auth.port.SocialTokenVerifier

/**
 * 외부 호출 없는 가짜 검증기. 토큰 문자열을 그대로 providerUserId로 쓴다.
 * "invalid"는 위조 토큰, "unavailable"은 제공자 장애로 취급한다.
 */
class FakeSocialTokenVerifier(
    override val provider: SocialProvider,
) : SocialTokenVerifier {

    override fun verify(token: String): SocialIdentity = when (token) {
        INVALID -> throw KindlException(AuthError.SOCIAL_TOKEN_INVALID)
        UNAVAILABLE -> throw KindlException(AuthError.SOCIAL_PROVIDER_UNAVAILABLE)
        else -> SocialIdentity(provider, providerUserId = token, email = "$token@example.com")
    }

    companion object {
        const val INVALID = "invalid"
        const val UNAVAILABLE = "unavailable"
    }
}
