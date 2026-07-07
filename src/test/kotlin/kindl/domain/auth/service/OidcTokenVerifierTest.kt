package kindl.domain.auth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kindl.domain.auth.model.OauthUserInfo
import kindl.domain.user.entity.OauthProvider
import kindl.global.exception.CustomException
import kindl.global.exception.ErrorCode
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder

class OidcTokenVerifierTest : DescribeSpec({

    val googleJwtDecoder = mockk<JwtDecoder>()
    val kakaoJwtDecoder = mockk<JwtDecoder>()
    val oidcTokenVerifier = OidcTokenVerifier(
        mapOf(
            OauthProvider.GOOGLE to googleJwtDecoder,
            OauthProvider.KAKAO to kakaoJwtDecoder,
        ),
    )

    describe("verify") {
        it("검증 성공시 claim을 OauthUserInfo로 매핑한다") {
            every { googleJwtDecoder.decode("valid") } returns
                Jwt.withTokenValue("valid")
                    .header("alg", "RS256")
                    .subject("sub-1")
                    .claim("email", "a@b.com")
                    .build()

            val oauthUserInfo = oidcTokenVerifier.verify(OauthProvider.GOOGLE, "valid")

            oauthUserInfo.provider shouldBe OauthProvider.GOOGLE
            oauthUserInfo.providerId shouldBe "sub-1"
            oauthUserInfo.email shouldBe "a@b.com"
        }

        it("카카오 ID 토큰 claim도 동일한 OauthUserInfo로 매핑한다") {
            every { kakaoJwtDecoder.decode("kakao-valid") } returns
                Jwt.withTokenValue("kakao-valid")
                    .header("alg", "RS256")
                    .subject("kakao-sub-1")
                    .claim("email", "kakao@kindl.test")
                    .build()

            val oauthUserInfo = oidcTokenVerifier.verify(OauthProvider.KAKAO, "kakao-valid")

            oauthUserInfo.provider shouldBe OauthProvider.KAKAO
            oauthUserInfo.providerId shouldBe "kakao-sub-1"
            oauthUserInfo.email shouldBe "kakao@kindl.test"
        }

        it("디코더가 없는 프로바이더는 UNSUPPORTED_OAUTH_PROVIDER") {
            shouldThrow<CustomException> { oidcTokenVerifier.verify(OauthProvider.APPLE, "token") }
                .errorCode shouldBe ErrorCode.UNSUPPORTED_OAUTH_PROVIDER
        }

        it("디코딩 실패(JwtException)는 INVALID_OAUTH_TOKEN") {
            every { googleJwtDecoder.decode("bad") } throws BadJwtException("invalid signature")

            shouldThrow<CustomException> { oidcTokenVerifier.verify(OauthProvider.GOOGLE, "bad") }
                .errorCode shouldBe ErrorCode.INVALID_OAUTH_TOKEN
        }

        it("sub claim이 공백이면 INVALID_OAUTH_TOKEN") {
            every { googleJwtDecoder.decode("blank-subject") } returns
                Jwt.withTokenValue("blank-subject")
                    .header("alg", "RS256")
                    .subject(" ")
                    .build()

            shouldThrow<CustomException> {
                oidcTokenVerifier.verify(OauthProvider.GOOGLE, "blank-subject")
            }.errorCode shouldBe ErrorCode.INVALID_OAUTH_TOKEN
        }
    }
})
