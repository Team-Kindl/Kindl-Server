package kindl.global.auth.security

import io.jsonwebtoken.Claims
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kindl.global.auth.jwt.JwtGenerator
import kindl.global.auth.jwt.JwtGenerator.Companion.ROLES_KEY
import kindl.global.auth.jwt.JwtGenerator.Companion.TYPE_KEY
import kindl.global.auth.jwt.JwtValidator
import kindl.global.auth.jwt.TokenType
import kindl.global.exception.CustomException
import kindl.global.exception.ErrorCode

class JwtProviderTest : DescribeSpec({

    val jwtGenerator = mockk<JwtGenerator>()
    val jwtValidator = mockk<JwtValidator>()
    val jwtProvider = JwtProvider(jwtGenerator, jwtValidator)

    describe("getAuthentication") {
        it("문자열 역할 claim을 GrantedAuthority로 변환한다") {
            val tokenClaims = mockk<Claims>()
            every { jwtValidator.parse("access-token") } returns tokenClaims
            every { tokenClaims[TYPE_KEY] } returns TokenType.ACCESS_TOKEN.name
            every { tokenClaims.subject } returns "user-id"
            every { tokenClaims[ROLES_KEY] } returns listOf("USER", "ADMIN")

            val authentication = jwtProvider.getAuthentication("access-token")

            authentication.name shouldBe "user-id"
            authentication.authorities.map { grantedAuthority -> grantedAuthority.authority }
                .shouldContainExactly("ROLE_USER", "ROLE_ADMIN")
        }

        it("역할 claim에 문자열이 아닌 값이 있으면 INVALID_TOKEN") {
            val tokenClaims = mockk<Claims>()
            every { jwtValidator.parse("invalid-roles-token") } returns tokenClaims
            every { tokenClaims[TYPE_KEY] } returns TokenType.ACCESS_TOKEN.name
            every { tokenClaims.subject } returns "user-id"
            every { tokenClaims[ROLES_KEY] } returns listOf("USER", 1)

            shouldThrow<CustomException> {
                jwtProvider.getAuthentication("invalid-roles-token")
            }.errorCode shouldBe ErrorCode.INVALID_TOKEN
        }
    }
})
