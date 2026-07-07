package kindl.domain.auth.model

import kindl.domain.user.entity.OauthProvider

@ConsistentCopyVisibility
data class OauthUserInfo private constructor(
    val provider: OauthProvider,
    val providerId: String,
    val email: String?,
) {
    companion object {
        fun of(
            provider: OauthProvider,
            providerId: String,
            email: String?,
        ): OauthUserInfo = OauthUserInfo(
            provider = provider,
            providerId = providerId,
            email = email,
        )
    }
}
