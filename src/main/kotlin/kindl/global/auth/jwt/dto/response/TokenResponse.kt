package kindl.global.auth.jwt.dto.response

@ConsistentCopyVisibility
data class TokenResponse private constructor(
    val accessToken: TokenDetailResponse,
    val refreshToken: TokenDetailResponse,
) {
    @ConsistentCopyVisibility
    data class TokenDetailResponse private constructor(
        val token: String,
        val expiredAt: Long,
    ) {
        companion object {
            fun of(
                token: String,
                expiredAt: Long,
            ): TokenDetailResponse = TokenDetailResponse(
                token = token,
                expiredAt = expiredAt,
            )
        }
    }

    companion object {
        fun of(
            accessToken: TokenDetailResponse,
            refreshToken: TokenDetailResponse,
        ): TokenResponse = TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )

        fun of(
            accessToken: String,
            accessTokenExpiredAt: Long,
            refreshToken: String,
            refreshTokenExpiredAt: Long,
        ): TokenResponse = of(
            accessToken = TokenDetailResponse.of(accessToken, accessTokenExpiredAt),
            refreshToken = TokenDetailResponse.of(refreshToken, refreshTokenExpiredAt),
        )
    }
}
