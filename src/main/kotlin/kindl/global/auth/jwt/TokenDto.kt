package kindl.global.auth.jwt

@ConsistentCopyVisibility
data class TokenDto private constructor(
    val accessToken: Token,
    val refreshToken: Token,
) {
    @ConsistentCopyVisibility
    data class Token private constructor(
        val token: String,
        val expiredAt: Long,
    ) {
        companion object {
            fun of(
                token: String,
                expiredAt: Long,
            ): Token = Token(
                token = token,
                expiredAt = expiredAt,
            )
        }
    }

    companion object {
        fun of(
            accessToken: Token,
            refreshToken: Token,
        ): TokenDto = TokenDto(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )

        fun of(
            accessToken: String,
            accessTokenExpiredAt: Long,
            refreshToken: String,
            refreshTokenExpiredAt: Long,
        ): TokenDto = of(
            accessToken = Token.of(accessToken, accessTokenExpiredAt),
            refreshToken = Token.of(refreshToken, refreshTokenExpiredAt),
        )
    }
}
