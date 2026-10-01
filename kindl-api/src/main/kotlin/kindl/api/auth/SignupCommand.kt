package kindl.api.auth

import kindl.domain.user.command.CreateUserCommand
import kindl.domain.user.command.DeviceCommand

data class SignupCommand(
    val signupToken: String,
    val user: CreateUserCommand,
    val device: DeviceCommand,
)
