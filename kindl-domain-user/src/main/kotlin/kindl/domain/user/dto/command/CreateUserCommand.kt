package kindl.domain.user.dto.command

import kindl.domain.user.vo.Nickname
import java.time.ZoneId
import java.util.Locale

data class CreateUserCommand(
    val nickname: Nickname,
    val timezone: ZoneId,
    val locale: Locale,
    val termsVersion: String,
)
