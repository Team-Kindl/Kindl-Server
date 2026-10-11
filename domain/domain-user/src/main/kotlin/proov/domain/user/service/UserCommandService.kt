package proov.domain.user.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import proov.core.exception.ProovException
import proov.domain.user.User
import proov.domain.user.command.CreateUserCommand
import proov.domain.user.exception.UserFailure
import proov.domain.user.port.UserRepositoryPort
import java.time.Instant

@Service
class UserCommandService(
    private val userRepositoryPort: UserRepositoryPort,
) {
    @Transactional
    fun create(command: CreateUserCommand, now: Instant): User {
        if (userRepositoryPort.existsByNicknameKey(command.nickname.key)) throw ProovException(UserFailure.NICKNAME_TAKEN)
        return userRepositoryPort.create(User.create(command, now))
    }
}
