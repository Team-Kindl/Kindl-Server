package kindl.domain.user

import kindl.global.auth.oauth.OauthProvider
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userRegistrationService: UserRegistrationService,
) {
    @Transactional(readOnly = true)
    fun findByOauthIdentity(provider: OauthProvider, providerId: String): User? =
        userRepository.findByProviderAndProviderId(provider, providerId)

    fun findOrRegister(
        provider: OauthProvider,
        providerId: String,
        email: String?,
        nickname: String,
    ): User {
        findByOauthIdentity(provider, providerId)?.let { return it }

        return try {
            userRegistrationService.registerOrReactivate(provider, providerId, email, nickname)
        } catch (dataIntegrityViolationException: DataIntegrityViolationException) {
            // 다른 요청이 같은 소셜 계정을 먼저 생성한 경우 기존 유저를 반환한다.
            findByOauthIdentity(provider, providerId) ?: throw dataIntegrityViolationException
        }
    }
}
