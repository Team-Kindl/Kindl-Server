package kindl.global.auth.oauth

/**
 * 로그인 시 검증된 소셜 신원. 미가입 유저의 경우 회원가입 완료 전까지 잠시 보관한다.
 */
data class RegistrationInfo(
    val provider: OauthProvider,
    val providerId: String,
    val email: String?,
) {
    companion object {
        fun from(oauthUserInfo: OauthUserInfo): RegistrationInfo = RegistrationInfo(
            provider = oauthUserInfo.provider,
            providerId = oauthUserInfo.providerId,
            email = oauthUserInfo.email,
        )
    }
}
