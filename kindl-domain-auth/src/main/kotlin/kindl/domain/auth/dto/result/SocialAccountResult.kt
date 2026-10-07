package kindl.domain.auth.dto.result

import kindl.domain.auth.entity.SocialAccount

data class SocialAccountResult(
    val id: Long,
    val userId: String,
) {
    companion object {
        fun from(account: SocialAccount) = SocialAccountResult(requireNotNull(account.id), account.userId)
    }
}
