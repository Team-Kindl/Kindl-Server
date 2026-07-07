package kindl.domain.user.service

import kindl.domain.user.entity.User
import kindl.domain.user.entity.OauthProvider
import kindl.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class UserRegistrationService(
    private val userRepository: UserRepository,
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun registerOrReactivate(
        provider: OauthProvider,
        providerId: String,
        email: String?,
        nickname: String,
    ): User {
        val normalizedNickname = User.normalizeNickname(nickname)
        val reactivatedUserCount = userRepository.reactivateDeletedUser(
            provider = provider.name,
            providerId = providerId,
            nickname = normalizedNickname,
            email = email,
        )
        if (reactivatedUserCount > 0) {
            return checkNotNull(userRepository.findByProviderAndProviderId(provider, providerId)) {
                "재활성화된 사용자를 조회할 수 없습니다."
            }
        }

        return userRepository.saveAndFlush(
            User.create(provider, providerId, email, normalizedNickname),
        )
    }
}
