package kindl.global.auth.oauth

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
