package proov.storage.db.auth.adapter

import org.springframework.stereotype.Repository
import proov.domain.auth.RefreshToken
import proov.domain.auth.port.RefreshTokenRepositoryPort
import proov.storage.db.auth.entity.RefreshTokenEntity
import proov.storage.db.auth.repository.RefreshTokenJpaRepository
import java.time.Instant

@Repository
class RefreshTokenRepositoryAdapter(
    private val refreshTokenJpaRepository: RefreshTokenJpaRepository,
) : RefreshTokenRepositoryPort {

    override fun create(token: RefreshToken): RefreshToken =
        refreshTokenJpaRepository.save(RefreshTokenEntity.from(token)).toDomain()

    override fun update(token: RefreshToken): RefreshToken =
        refreshTokenJpaRepository.getReferenceById(token.id).apply { update(token) }.toDomain()

    override fun findByTokenHashForUpdate(tokenHash: String): RefreshToken? =
        refreshTokenJpaRepository.findByTokenHashForUpdate(tokenHash)?.toDomain()

    override fun revokeFamily(familyId: Long, now: Instant) {
        refreshTokenJpaRepository.revokeFamily(familyId, now)
    }
}
