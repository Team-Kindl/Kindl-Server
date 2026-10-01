package kindl.api.auth.dto

import kindl.api.auth.AuthTokens
import java.time.Instant

data class TokenResponse(
    val accessToken: String,
    val accessTokenExpiresAt: Instant,
    val refreshToken: String,
    val refreshTokenExpiresAt: Instant,
) {
    companion object {
        fun from(tokens: AuthTokens) = TokenResponse(
            accessToken = tokens.access.token,
            accessTokenExpiresAt = tokens.access.expiresAt,
            refreshToken = tokens.refresh.token,
            refreshTokenExpiresAt = tokens.refresh.expiresAt,
        )
    }
}
