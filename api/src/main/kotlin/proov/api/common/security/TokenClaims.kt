package proov.api.common.security

internal object TokenClaims {
    const val TYPE = "typ"
    const val ACCESS = "access"
    const val SIGNUP = "signup"

    const val PROVIDER = "provider"
    const val PROVIDER_USER_ID = "providerUserId"
    const val EMAIL = "email"

    const val EXPIRED_ERROR_CODE = "token_expired"
}
