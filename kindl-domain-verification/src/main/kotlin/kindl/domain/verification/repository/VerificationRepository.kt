package kindl.domain.verification.repository

import kindl.domain.verification.entity.Verification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface VerificationRepository : JpaRepository<Verification, String> {

    @Query("select v.id from Verification v where v.promiseId in :promiseIds")
    fun findIdsByPromiseIds(promiseIds: Collection<String>): List<String>

    fun countByRoomIdAndUserId(roomId: String, userId: String): Long

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Verification v set v.deletedAt = :now, v.updatedAt = :now where v.id in :ids and v.deletedAt is null")
    fun softDeleteAllByIds(ids: Collection<String>, now: Instant): Int
}
