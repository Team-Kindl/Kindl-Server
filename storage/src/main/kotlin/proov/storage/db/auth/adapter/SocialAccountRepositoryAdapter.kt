package proov.storage.db.auth.adapter

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Repository
import proov.core.exception.ProovException
import proov.core.type.SocialProvider
import proov.domain.auth.SocialAccount
import proov.domain.auth.exception.AuthFailure
import proov.domain.auth.port.SocialAccountRepositoryPort
import proov.storage.db.auth.entity.SocialAccountEntity
import proov.storage.db.auth.repository.SocialAccountJpaRepository

@Repository
class SocialAccountRepositoryAdapter(
    private val socialAccountJpaRepository: SocialAccountJpaRepository,
) : SocialAccountRepositoryPort {

    override fun create(account: SocialAccount): SocialAccount = try {
        socialAccountJpaRepository.saveAndFlush(SocialAccountEntity.from(account)).toDomain()
    } catch (e: DataIntegrityViolationException) {
        throw ProovException(AuthFailure.ALREADY_SIGNED_UP, cause = e)
    }

    override fun findByProviderAndProviderUserId(provider: SocialProvider, providerUserId: String): SocialAccount? =
        socialAccountJpaRepository.findByProviderAndProviderUserId(provider, providerUserId)?.toDomain()
}
