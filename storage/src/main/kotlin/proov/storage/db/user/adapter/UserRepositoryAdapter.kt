package proov.storage.db.user.adapter

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import proov.core.exception.ProovException
import proov.domain.user.User
import proov.domain.user.exception.UserFailure
import proov.domain.user.port.UserRepositoryPort
import proov.storage.db.user.entity.UserEntity
import proov.storage.db.user.repository.UserJpaRepository

@Repository
class UserRepositoryAdapter(
    private val userJpaRepository: UserJpaRepository,
) : UserRepositoryPort {

    override fun create(user: User): User = try {
        userJpaRepository.saveAndFlush(UserEntity.from(user)).toDomain()
    } catch (e: DataIntegrityViolationException) {
        throw ProovException(UserFailure.NICKNAME_TAKEN, cause = e)
    }

    override fun findById(userId: String): User? = userJpaRepository.findByIdOrNull(userId)?.toDomain()

    override fun existsById(userId: String): Boolean = userJpaRepository.existsById(userId)

    override fun existsByNicknameKey(nicknameKey: String): Boolean = userJpaRepository.existsByNicknameKey(nicknameKey)
}
