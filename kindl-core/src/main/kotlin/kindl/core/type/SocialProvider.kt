package kindl.core.type

enum class SocialProvider {
    KAKAO,
    APPLE,
    GOOGLE,
    ;

    companion object {
        fun fromPath(value: String): SocialProvider? =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}
