package proov.domain.user

import proov.core.exception.ProovException
import proov.core.text.DisplayText
import proov.domain.user.exception.UserFailure
import java.text.Normalizer
import java.util.Locale

@JvmInline
value class Nickname private constructor(val value: String) {

    val key: String get() = keyOf(value)

    companion object {
        const val MAX_GRAPHEMES = 15
        private val BLOCKED = setOf("운영자", "관리자", "admin", "proov")

        fun of(raw: String): Nickname {
            val normalized = DisplayText.normalize(raw)
            if (DisplayText.graphemeCount(normalized, MAX_GRAPHEMES) !in 1..MAX_GRAPHEMES) {
                throw ProovException(UserFailure.NICKNAME_LENGTH)
            }
            if (DisplayText.containsForbiddenChar(normalized)) throw ProovException(UserFailure.NICKNAME_INVALID_CHAR)
            if (BLOCKED.any { keyOf(normalized).contains(it) }) throw ProovException(UserFailure.NICKNAME_BLOCKED)
            return Nickname(normalized)
        }

        fun restore(value: String) = Nickname(value)

        fun keyOf(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFKC).lowercase(Locale.ROOT)
    }
}
