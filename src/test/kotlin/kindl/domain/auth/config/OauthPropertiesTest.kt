package kindl.domain.auth.config

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.collections.shouldBeEmpty
import jakarta.validation.Validation
import kindl.global.auth.jwt.JwtProperties

class OauthPropertiesTest : DescribeSpec({

    val beanValidator = Validation.buildDefaultValidatorFactory().validator

    describe("OAuth client ID 설정 검증") {
        it("client ID가 하나 이상이고 공백이 아니면 통과한다") {
            beanValidator.validate(GoogleOauthProperties(listOf("google-client-id"))).shouldBeEmpty()
            beanValidator.validate(KakaoOauthProperties(listOf("kakao-client-id"))).shouldBeEmpty()
        }

        it("client ID 목록이 비어 있으면 실패한다") {
            beanValidator.validate(GoogleOauthProperties(emptyList())).shouldNotBeEmpty()
            beanValidator.validate(KakaoOauthProperties(emptyList())).shouldNotBeEmpty()
        }

        it("공백 client ID가 포함되면 실패한다") {
            beanValidator.validate(GoogleOauthProperties(listOf(""))).shouldNotBeEmpty()
            beanValidator.validate(KakaoOauthProperties(listOf(" "))).shouldNotBeEmpty()
        }
    }

    describe("JWT 설정 검증") {
        it("32자 이상의 secret과 양수 만료 시간은 통과한다") {
            beanValidator.validate(
                JwtProperties(
                    secret = "a".repeat(32),
                    accessTokenExpireTime = 3_600_000,
                    refreshTokenExpireTime = 1_209_600_000,
                ),
            ).shouldBeEmpty()
        }

        it("짧은 secret 또는 0 이하 만료 시간은 실패한다") {
            beanValidator.validate(
                JwtProperties(
                    secret = "short-secret",
                    accessTokenExpireTime = 0,
                    refreshTokenExpireTime = -1,
                ),
            ).shouldNotBeEmpty()
        }
    }
})
