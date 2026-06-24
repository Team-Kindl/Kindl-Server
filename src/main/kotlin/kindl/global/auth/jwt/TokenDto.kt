package kindl.global.auth.jwt

data class TokenDto(
    val accessToken: Token,
    val refreshToken: Token,
) {
    data class Token(
        val token: String,
        val expiredAt: Long,
    )

    companion object {
        fun of(
            accessToken: String,
            accessTokenExpiredAt: Long,
            refreshToken: String,
            refreshTokenExpiredAt: Long,
        ) = TokenDto(
            accessToken = Token(accessToken, accessTokenExpiredAt),
            refreshToken = Token(refreshToken, refreshTokenExpiredAt),
        )
    }
}
