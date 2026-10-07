package kindl.domain.user.repository

import kindl.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, String> {
    fun existsByNicknameKey(nicknameKey: String): Boolean
}
