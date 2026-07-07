package kindl.global.auth.oauth

import kindl.global.exception.CustomException
import kindl.global.exception.ErrorCode

enum class OauthProvider {
    GOOGLE,
    KAKAO,
    APPLE,
    ;

    companion object {
        fun from(value: String): OauthProvider =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: throw CustomException(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER)
    }
}
