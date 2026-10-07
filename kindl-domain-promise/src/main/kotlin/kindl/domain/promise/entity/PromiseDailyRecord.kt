package kindl.domain.promise.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import kindl.support.entity.BaseTimeEntity
import kindl.support.id.TsidId
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * "오늘 몇 번 했나", "이번 주 며칠 채웠나"를 한 줄 조회로 만드는 파생 테이블. soft delete 없음.
 * 승인 카운트는 approved_count < per_day_count 조건을 건 UPDATE로 올린다.
 */
@Entity
@Table(name = "promise_daily_records")
class PromiseDailyRecord private constructor(
    promiseId: String,
    userId: String,
    serviceDate: LocalDate,
) : BaseTimeEntity() {

    @Id @TsidId
    var id: Long? = null
        protected set

    @Column(nullable = false, length = 13, updatable = false)
    val promiseId: String = promiseId

    @Column(nullable = false, length = 13, updatable = false)
    val userId: String = userId

    // 유저 시간대 기준 그날
    @Column(nullable = false, updatable = false)
    val serviceDate: LocalDate = serviceDate

    // 그 주 월요일 (ISO 주)
    @Column(nullable = false, updatable = false)
    val weekStart: LocalDate = serviceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(nullable = false)
    var approvedCount: Int = 0
        protected set

    companion object {
        fun open(promiseId: String, userId: String, serviceDate: LocalDate) =
            PromiseDailyRecord(promiseId, userId, serviceDate)
    }
}
