package kindl.support.entity

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.LastModifiedDate
import java.time.Instant

/**
 * 계속 갱신되지만 지울 일은 없는 테이블용 (promise_daily_records).
 */
@MappedSuperclass
abstract class BaseTimeEntity : BaseCreatedEntity() {

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: Instant
        protected set
}
