package proov.domain.auth.port

import proov.domain.auth.RefreshToken
import java.time.Instant

interface RefreshTokenRepositoryPort {
    fun create(token: RefreshToken): RefreshToken

    fun update(token: RefreshToken): RefreshToken

    fun findByTokenHashForUpdate(tokenHash: String): RefreshToken?

    fun revokeFamily(familyId: Long, now: Instant)
}
