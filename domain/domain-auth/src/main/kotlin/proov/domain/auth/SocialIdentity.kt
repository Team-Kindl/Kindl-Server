package proov.domain.auth

import proov.core.type.SocialProvider

data class SocialIdentity(
    val provider: SocialProvider,
    val providerUserId: String,
    val email: String?,
)
