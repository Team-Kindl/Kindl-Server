package proov.storage.db.user.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import proov.domain.user.Nickname
import proov.domain.user.User
import proov.storage.db.common.BaseEntity
import proov.storage.db.common.LocaleConverter
import proov.storage.db.common.ZoneIdConverter
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

@Entity
@Table(name = "users", uniqueConstraints = [UniqueConstraint(columnNames = ["nickname_key"])])
class UserEntity(
    id: String,
    nickname: String,
    nicknameKey: String,
    timezone: ZoneId,
    locale: Locale,
    termsVersion: String,
    termsAgreedAt: Instant,
) : BaseEntity<String>() {

    @Id
    @Column(length = 13)
    private val id: String = id

    @Column(nullable = false, length = 60)
    var nickname: String = nickname
        protected set

    @Column(nullable = false, length = 60)
    var nicknameKey: String = nicknameKey
        protected set

    @Convert(converter = ZoneIdConverter::class)
    @Column(nullable = false, length = 40)
    var timezone: ZoneId = timezone
        protected set

    @Convert(converter = LocaleConverter::class)
    @Column(nullable = false, length = 35)
    var locale: Locale = locale
        protected set

    @Column(nullable = false, length = 20)
    var termsVersion: String = termsVersion
        protected set

    @Column(nullable = false)
    var termsAgreedAt: Instant = termsAgreedAt
        protected set

    override fun getId(): String = id

    fun toDomain() = User(
        id = id,
        nickname = Nickname.restore(nickname),
        timezone = timezone,
        locale = locale,
        termsVersion = termsVersion,
        termsAgreedAt = termsAgreedAt,
    )

    companion object {
        fun from(user: User) = UserEntity(
            id = user.id,
            nickname = user.nickname.value,
            nicknameKey = user.nickname.key,
            timezone = user.timezone,
            locale = user.locale,
            termsVersion = user.termsVersion,
            termsAgreedAt = user.termsAgreedAt,
        )
    }
}
