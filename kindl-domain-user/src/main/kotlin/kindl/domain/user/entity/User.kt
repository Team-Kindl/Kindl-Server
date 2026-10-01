package kindl.domain.user.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import kindl.domain.user.vo.Nickname
import kindl.support.converter.LocaleConverter
import kindl.support.converter.ZoneIdConverter
import kindl.support.entity.BaseSoftDeleteEntity
import kindl.support.id.TsidId
import org.hibernate.annotations.SQLRestriction
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

@Entity
@Table(name = "users")
@SQLRestriction("deleted_at IS NULL")
class User private constructor(
    nickname: Nickname,
    timezone: ZoneId,
    locale: Locale,
    termsVersion: String,
    termsAgreedAt: Instant,
) : BaseSoftDeleteEntity() {

    @Id @TsidId
    @Column(length = 13, updatable = false)
    var id: String? = null
        protected set

    // 표시용 NFC. 규칙은 그래핌 15개, 컬럼 길이는 상한 안전망
    @Column(nullable = false, length = 60)
    var nickname: String = nickname.value
        protected set

    // 중복 판정용 NFKC + 소문자
    @Column(nullable = false, length = 60)
    var nicknameKey: String = nickname.key
        protected set

    // 2단계
    @Column(length = 13)
    var profileImageFileId: String? = null
        protected set

    @Convert(converter = ZoneIdConverter::class)
    @Column(nullable = false, length = 40)
    var timezone: ZoneId = timezone
        protected set

    @Convert(converter = ZoneIdConverter::class)
    @Column(length = 40)
    var nextTimezone: ZoneId? = null
        protected set

    // 이 날짜부터 nextTimezone이 timezone으로 승격된다
    var nextTimezoneFrom: LocalDate? = null
        protected set

    // 서버발 문구(푸시)용
    @Convert(converter = LocaleConverter::class)
    @Column(nullable = false, length = 35)
    var locale: Locale = locale
        protected set

    @Column(nullable = false)
    var currentStreak: Int = 0
        protected set

    @Column(nullable = false)
    var bestStreak: Int = 0
        protected set

    var lastStreakDate: LocalDate? = null
        protected set

    var onboardedAt: Instant? = null
        protected set

    @Column(nullable = false, length = 20)
    var termsVersion: String = termsVersion
        protected set

    @Column(nullable = false)
    var termsAgreedAt: Instant = termsAgreedAt
        protected set

    fun changeNickname(nickname: Nickname) {
        this.nickname = nickname.value
        this.nicknameKey = nickname.key
    }

    fun changeLocale(locale: Locale) {
        this.locale = locale
    }

    companion object {
        fun create(
            nickname: Nickname,
            timezone: ZoneId,
            locale: Locale,
            termsVersion: String,
            now: Instant,
        ): User = User(
            nickname = nickname,
            timezone = timezone,
            locale = locale,
            termsVersion = termsVersion,
            termsAgreedAt = now,
        )
    }
}
