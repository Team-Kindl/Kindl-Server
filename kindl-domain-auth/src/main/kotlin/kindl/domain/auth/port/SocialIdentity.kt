package kindl.domain.auth.port

import kindl.core.type.SocialProvider

/** 소셜 토큰 검증으로 얻은 신원. 사람은 (provider, providerUserId)로만 식별한다. */
data class SocialIdentity(
    val provider: SocialProvider,
    val providerUserId: String,
    val email: String?,
)
