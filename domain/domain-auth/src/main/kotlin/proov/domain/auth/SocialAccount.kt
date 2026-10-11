package proov.domain.auth

import proov.core.id.Tsids
import proov.core.type.SocialProvider

data class SocialAccount(
    val id: Long,
    val userId: String,
    val provider: SocialProvider,
    val providerUserId: String,
    val email: String?,
) {
    companion object {
        fun link(userId: String, identity: SocialIdentity) =
            SocialAccount(Tsids.nextLong(), userId, identity.provider, identity.providerUserId, identity.email)
    }
}
