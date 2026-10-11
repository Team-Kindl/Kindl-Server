package proov.domain.user.service

import org.springframework.stereotype.Service
import proov.core.exception.ProovException
import proov.core.extension.orThrow
import proov.domain.user.Nickname
import proov.domain.user.NicknameAvailability
import proov.domain.user.User
import proov.domain.user.exception.UserFailure
import proov.domain.user.port.UserRepositoryPort

@Service
class UserQueryService(
    private val userRepositoryPort: UserRepositoryPort,
) {
    fun checkNickname(raw: String): NicknameAvailability {
        val nickname = try {
            Nickname.of(raw)
        } catch (e: ProovException) {
            return NicknameAvailability.unavailable(e.failure)
        }
        if (userRepositoryPort.existsByNicknameKey(nickname.key)) return NicknameAvailability.unavailable(UserFailure.NICKNAME_TAKEN)
        return NicknameAvailability.AVAILABLE
    }

    fun getById(userId: String): User = userRepositoryPort.findById(userId).orThrow(UserFailure.USER_NOT_FOUND)

    fun getAllByIds(userIds: Collection<String>): List<User> =
        if (userIds.isEmpty()) emptyList() else userRepositoryPort.findAllByIds(userIds)

    fun requireExists(userId: String) {
        if (!userRepositoryPort.existsById(userId)) throw ProovException(UserFailure.USER_NOT_FOUND)
    }
}
