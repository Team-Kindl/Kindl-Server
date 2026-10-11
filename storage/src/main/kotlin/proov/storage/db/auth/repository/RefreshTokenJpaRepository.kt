package proov.storage.db.auth.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import proov.storage.db.auth.entity.RefreshTokenEntity
import java.time.Instant

interface RefreshTokenJpaRepository : JpaRepository<RefreshTokenEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from RefreshTokenEntity t where t.tokenHash = :tokenHash")
    fun findByTokenHashForUpdate(tokenHash: String): RefreshTokenEntity?

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        """
        update RefreshTokenEntity t set t.revokedAt = :now, t.updatedAt = :now
        where t.familyId = :familyId and t.revokedAt is null
        """,
    )
    fun revokeFamily(familyId: Long, now: Instant): Int
}
