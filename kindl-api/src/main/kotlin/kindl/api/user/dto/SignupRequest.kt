package kindl.api.user.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kindl.api.auth.SignupCommand
import kindl.api.auth.dto.DeviceRequest
import kindl.core.error.KindlException
import kindl.domain.user.command.CreateUserCommand
import kindl.domain.user.error.UserError
import kindl.domain.user.vo.Nickname
import java.time.DateTimeException
import java.time.ZoneId
import java.util.Locale

/**
 * 익명 프로필 저장 = 유저 생성.
 * 형식(필수·길이)은 여기서, 닉네임 규칙(그래핌·이모지·금칙어)은 Nickname이 맡는다.
 */
data class SignupRequest(
    @field:NotBlank @field:Size(max = 2_048)
    val signupToken: String?,
    @field:NotBlank @field:Size(max = 60)
    val nickname: String?,
    @field:NotBlank @field:Size(max = 40)
    val timezone: String?,
    @field:NotBlank @field:Size(max = 35)
    val locale: String?,
    @field:NotBlank @field:Size(max = 20)
    val termsVersion: String?,
    @field:NotNull @field:Valid
    val device: DeviceRequest?,
) {
    fun toCommand() = SignupCommand(
        signupToken = signupToken!!,
        user = CreateUserCommand(
            nickname = Nickname.of(nickname!!),
            timezone = parseZone(timezone!!),
            locale = parseLocale(locale!!),
            termsVersion = termsVersion!!,
        ),
        device = device!!.toCommand(),
    )

    private fun parseZone(raw: String): ZoneId = try {
        ZoneId.of(raw)
    } catch (e: DateTimeException) {
        throw KindlException(UserError.INVALID_TIMEZONE, "timezone=$raw", e)
    }

    private fun parseLocale(raw: String): Locale =
        Locale.forLanguageTag(raw).takeIf { it.language.isNotEmpty() }
            ?: throw KindlException(UserError.INVALID_LOCALE, "locale=$raw")
}
