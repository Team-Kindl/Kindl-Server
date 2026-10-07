package kindl.domain.user.service

import java.time.Instant
import kindl.core.error.KindlException
import kindl.domain.user.dto.command.CreateUserCommand
import kindl.domain.user.dto.result.NicknameAvailability
import kindl.domain.user.dto.result.UserResult
import kindl.domain.user.entity.User
import kindl.domain.user.error.UserError
import kindl.domain.user.repository.UserRepository
import kindl.domain.user.vo.Nickname
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val users: UserRepository,
) {
    @Transactional(readOnly = true)
    fun checkNickname(raw: String): NicknameAvailability {
        val nickname = try {
            Nickname.of(raw)
        } catch (e: KindlException) {
            return NicknameAvailability.unavailable(e.errorCode)
        }
        return if (users.existsByNicknameKey(nickname.key)) {
            NicknameAvailability.unavailable(UserError.NICKNAME_TAKEN)
        } else {
            NicknameAvailability.AVAILABLE
        }
    }

    /**
     * 사전 확인을 통과해도 경합으로 uk_users_nickname 위반이 날 수 있다.
     * saveAndFlush로 위반을 이 자리에서 드러내고, 전역 핸들러가 NICKNAME_TAKEN으로 바꾼다.
     */
    @Transactional
    fun create(command: CreateUserCommand, now: Instant): UserResult {
        if (users.existsByNicknameKey(command.nickname.key)) throw KindlException(UserError.NICKNAME_TAKEN)
        val user = User.create(command.nickname, command.timezone, command.locale, command.termsVersion, now)
        return UserResult.from(users.saveAndFlush(user))
    }

    @Transactional(readOnly = true)
    fun requireActive(userId: String): UserResult =
        users.findByIdOrNull(userId)?.let(UserResult::from) ?: throw KindlException(UserError.USER_NOT_FOUND)
}
