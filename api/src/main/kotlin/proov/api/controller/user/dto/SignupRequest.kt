package proov.api.controller.user.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import proov.api.controller.auth.dto.DeviceRequest
import proov.core.exception.ProovException
import proov.domain.auth.command.SignupCommand
import proov.domain.user.Nickname
import proov.domain.user.command.CreateUserCommand
import proov.domain.user.exception.UserFailure
import java.time.DateTimeException
import java.time.ZoneId
import java.util.Locale

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
        throw ProovException(UserFailure.INVALID_TIMEZONE, "timezone=$raw", e)
    }

    private fun parseLocale(raw: String): Locale =
        Locale.forLanguageTag(raw).takeIf { it.language.isNotEmpty() }
            ?: throw ProovException(UserFailure.INVALID_LOCALE, "locale=$raw")
}
