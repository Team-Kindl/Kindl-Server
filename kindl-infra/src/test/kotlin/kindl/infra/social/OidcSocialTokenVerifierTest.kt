package kindl.infra.social

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kindl.core.error.KindlException
import kindl.core.type.SocialProvider
import kindl.domain.auth.error.AuthError
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtException
import java.io.IOException

class OidcSocialTokenVerifierTest : DescribeSpec({

    val decoder = mockk<JwtDecoder>()
    val verifier = OidcSocialTokenVerifier(SocialProvider.KAKAO, decoder)

    fun idToken(subject: String?, email: String? = null): Jwt = Jwt.withTokenValue("id-token")
        .header("alg", "RS256")
        .apply { subject?.let(::subject) }
        .apply { email?.let { claim("email", it) } }
        .claim("iss", "https://kauth.kakao.com")
        .build()

    describe("OIDC ID 토큰 검증") {
        it("sub를 providerUserId로 쓴다") {
            every { decoder.decode("ok") } returns idToken("12345", "a@b.com")

            val identity = verifier.verify("ok")

            identity.provider shouldBe SocialProvider.KAKAO
            identity.providerUserId shouldBe "12345"
            identity.email shouldBe "a@b.com"
        }

        it("sub가 없으면 SOCIAL_TOKEN_INVALID") {
            every { decoder.decode("no-sub") } returns idToken(subject = null)

            shouldThrow<KindlException> { verifier.verify("no-sub") }.errorCode shouldBe AuthError.SOCIAL_TOKEN_INVALID
        }

        it("서명·클레임 검증 실패는 SOCIAL_TOKEN_INVALID") {
            every { decoder.decode("bad") } throws BadJwtException("bad signature")

            shouldThrow<KindlException> { verifier.verify("bad") }.errorCode shouldBe AuthError.SOCIAL_TOKEN_INVALID
        }

        it("공개키를 못 받아 오면 제공자 장애로 SOCIAL_PROVIDER_UNAVAILABLE") {
            every { decoder.decode("down") } throws JwtException("Couldn't retrieve remote JWK set", IOException("timeout"))

            shouldThrow<KindlException> { verifier.verify("down") }.errorCode shouldBe AuthError.SOCIAL_PROVIDER_UNAVAILABLE
        }

        it("client ID가 설정되지 않은 제공자는 SOCIAL_PROVIDER_UNSUPPORTED") {
            val disabled = OidcSocialTokenVerifier(SocialProvider.APPLE, decoder = null)

            shouldThrow<KindlException> { disabled.verify("any") }.errorCode shouldBe AuthError.SOCIAL_PROVIDER_UNSUPPORTED
        }
    }
})
