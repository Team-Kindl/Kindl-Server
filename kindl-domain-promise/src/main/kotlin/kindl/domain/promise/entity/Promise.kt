package kindl.domain.promise.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import kindl.core.error.KindlException
import kindl.core.type.VerificationMethod
import kindl.domain.promise.converter.VerificationMethodsConverter
import kindl.domain.promise.error.PromiseError
import kindl.support.entity.BaseSoftDeleteEntity
import kindl.support.id.TsidId
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.SqlTypes
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@Entity
@Table(name = "promises")
@SQLRestriction("deleted_at IS NULL")
class Promise private constructor(
    roomId: String,
    userId: String,
    title: String,
    cycleType: CycleType,
    perDayCount: Int,
    weekDaysCount: Int?,
    startDate: LocalDate,
    endDate: LocalDate,
    endsAt: Instant,
    methods: Set<VerificationMethod>,
    targetCount: Int,
) : BaseSoftDeleteEntity() {

    @Id @TsidId
    @Column(length = 13, updatable = false)
    var id: String? = null
        protected set

    @Column(nullable = false, length = 13, updatable = false)
    val roomId: String = roomId

    @Column(nullable = false, length = 13, updatable = false)
    val userId: String = userId

    // 규칙은 그래핌 4~30개
    @Column(nullable = false, length = 90)
    var title: String = title
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var cycleType: CycleType = cycleType
        protected set

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(nullable = false)
    var perDayCount: Int = perDayCount
        protected set

    @JdbcTypeCode(SqlTypes.TINYINT)
    var weekDaysCount: Int? = weekDaysCount
        protected set

    // 유저 시간대 기준. 주 단위는 보정된 시작일
    @Column(nullable = false)
    var startDate: LocalDate = startDate
        protected set

    // 포함
    @Column(nullable = false)
    var endDate: LocalDate = endDate
        protected set

    // endDate 다음 날 00:00(유저 시간대)의 UTC. 매시 종료 배치의 기준
    @Column(nullable = false)
    var endsAt: Instant = endsAt
        protected set

    @Convert(converter = VerificationMethodsConverter::class)
    @Column(nullable = false, length = 20)
    var methods: Set<VerificationMethod> = methods
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: PromiseStatus = PromiseStatus.ACTIVE
        protected set

    @Column(nullable = false)
    var targetCount: Int = targetCount
        protected set

    @Column(nullable = false)
    var approvedCount: Int = 0
        protected set

    @Column(nullable = false)
    var currentStreak: Int = 0
        protected set

    var lastStreakPeriod: LocalDate? = null
        protected set

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(nullable = false)
    var editCount: Int = 0
        protected set

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(nullable = false)
    var editLimit: Int = DEFAULT_EDIT_LIMIT
        protected set

    @Column(nullable = false)
    var editBonusGranted: Boolean = false
        protected set

    @Column(nullable = false)
    var alarmEnabled: Boolean = true
        protected set

    // 유저 시간대의 벽시계 시각
    @Column(nullable = false)
    var alarmTime: LocalTime = DEFAULT_ALARM_TIME
        protected set

    // 요일 비트마스크, 0이면 매일
    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(nullable = false)
    var alarmDays: Int = 0
        protected set

    // 4단계
    @Column(length = 13)
    var reviewId: String? = null
        protected set

    var stoppedAt: Instant? = null
        protected set

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    var stopReason: StopReason? = null
        protected set

    var completedAt: Instant? = null
        protected set

    fun stop(reason: StopReason, now: Instant) {
        requireActive()
        if (now < createdAt.plus(STOP_AVAILABLE_AFTER)) throw KindlException(PromiseError.STOP_TOO_EARLY)
        status = PromiseStatus.STOPPED
        stopReason = reason
        stoppedAt = now
    }

    fun complete(now: Instant) {
        requireActive()
        status = PromiseStatus.COMPLETED
        completedAt = now
    }

    private fun requireActive() {
        if (status != PromiseStatus.ACTIVE) throw KindlException(PromiseError.NOT_ACTIVE)
    }

    companion object {
        const val DEFAULT_EDIT_LIMIT = 3
        private val DEFAULT_ALARM_TIME: LocalTime = LocalTime.of(8, 0)
        private val STOP_AVAILABLE_AFTER: Duration = Duration.ofDays(7)

        /** 주기·기간 계산 규칙(CyclePolicy)은 공약 기능 PR에서 이 앞단에 붙는다. */
        fun create(
            roomId: String,
            userId: String,
            title: String,
            cycleType: CycleType,
            perDayCount: Int,
            weekDaysCount: Int?,
            startDate: LocalDate,
            endDate: LocalDate,
            methods: Set<VerificationMethod>,
            targetCount: Int,
            zone: ZoneId,
        ): Promise = Promise(
            roomId = roomId,
            userId = userId,
            title = title,
            cycleType = cycleType,
            perDayCount = perDayCount,
            weekDaysCount = weekDaysCount,
            startDate = startDate,
            endDate = endDate,
            endsAt = endDate.plusDays(1).atStartOfDay(zone).toInstant(),
            methods = methods,
            targetCount = targetCount,
        )
    }
}
