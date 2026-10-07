package kindl.domain.verification.repository

import kindl.domain.verification.entity.VerificationItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface VerificationItemRepository : JpaRepository<VerificationItem, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        """
        update VerificationItem i set i.deletedAt = :now, i.updatedAt = :now
        where i.verificationId in :verificationIds and i.deletedAt is null
        """,
    )
    fun softDeleteAllByVerificationIds(verificationIds: Collection<String>, now: Instant): Int
}
