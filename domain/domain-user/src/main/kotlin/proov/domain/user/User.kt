package proov.domain.user

import proov.core.id.Tsids
import proov.domain.user.command.CreateUserCommand
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

data class User(
    val id: String,
    val nickname: Nickname,
    val timezone: ZoneId,
    val locale: Locale,
    val termsVersion: String,
    val termsAgreedAt: Instant,
) {
    companion object {
        fun create(command: CreateUserCommand, now: Instant) = User(
            id = Tsids.nextString(),
            nickname = command.nickname,
            timezone = command.timezone,
            locale = command.locale,
            termsVersion = command.termsVersion,
            termsAgreedAt = now,
        )
    }
}
