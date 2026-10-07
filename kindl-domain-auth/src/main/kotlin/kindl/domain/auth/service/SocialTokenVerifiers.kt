package kindl.domain.auth.service

import kindl.core.error.KindlException
import kindl.core.type.SocialProvider
import kindl.domain.auth.error.AuthError
import kindl.domain.auth.port.SocialIdentity
import kindl.domain.auth.port.SocialTokenVerifier
import org.springframework.stereotype.Component

/** 등록된 검증기를 제공자별로 고른다. 새 제공자 = 검증기 빈 하나 추가. */
@Component
class SocialTokenVerifiers(
    verifiers: List<SocialTokenVerifier>,
) {
    private val byProvider: Map<SocialProvider, SocialTokenVerifier> = verifiers.associateBy { it.provider }

    fun verify(provider: SocialProvider, token: String): SocialIdentity =
        (byProvider[provider] ?: throw KindlException(AuthError.SOCIAL_PROVIDER_UNSUPPORTED)).verify(token)
}
