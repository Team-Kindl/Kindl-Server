package kindl.api.security

internal object TokenClaims {
    const val TYPE = "typ"
    const val ACCESS = "access"
    const val SIGNUP = "signup"

    const val PROVIDER = "provider"
    const val PROVIDER_USER_ID = "providerUserId"
    const val EMAIL = "email"

    // 만료만 따로 표시해 진입점이 TOKEN_EXPIRED와 TOKEN_INVALID를 문자열 비교 없이 가른다
    const val EXPIRED_ERROR_CODE = "token_expired"
}
