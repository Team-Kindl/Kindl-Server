package kindl.domain.auth.model

import kindl.global.auth.jwt.dto.response.TokenResponse

sealed interface OauthLoginResult {
    // 기존 회원: 바로 토큰 발급
    data class LoggedIn(val tokens: TokenResponse) : OauthLoginResult

    // 미가입: 회원가입 필요 (registrationKey 로 이어서 signup)
    data class NeedsSignup(val registrationKey: String) : OauthLoginResult

    companion object {
        fun loggedIn(tokens: TokenResponse): LoggedIn = LoggedIn(tokens)

        fun needsSignup(registrationKey: String): NeedsSignup = NeedsSignup(registrationKey)
    }
}
