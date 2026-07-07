package kindl.domain.user.repository

import kindl.domain.user.entity.User
import kindl.domain.user.entity.OauthProvider
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserRepository : JpaRepository<User, String> {
    fun findByProviderAndProviderId(provider: OauthProvider, providerId: String): User?

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value = """
            update users
            set deleted_at = null,
                nickname = :nickname,
                email = :email,
                updated_at = CURRENT_TIMESTAMP(6)
            where provider = :provider
              and provider_id = :providerId
              and deleted_at is not null
        """,
        nativeQuery = true,
    )
    fun reactivateDeletedUser(
        @Param("provider") provider: String,
        @Param("providerId") providerId: String,
        @Param("nickname") nickname: String,
        @Param("email") email: String?,
    ): Int
}
