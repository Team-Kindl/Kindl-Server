package kindl.domain.user.dto.result

import kindl.domain.user.entity.User
import java.time.ZoneId

/** 다른 화면에 작성자·멤버로 보여 줄 최소 정보 */
data class UserSummary(
    val userId: String,
    val nickname: String,
    val profileImageFileId: String?,
    val timezone: ZoneId,
) {
    companion object {
        fun from(user: User) = UserSummary(requireNotNull(user.id), user.nickname, user.profileImageFileId, user.timezone)
    }
}
