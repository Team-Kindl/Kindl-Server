package kindl.domain.user.service

import kindl.core.error.KindlException
import kindl.domain.user.dto.command.CreateUserCommand
import kindl.domain.user.dto.result.UserResult
import kindl.domain.user.entity.User
import kindl.domain.user.error.UserError
import kindl.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class UserCommandService(
    private val users: UserRepository,
) {
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
}
