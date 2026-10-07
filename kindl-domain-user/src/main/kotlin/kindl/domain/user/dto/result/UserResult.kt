package kindl.domain.user.dto.result

import kindl.domain.user.entity.User
import java.time.ZoneId
import java.util.Locale

data class UserResult(
    val id: String,
    val nickname: String,
    val timezone: ZoneId,
    val locale: Locale,
) {
    companion object {
        fun from(user: User) = UserResult(
            id = requireNotNull(user.id),
            nickname = user.nickname,
            timezone = user.timezone,
            locale = user.locale,
        )
    }
}
