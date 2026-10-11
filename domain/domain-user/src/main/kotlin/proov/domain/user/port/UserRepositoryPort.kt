package proov.domain.user.port

import proov.domain.user.User

interface UserRepositoryPort {
    fun create(user: User): User

    fun findById(userId: String): User?

    fun existsById(userId: String): Boolean

    fun existsByNicknameKey(nicknameKey: String): Boolean
}
