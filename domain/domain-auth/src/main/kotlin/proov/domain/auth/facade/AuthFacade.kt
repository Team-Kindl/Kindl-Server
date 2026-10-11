package proov.domain.auth.facade

import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate
import proov.core.exception.ProovException
import proov.core.type.SocialProvider
import proov.domain.auth.AuthTokens
import proov.domain.auth.LoginResult
import proov.domain.auth.RefreshRotation
import proov.domain.auth.command.SignupCommand
import proov.domain.auth.exception.AuthFailure
import proov.domain.auth.port.AccessTokenIssuerPort
import proov.domain.auth.port.SignupTokenPort
import proov.domain.auth.service.RefreshTokenService
import proov.domain.auth.service.SocialAccountService
import proov.domain.auth.service.SocialTokenVerifiers
import proov.domain.user.command.DeviceCommand
import proov.domain.user.port.AuthUserPort
import java.time.Clock
import java.time.Instant

@Component
class AuthFacade(
    private val socialTokenVerifiers: SocialTokenVerifiers,
    private val socialAccountService: SocialAccountService,
    private val refreshTokenService: RefreshTokenService,
    private val accessTokenIssuerPort: AccessTokenIssuerPort,
    private val signupTokenPort: SignupTokenPort,
    private val authUserPort: AuthUserPort,
    private val transactionTemplate: TransactionTemplate,
    private val clock: Clock,
) {
    fun login(provider: SocialProvider, idToken: String, device: DeviceCommand): LoginResult {
        val identity = socialTokenVerifiers.verify(provider, idToken)
        val now = clock.instant()
        return transactionTemplate.execute {
            val account = socialAccountService.findByIdentity(identity)
                ?: return@execute LoginResult.SignupRequired(signupTokenPort.issue(identity, now))
            LoginResult.SignedIn(issueTokens(account.userId, device, now))
        }!!
    }

    fun signup(command: SignupCommand): AuthTokens {
        val identity = signupTokenPort.decode(command.signupToken)
        val now = clock.instant()
        return transactionTemplate.execute {
            if (socialAccountService.findByIdentity(identity) != null) throw ProovException(AuthFailure.ALREADY_SIGNED_UP)
            val user = authUserPort.createUser(command.user, now)
            socialAccountService.link(user.id, identity)
            issueTokens(user.id, command.device, now)
        }!!
    }

    fun refresh(refreshToken: String): AuthTokens {
        val now = clock.instant()
        return when (val rotation = refreshTokenService.rotate(refreshToken, now)) {
            is RefreshRotation.Rotated -> {
                authUserPort.requireExists(rotation.userId)
                AuthTokens(accessTokenIssuerPort.issue(rotation.userId, now), rotation.issued)
            }
            RefreshRotation.Invalid -> throw ProovException(AuthFailure.REFRESH_INVALID)
            RefreshRotation.Race -> throw ProovException(AuthFailure.REFRESH_RACE)
            RefreshRotation.Reused -> throw ProovException(AuthFailure.REFRESH_REUSED)
        }
    }

    fun logout(userId: String, refreshToken: String) {
        refreshTokenService.revokeFamily(refreshToken, userId, clock.instant())
    }

    private fun issueTokens(userId: String, device: DeviceCommand, now: Instant): AuthTokens {
        val deviceId = authUserPort.syncDevice(userId, device, now).id
        return AuthTokens(
            access = accessTokenIssuerPort.issue(userId, now),
            refresh = refreshTokenService.issue(userId, deviceId, now),
        )
    }
}
