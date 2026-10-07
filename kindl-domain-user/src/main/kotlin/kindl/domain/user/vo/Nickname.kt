package kindl.domain.user.vo

import kindl.core.error.KindlException
import kindl.core.text.DisplayText
import kindl.domain.user.error.UserError
import java.text.Normalizer
import java.util.Locale

/**
 * 닉네임 규칙의 유일한 주인.
 * 정규화 → 길이(그래핌) → 금지 문자(이모지) → 금칙어 순서로 검사한다. 전체 중복은 유니크 인덱스가 최종 판정한다.
 */
@JvmInline
value class Nickname private constructor(val value: String) {

    /** 중복 판정용 키: NFKC로 전각·호환 문자를 펼치고, 로케일 없이 소문자화 */
    val key: String get() = keyOf(value)

    companion object {
        const val MAX_GRAPHEMES = 15
        private val BLOCKED = setOf("운영자", "관리자", "admin", "kindl", "proov")

        fun of(raw: String): Nickname {
            val normalized = DisplayText.normalize(raw)
            if (DisplayText.graphemeCount(normalized, MAX_GRAPHEMES) !in 1..MAX_GRAPHEMES) {
                throw KindlException(UserError.NICKNAME_LENGTH)
            }
            if (DisplayText.containsForbiddenChar(normalized)) throw KindlException(UserError.NICKNAME_INVALID_CHAR)
            val folded = keyOf(normalized)
            if (BLOCKED.any { folded.contains(it) }) throw KindlException(UserError.NICKNAME_BLOCKED)
            return Nickname(normalized)
        }

        private fun keyOf(value: String): String =
            Normalizer.normalize(value, Normalizer.Form.NFKC).lowercase(Locale.ROOT)
    }
}
