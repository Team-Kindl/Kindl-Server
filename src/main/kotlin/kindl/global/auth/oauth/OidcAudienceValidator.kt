package kindl.global.auth.oauth

import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2ErrorCodes
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt

internal class OidcAudienceValidator(
    allowedAudiences: Collection<String>,
) : OAuth2TokenValidator<Jwt> {

    private val allowedAudiences = allowedAudiences.toSet()

    init {
        require(this.allowedAudiences.isNotEmpty()) { "OIDC audience는 하나 이상이어야 합니다." }
    }

    override fun validate(token: Jwt): OAuth2TokenValidatorResult {
        if (token.audience.none { tokenAudience -> tokenAudience in allowedAudiences }) {
            return failure("ID 토큰의 audience가 허용되지 않았습니다.")
        }

        val authorizedPartyClaim = token.claims[AUTHORIZED_PARTY_CLAIM]
        if (authorizedPartyClaim != null && authorizedPartyClaim !is String) {
            return failure("ID 토큰의 azp 형식이 올바르지 않습니다.")
        }

        val authorizedParty = authorizedPartyClaim
        if (token.audience.size > 1 && authorizedParty == null) {
            return failure("여러 audience를 가진 ID 토큰에는 azp가 필요합니다.")
        }
        if (authorizedParty != null && authorizedParty !in allowedAudiences) {
            return failure("ID 토큰의 azp가 허용되지 않았습니다.")
        }

        return OAuth2TokenValidatorResult.success()
    }

    private fun failure(description: String): OAuth2TokenValidatorResult =
        OAuth2TokenValidatorResult.failure(
            OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN, description, null),
        )

    companion object {
        private const val AUTHORIZED_PARTY_CLAIM = "azp"
    }
}
