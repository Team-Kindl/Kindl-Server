package kindl.api.auth.dto.result

import kindl.api.security.IssuedAccessToken
import kindl.domain.auth.dto.result.IssuedRefreshToken

sealed interface LoginResult {
    data class SignedIn(val tokens: AuthTokens) : LoginResult

    // 소셜 로그인만 끝낸 사람은 아직 유저가 아니다
    data class SignupRequired(val signupToken: String) : LoginResult
}

data class AuthTokens(
    val access: IssuedAccessToken,
    val refresh: IssuedRefreshToken,
)
