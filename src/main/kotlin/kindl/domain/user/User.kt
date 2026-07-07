package kindl.domain.user

import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import kindl.domain.common.entity.BaseEntity
import kindl.global.auth.oauth.OauthProvider
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction

@Entity
@Table(
    name = "users",
    comment = "유저 정보",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_provider_provider_id", columnNames = ["provider", "provider_id"]),
    ],
)
@SQLRestriction("deleted_at is null")
@SQLDelete(sql = "update users set deleted_at = CURRENT_TIMESTAMP(6) where id = ?")
class User(
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, comment = "소셜 로그인 제공자")
    val provider: OauthProvider,

    @Column(name = "provider_id", nullable = false, comment = "제공자 측 유저 ID (OIDC sub)")
    val providerId: String,

    @Column(nullable = false, length = MAX_NICKNAME_LENGTH, comment = "닉네임")
    var nickname: String,

    @Column(length = 100, comment = "이메일")
    val email: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, comment = "권한")
    val role: UserRole = UserRole.USER,

    @Id
    @Tsid
    @Column(length = 13, comment = "유저 ID")
    val id: String? = null,
) : BaseEntity() {

    fun updateNickname(newNickname: String) {
        nickname = normalizeNickname(newNickname)
    }

    fun requireId(): String = checkNotNull(id) { "저장되지 않은 사용자는 ID가 없습니다." }

    companion object {
        fun create(
            provider: OauthProvider,
            providerId: String,
            email: String?,
            nickname: String,
        ): User = User(
            provider = provider,
            providerId = providerId,
            email = email,
            nickname = normalizeNickname(nickname),
        )

        fun normalizeNickname(nickname: String): String = nickname.trim().also { normalizedNickname ->
            require(normalizedNickname.isNotBlank()) { "닉네임은 공백일 수 없습니다." }
            require(normalizedNickname.length <= MAX_NICKNAME_LENGTH) {
                "닉네임은 ${MAX_NICKNAME_LENGTH}자를 초과할 수 없습니다."
            }
        }

        const val MAX_NICKNAME_LENGTH = 50
    }
}
