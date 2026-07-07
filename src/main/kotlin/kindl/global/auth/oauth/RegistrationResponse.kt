package kindl.global.auth.oauth

@ConsistentCopyVisibility
data class RegistrationResponse private constructor(
    val registrationKey: String,
) {
    companion object {
        fun of(registrationKey: String): RegistrationResponse = RegistrationResponse(
            registrationKey = registrationKey,
        )
    }
}
