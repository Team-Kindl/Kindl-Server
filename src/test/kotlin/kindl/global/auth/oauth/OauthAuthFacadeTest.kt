package kindl.global.auth.oauth

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kindl.domain.user.User
import kindl.domain.user.UserService
import kindl.global.auth.jwt.TokenDto
import kindl.global.auth.security.JwtProvider
import kindl.global.exception.CustomException
import kindl.global.exception.ErrorCode

class OauthAuthFacadeTest : DescribeSpec({

    val oidcTokenVerifier = mockk<OidcTokenVerifier>()
    val userService = mockk<UserService>()
    val registrationStore = mockk<RegistrationStore>()
    val jwtProvider = mockk<JwtProvider>()
    val oauthAuthFacade = OauthAuthFacade(oidcTokenVerifier, userService, registrationStore, jwtProvider)

    val issuedTokens = TokenDto.of("access", 1L, "refresh", 2L)

    beforeTest { clearAllMocks() }

    describe("login") {
        it("기존 회원이면 LoggedIn + 토큰을 발급한다") {
            val existingUser = User(
                provider = OauthProvider.GOOGLE,
                providerId = "sub-1",
                nickname = "nick",
                email = "a@b.com",
                id = "user-1",
            )
            every { oidcTokenVerifier.verify(OauthProvider.GOOGLE, "idtoken") } returns
                OauthUserInfo.of(OauthProvider.GOOGLE, "sub-1", "a@b.com")
            every { userService.findByOauthIdentity(OauthProvider.GOOGLE, "sub-1") } returns existingUser
            every { jwtProvider.issueToken("user-1", listOf("USER")) } returns issuedTokens

            oauthAuthFacade.login(OauthProvider.GOOGLE, "idtoken") shouldBe OauthLoginResult.loggedIn(issuedTokens)
        }

        it("미가입이면 신원을 저장하고 NeedsSignup을 반환한다(토큰 미발급)") {
            every { oidcTokenVerifier.verify(OauthProvider.GOOGLE, "idtoken") } returns
                OauthUserInfo.of(OauthProvider.GOOGLE, "sub-2", "b@c.com")
            every { userService.findByOauthIdentity(OauthProvider.GOOGLE, "sub-2") } returns null
            every {
                registrationStore.save(RegistrationInfo(OauthProvider.GOOGLE, "sub-2", "b@c.com"))
            } returns "reg-key"

            oauthAuthFacade.login(OauthProvider.GOOGLE, "idtoken") shouldBe OauthLoginResult.needsSignup("reg-key")
            verify(exactly = 0) { jwtProvider.issueToken(any(), any()) }
        }
    }

    describe("signup") {
        it("registrationKey를 소비해 유저를 멱등 생성하고 토큰을 발급한다") {
            val registeredUser = User(
                provider = OauthProvider.GOOGLE,
                providerId = "sub-3",
                nickname = "입력닉",
                email = "c@d.com",
                id = "user-3",
            )
            every { registrationStore.consume("reg-key") } returns
                RegistrationInfo(OauthProvider.GOOGLE, "sub-3", "c@d.com")
            every {
                userService.findOrRegister(OauthProvider.GOOGLE, "sub-3", "c@d.com", "입력닉")
            } returns registeredUser
            every { jwtProvider.issueToken("user-3", listOf("USER")) } returns issuedTokens

            oauthAuthFacade.signup("reg-key", "입력닉") shouldBe issuedTokens
            verify(exactly = 1) { registrationStore.consume("reg-key") }
        }

        it("registrationKey가 유효하지 않으면 REGISTRATION_NOT_FOUND") {
            every { registrationStore.consume("bad") } returns null

            shouldThrow<CustomException> { oauthAuthFacade.signup("bad", "닉") }
                .errorCode shouldBe ErrorCode.REGISTRATION_NOT_FOUND
        }
    }
})
