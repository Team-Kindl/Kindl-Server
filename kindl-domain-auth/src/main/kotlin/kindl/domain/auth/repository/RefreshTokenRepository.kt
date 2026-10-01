package kindl.domain.auth.repository

import jakarta.persistence.LockModeType
import kindl.domain.auth.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {

    // 같은 토큰으로 동시에 들어온 회전 요청을 한 줄로 세운다
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from RefreshToken t where t.tokenHash = :tokenHash")
    fun findForUpdate(tokenHash: String): RefreshToken?

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update RefreshToken t set t.revokedAt = :now where t.familyId = :familyId and t.revokedAt is null")
    fun revokeFamily(familyId: Long, now: Instant): Int
}
