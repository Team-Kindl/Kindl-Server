package kindl.global.auth.oauth

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import org.springframework.security.oauth2.jwt.Jwt

class OidcAudienceValidatorTest : DescribeSpec({

    val audienceValidator = OidcAudienceValidator(setOf("trusted-client"))

    fun idToken(
        audiences: List<String>,
        authorizedParty: Any? = null,
    ): Jwt = Jwt.withTokenValue("id-token")
        .header("alg", "RS256")
        .subject("provider-user-id")
        .audience(audiences)
        .apply {
            if (authorizedParty != null) {
                claim("azp", authorizedParty)
            }
        }
        .build()

    describe("OIDC audience 검증") {
        it("단일 audience가 허용 목록에 있으면 성공한다") {
            audienceValidator.validate(idToken(listOf("trusted-client"))).hasErrors().shouldBeFalse()
        }

        it("허용된 audience가 없으면 실패한다") {
            audienceValidator.validate(idToken(listOf("other-client"))).hasErrors().shouldBeTrue()
        }

        it("여러 audience에는 허용된 azp가 필요하다") {
            audienceValidator.validate(
                idToken(listOf("trusted-client", "other-client")),
            ).hasErrors().shouldBeTrue()

            audienceValidator.validate(
                idToken(listOf("trusted-client", "other-client"), "trusted-client"),
            ).hasErrors().shouldBeFalse()
        }

        it("azp가 있으면 허용 목록의 클라이언트여야 한다") {
            audienceValidator.validate(
                idToken(listOf("trusted-client"), "other-client"),
            ).hasErrors().shouldBeTrue()
        }

        it("azp claim이 문자열이 아니면 실패한다") {
            audienceValidator.validate(
                idToken(listOf("trusted-client"), listOf("trusted-client")),
            ).hasErrors().shouldBeTrue()
        }
    }
})
