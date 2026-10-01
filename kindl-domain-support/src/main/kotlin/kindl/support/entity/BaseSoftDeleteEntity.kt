package kindl.support.entity

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import java.time.Instant

/**
 * 사용자 행동으로 지워지고, 지운 뒤에도 기록이 필요한 도메인 테이블용.
 * 엔티티에는 @SQLRestriction("deleted_at IS NULL")을 함께 단다. 네이티브 쿼리에는 조건을 직접 넣는다.
 */
@MappedSuperclass
abstract class BaseSoftDeleteEntity : BaseTimeEntity() {

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null
        protected set

    val isDeleted: Boolean get() = deletedAt != null

    fun softDelete(now: Instant) {
        // 두 번 불려도 처음 시각을 유지한다
        if (deletedAt == null) deletedAt = now
    }
}
