package kindl.domain.user

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kindl.global.auth.oauth.OauthProvider

class UserRegistrationServiceTest : DescribeSpec({

    val userRepository = mockk<UserRepository>()
    val userRegistrationService = UserRegistrationService(userRepository)

    beforeTest { clearAllMocks() }

    describe("registerOrReactivate") {
        it("탈퇴 사용자가 있으면 기존 행을 재활성화해 반환한다") {
            val reactivatedUser = User(
                provider = OauthProvider.GOOGLE,
                providerId = "provider-user-id",
                nickname = "재가입 닉네임",
                email = "new@example.com",
                id = "user-id",
            )
            every {
                userRepository.reactivateDeletedUser(
                    provider = "GOOGLE",
                    providerId = "provider-user-id",
                    nickname = "재가입 닉네임",
                    email = "new@example.com",
                )
            } returns 1
            every {
                userRepository.findByProviderAndProviderId(OauthProvider.GOOGLE, "provider-user-id")
            } returns reactivatedUser

            userRegistrationService.registerOrReactivate(
                OauthProvider.GOOGLE,
                "provider-user-id",
                "new@example.com",
                "  재가입 닉네임  ",
            ) shouldBe reactivatedUser

            verify(exactly = 0) { userRepository.saveAndFlush(any()) }
        }

        it("탈퇴 사용자가 없으면 정규화된 닉네임으로 새 사용자를 저장한다") {
            every {
                userRepository.reactivateDeletedUser(
                    provider = "GOOGLE",
                    providerId = "new-provider-user-id",
                    nickname = "신규 닉네임",
                    email = "new@example.com",
                )
            } returns 0
            every { userRepository.saveAndFlush(any()) } answers { firstArg() }

            val registeredUser = userRegistrationService.registerOrReactivate(
                OauthProvider.GOOGLE,
                "new-provider-user-id",
                "new@example.com",
                "  신규 닉네임  ",
            )

            registeredUser.nickname shouldBe "신규 닉네임"
            registeredUser.providerId shouldBe "new-provider-user-id"
        }
    }
})
