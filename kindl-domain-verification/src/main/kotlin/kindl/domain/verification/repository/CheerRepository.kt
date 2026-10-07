package kindl.domain.verification.repository

import kindl.domain.verification.entity.Cheer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface CheerRepository : JpaRepository<Cheer, Long> {

    /** 지워지는 인증이 받은 응원 */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        """
        update Cheer c set c.deletedAt = :now, c.updatedAt = :now
        where c.verificationId in :verificationIds and c.deletedAt is null
        """,
    )
    fun softDeleteAllByVerificationIds(verificationIds: Collection<String>, now: Instant): Int

    /** 나가는 사람이 이 모임에서 보낸 응원 */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        """
        update Cheer c set c.deletedAt = :now, c.updatedAt = :now
        where c.senderUserId = :userId and c.deletedAt is null
          and c.verificationId in (select v.id from Verification v where v.roomId = :roomId)
        """,
    )
    fun softDeleteAllSentInRoom(roomId: String, userId: String, now: Instant): Int

    @Query(
        """
        select count(c) from Cheer c
        where c.receiverUserId = :userId
          and c.verificationId in (select v.id from Verification v where v.roomId = :roomId)
        """,
    )
    fun countReceivedInRoom(roomId: String, userId: String): Long
}
