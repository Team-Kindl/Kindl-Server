package kindl.api.auth

import io.kotest.core.spec.style.DescribeSpec
import kindl.support.IntegrationTestConfig
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(IntegrationTestConfig::class)
class NicknameAvailabilityIntegrationTest(
    private val mockMvc: MockMvc,
) : DescribeSpec({

    fun check(value: String) = mockMvc.perform(get("/api/v1/nicknames/availability").param("value", value))

    describe("닉네임 확인 (로그인 전)") {
        it("규칙에 맞고 아무도 안 쓰면 사용 가능") {
            check("새로운 수달").andExpect(status().isOk)
                .andExpect(jsonPath("$.data.available").value(true))
                .andExpect(jsonPath("$.data.reason").doesNotExist())
        }

        it("규칙 위반은 이유 코드와 함께 사용 불가") {
            check("수달😀").andExpect(jsonPath("$.data.available").value(false))
                .andExpect(jsonPath("$.data.reason").value("NICKNAME_INVALID_CHAR"))
            check("admin").andExpect(jsonPath("$.data.reason").value("NICKNAME_BLOCKED"))
        }
    }
})
