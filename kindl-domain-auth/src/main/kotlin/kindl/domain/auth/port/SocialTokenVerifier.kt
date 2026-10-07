package kindl.domain.auth.port

import kindl.core.type.SocialProvider

/**
 * 제공자별 소셜 토큰 검증. infra 모듈이 구현한다.
 * 실패하면 SOCIAL_TOKEN_INVALID, 제공자 장애면 SOCIAL_PROVIDER_UNAVAILABLE을 던진다.
 */
interface SocialTokenVerifier {
    val provider: SocialProvider

    fun verify(token: String): SocialIdentity
}
