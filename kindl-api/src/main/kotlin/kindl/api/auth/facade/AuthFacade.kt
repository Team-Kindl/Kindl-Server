package kindl.api.auth.facade

import java.time.Clock
import java.time.Instant
import kindl.api.auth.dto.command.SignupCommand
import kindl.api.auth.dto.result.AuthTokens
import kindl.api.auth.dto.result.LoginResult
import kindl.api.security.AccessTokenIssuer
import kindl.api.security.SignupTokenCodec
import kindl.core.error.KindlException
import kindl.core.type.SocialProvider
import kindl.domain.auth.dto.result.RefreshRotation
import kindl.domain.auth.error.AuthError
import kindl.domain.auth.service.RefreshTokenService
import kindl.domain.auth.service.SocialAccountCommandService
import kindl.domain.auth.service.SocialAccountQueryService
import kindl.domain.auth.service.SocialTokenVerifiers
import kindl.domain.user.dto.command.DeviceCommand
import kindl.domain.user.service.DeviceService
import kindl.domain.user.service.UserCommandService
import kindl.domain.user.service.UserQueryService
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

/**
 * 로그인·가입·토큰 회전을 엮는다 (auth + user 두 도메인).
 * 외부 호출(소셜 검증, 최대 3초)은 트랜잭션 밖에서 하고, DB 구간만 TransactionTemplate으로 감싼다.
 */
@Component
class AuthFacade(
    private val socialTokenVerifiers: SocialTokenVerifiers,
    private val socialAccountQueryService: SocialAccountQueryService,
    private val socialAccountCommandService: SocialAccountCommandService,
    private val refreshTokenService: RefreshTokenService,
    private val userQueryService: UserQueryService,
    private val userCommandService: UserCommandService,
    private val deviceService: DeviceService,
    private val accessTokenIssuer: AccessTokenIssuer,
    private val signupTokenCodec: SignupTokenCodec,
    private val tx: TransactionTemplate,
    private val clock: Clock,
) {
    fun login(provider: SocialProvider, idToken: String, device: DeviceCommand): LoginResult {
        val identity = socialTokenVerifiers.verify(provider, idToken)
        val now = clock.instant()
        return tx.execute {
            val account = socialAccountQueryService.findActive(identity)
                ?: return@execute LoginResult.SignupRequired(signupTokenCodec.issue(identity, now))
            LoginResult.SignedIn(issueTokens(account.userId, device, now))
        }!!
    }

    /** users · social_accounts · devices · refresh_tokens를 한 트랜잭션으로 만든다. */
    fun signup(command: SignupCommand): AuthTokens {
        val identity = signupTokenCodec.decode(command.signupToken)
        val now = clock.instant()
        return tx.execute {
            // 가입 버튼을 두 번 누른 경우. 앱은 로그인으로 다시 시도한다
            if (socialAccountQueryService.findActive(identity) != null) throw KindlException(AuthError.ALREADY_SIGNED_UP)
            val user = userCommandService.create(command.user, now)
            socialAccountCommandService.link(user.id, identity)
            issueTokens(user.id, command.device, now)
        }!!
    }

    /** 폐기 결과가 커밋돼야 하므로 바깥 트랜잭션 없이 부르고, 실패는 커밋 뒤에 던진다. */
    fun refresh(refreshToken: String): AuthTokens {
        val now = clock.instant()
        return when (val rotation = refreshTokenService.rotate(refreshToken, now)) {
            is RefreshRotation.Rotated -> {
                userQueryService.requireActive(rotation.userId)
                AuthTokens(accessTokenIssuer.issue(rotation.userId, now), rotation.issued)
            }
            RefreshRotation.Invalid -> throw KindlException(AuthError.REFRESH_INVALID)
            RefreshRotation.Race -> throw KindlException(AuthError.REFRESH_RACE)
            RefreshRotation.Reused -> throw KindlException(AuthError.REFRESH_REUSED)
        }
    }

    fun logout(userId: String, refreshToken: String) {
        refreshTokenService.revokeFamily(refreshToken, userId, clock.instant())
    }

    private fun issueTokens(userId: String, device: DeviceCommand, now: Instant): AuthTokens {
        val deviceId = deviceService.upsert(userId, device, now).id
        return AuthTokens(
            access = accessTokenIssuer.issue(userId, now),
            refresh = refreshTokenService.issue(userId, deviceId, now),
        )
    }
}
