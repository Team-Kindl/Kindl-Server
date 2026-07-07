package kindl.domain.user.entity

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kindl.global.exception.CustomException
import kindl.global.exception.ErrorCode

class OauthProviderTest : DescribeSpec({

    describe("OauthProvider.from") {
        it("대소문자 무관하게 enum으로 매핑한다") {
            OauthProvider.from("google") shouldBe OauthProvider.GOOGLE
            OauthProvider.from("GOOGLE") shouldBe OauthProvider.GOOGLE
            OauthProvider.from("Kakao") shouldBe OauthProvider.KAKAO
        }

        it("지원하지 않는 값이면 UNSUPPORTED_OAUTH_PROVIDER 예외") {
            val customException = shouldThrow<CustomException> { OauthProvider.from("naver") }
            customException.errorCode shouldBe ErrorCode.UNSUPPORTED_OAUTH_PROVIDER
        }
    }
})
