package kindl.api.auth.dto.command

import kindl.domain.user.dto.command.CreateUserCommand
import kindl.domain.user.dto.command.DeviceCommand

data class SignupCommand(
    val signupToken: String,
    val user: CreateUserCommand,
    val device: DeviceCommand,
)
