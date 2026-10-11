package proov.domain.user.command

import proov.domain.user.Nickname
import java.time.ZoneId
import java.util.Locale

data class CreateUserCommand(
    val nickname: Nickname,
    val timezone: ZoneId,
    val locale: Locale,
    val termsVersion: String,
)
