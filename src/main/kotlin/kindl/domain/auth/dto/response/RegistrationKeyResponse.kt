package kindl.domain.auth.dto.response

@ConsistentCopyVisibility
data class RegistrationKeyResponse private constructor(
    val registrationKey: String,
) {
    companion object {
        fun of(registrationKey: String): RegistrationKeyResponse = RegistrationKeyResponse(
            registrationKey = registrationKey,
        )
    }
}
