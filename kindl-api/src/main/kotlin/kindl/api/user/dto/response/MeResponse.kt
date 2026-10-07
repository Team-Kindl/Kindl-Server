package kindl.api.user.dto.response

import kindl.domain.user.dto.result.UserResult

data class MeResponse(
    val id: String,
    val nickname: String,
    val timezone: String,
    val locale: String,
) {
    companion object {
        fun from(user: UserResult) = MeResponse(user.id, user.nickname, user.timezone.id, user.locale.toLanguageTag())
    }
}
