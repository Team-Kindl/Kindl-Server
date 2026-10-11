package proov.domain.auth

import java.time.Instant

data class IssuedToken(
    val token: String,
    val expiresAt: Instant,
)

data class AuthTokens(
    val access: IssuedToken,
    val refresh: IssuedToken,
)

sealed interface LoginResult {
    data class SignedIn(val tokens: AuthTokens) : LoginResult

    data class SignupRequired(val signupToken: String) : LoginResult
}

sealed interface RefreshRotation {
    data class Rotated(val userId: String, val issued: IssuedToken) : RefreshRotation

    data object Invalid : RefreshRotation

    data object Race : RefreshRotation

    data object Reused : RefreshRotation
}
