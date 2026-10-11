package proov.storage.db.user.repository

import org.springframework.data.jpa.repository.JpaRepository
import proov.storage.db.user.entity.UserEntity

interface UserJpaRepository : JpaRepository<UserEntity, String> {
    fun existsByNicknameKey(nicknameKey: String): Boolean
}
