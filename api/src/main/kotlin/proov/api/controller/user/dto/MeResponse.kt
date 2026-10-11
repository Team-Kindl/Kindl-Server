package proov.api.controller.user.dto

import proov.domain.user.User

data class MeResponse(
    val id: String,
    val nickname: String,
    val timezone: String,
    val locale: String,
) {
    companion object {
        fun from(user: User) = MeResponse(user.id, user.nickname.value, user.timezone.id, user.locale.toLanguageTag())
    }
}
