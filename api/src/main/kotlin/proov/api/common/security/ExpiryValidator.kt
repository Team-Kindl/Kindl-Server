package proov.api.common.security

import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt
import java.time.Clock
import java.time.Duration

internal class ExpiryValidator(
    private val clock: Clock,
    private val skew: Duration,
) : OAuth2TokenValidator<Jwt> {

    override fun validate(token: Jwt): OAuth2TokenValidatorResult {
        val now = clock.instant()
        val expiresAt = token.expiresAt
            ?: return OAuth2TokenValidatorResult.failure(OAuth2Error("invalid_token", "exp 없음", null))
        if (now.minus(skew).isAfter(expiresAt)) {
            return OAuth2TokenValidatorResult.failure(OAuth2Error(TokenClaims.EXPIRED_ERROR_CODE, "만료된 토큰", null))
        }
        val issuedAt = token.issuedAt
        if (issuedAt != null && now.plus(skew).isBefore(issuedAt)) {
            return OAuth2TokenValidatorResult.failure(OAuth2Error("invalid_token", "아직 유효하지 않은 토큰", null))
        }
        return OAuth2TokenValidatorResult.success()
    }
}
