package kindl.domain.user.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kindl.domain.user.entity.User
import kindl.domain.user.entity.OauthProvider
import kindl.domain.user.repository.UserRepository
import org.springframework.dao.DataIntegrityViolationException

class UserServiceTest : DescribeSpec({

    val userRepository = mockk<UserRepository>()
    val userRegistrationService = mockk<UserRegistrationService>()
    val userService = UserService(userRepository, userRegistrationService)

    describe("find") {
        it("provider+providerId로 유저를 조회한다") {
            val foundUser = User.create(OauthProvider.GOOGLE, "sub-1", "a@b.com", "nick")
            every { userRepository.findByProviderAndProviderId(OauthProvider.GOOGLE, "sub-1") } returns foundUser

            userService.findByOauthIdentity(OauthProvider.GOOGLE, "sub-1") shouldBe foundUser
        }

        it("없으면 null을 반환한다") {
            every { userRepository.findByProviderAndProviderId(OauthProvider.GOOGLE, "x") } returns null

            userService.findByOauthIdentity(OauthProvider.GOOGLE, "x").shouldBeNull()
        }
    }

    describe("findOrRegister") {
        it("기존 유저가 있으면 새로 생성하지 않는다") {
            val existingUser = User.create(OauthProvider.GOOGLE, "sub-2", "a@b.com", "기존")
            every { userRepository.findByProviderAndProviderId(OauthProvider.GOOGLE, "sub-2") } returns existingUser

            userService.findOrRegister(OauthProvider.GOOGLE, "sub-2", "a@b.com", "신규") shouldBe existingUser

            verify(exactly = 0) { userRegistrationService.registerOrReactivate(any(), any(), any(), any()) }
        }

        it("유저가 없으면 새로 생성한다") {
            val newlyRegisteredUser = User.create(OauthProvider.GOOGLE, "sub-3", "a@b.com", "닉네임")
            every { userRepository.findByProviderAndProviderId(OauthProvider.GOOGLE, "sub-3") } returns null
            every {
                userRegistrationService.registerOrReactivate(OauthProvider.GOOGLE, "sub-3", "a@b.com", "닉네임")
            } returns newlyRegisteredUser

            userService.findOrRegister(OauthProvider.GOOGLE, "sub-3", "a@b.com", "닉네임") shouldBe newlyRegisteredUser
        }

        it("동시 생성으로 유니크 충돌이 나면 먼저 생성된 유저를 반환한다") {
            val concurrentlyRegisteredUser = User.create(OauthProvider.GOOGLE, "sub-4", "a@b.com", "선행")
            every { userRepository.findByProviderAndProviderId(OauthProvider.GOOGLE, "sub-4") } returnsMany
                listOf(null, concurrentlyRegisteredUser)
            every {
                userRegistrationService.registerOrReactivate(OauthProvider.GOOGLE, "sub-4", "a@b.com", "후행")
            } throws DataIntegrityViolationException("duplicate")

            userService.findOrRegister(OauthProvider.GOOGLE, "sub-4", "a@b.com", "후행") shouldBe concurrentlyRegisteredUser
        }
    }
})
