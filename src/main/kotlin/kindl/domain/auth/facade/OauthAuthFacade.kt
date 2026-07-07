package kindl.domain.auth.facade

import kindl.domain.auth.model.OauthLoginResult
import kindl.domain.auth.model.RegistrationInfo
import kindl.domain.auth.repository.RegistrationStore
import kindl.domain.auth.service.OidcTokenVerifier
import kindl.domain.user.entity.User
import kindl.domain.user.entity.OauthProvider
import kindl.domain.user.service.UserService
import kindl.global.auth.jwt.dto.response.TokenResponse
import kindl.global.auth.security.JwtProvider
import kindl.global.exception.CustomException
import kindl.global.exception.ErrorCode
import org.springframework.stereotype.Service

@Service
class OauthAuthFacade(
    private val oidcTokenVerifier: OidcTokenVerifier,
    private val userService: UserService,
    private val registrationStore: RegistrationStore,
    private val jwtProvider: JwtProvider,
) {
    fun login(provider: OauthProvider, idToken: String): OauthLoginResult {
        val oauthUserInfo = oidcTokenVerifier.verify(provider, idToken)

        val existingUser = userService.findByOauthIdentity(provider, oauthUserInfo.providerId)
        return if (existingUser != null) {
            OauthLoginResult.loggedIn(issueTokens(existingUser))
        } else {
            // 미가입: 검증된 신원만 임시 보관하고 회원가입으로 유도
            OauthLoginResult.needsSignup(
                registrationStore.save(RegistrationInfo.from(oauthUserInfo)),
            )
        }
    }

    fun signup(registrationKey: String, nickname: String): TokenResponse {
        val registrationInfo = registrationStore.consume(registrationKey)
            ?: throw CustomException(ErrorCode.REGISTRATION_NOT_FOUND)

        val registeredUser = userService.findOrRegister(
            registrationInfo.provider,
            registrationInfo.providerId,
            registrationInfo.email,
            nickname,
        )
        return issueTokens(registeredUser)
    }

    private fun issueTokens(authenticatedUser: User): TokenResponse =
        jwtProvider.issueToken(
            userId = authenticatedUser.requireId(),
            userRoles = listOf(authenticatedUser.role.name),
        )
}
