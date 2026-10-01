package kindl.support.outbox

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import kindl.support.entity.BaseTimeEntity
import kindl.support.id.TsidId
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant

/**
 * 같은 트랜잭션이 필요 없는 후속 작업(소셜 연결 해제 등)을 커밋과 함께 기록한다.
 * 처리 끝난 행은 7일 뒤 물리 삭제.
 */
@Entity
@Table(name = "outbox_events")
class OutboxEvent private constructor(
    aggregateType: String,
    aggregateId: String,
    eventType: String,
    payload: String,
    nextAttemptAt: Instant,
) : BaseTimeEntity() {

    @Id @TsidId
    var id: Long? = null
        protected set

    @Column(nullable = false, length = 30, updatable = false)
    val aggregateType: String = aggregateType

    @Column(nullable = false, length = 13, updatable = false)
    val aggregateId: String = aggregateId

    @Column(nullable = false, length = 40, updatable = false)
    val eventType: String = eventType

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, updatable = false)
    val payload: String = payload

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: OutboxStatus = OutboxStatus.PENDING
        protected set

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(nullable = false)
    var attempts: Int = 0
        protected set

    @Column(nullable = false)
    var nextAttemptAt: Instant = nextAttemptAt
        protected set

    @Column(length = 500)
    var lastError: String? = null
        protected set

    companion object {
        fun of(aggregateType: String, aggregateId: String, eventType: String, payload: String, now: Instant) =
            OutboxEvent(aggregateType, aggregateId, eventType, payload, nextAttemptAt = now)
    }
}
