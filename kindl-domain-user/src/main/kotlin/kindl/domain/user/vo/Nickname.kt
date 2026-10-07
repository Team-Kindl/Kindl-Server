package kindl.domain.user.vo

import kindl.core.error.KindlException
import kindl.domain.user.error.UserError
import java.text.BreakIterator
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
        private val SPACES = Regex("\\s+")

        fun of(raw: String): Nickname {
            val normalized = Normalizer.normalize(raw, Normalizer.Form.NFC).trim().replace(SPACES, " ")
            if (graphemes(normalized) !in 1..MAX_GRAPHEMES) throw KindlException(UserError.NICKNAME_LENGTH)
            if (normalized.codePoints().anyMatch(::isForbidden)) throw KindlException(UserError.NICKNAME_INVALID_CHAR)
            val folded = keyOf(normalized)
            if (BLOCKED.any { folded.contains(it) }) throw KindlException(UserError.NICKNAME_BLOCKED)
            return Nickname(normalized)
        }

        private fun keyOf(value: String): String =
            Normalizer.normalize(value, Normalizer.Form.NFKC).lowercase(Locale.ROOT)

        // Java 20+ BreakIterator는 유니코드 확장 그래핌 클러스터 단위로 끊는다
        private fun graphemes(value: String): Int {
            val iterator = BreakIterator.getCharacterInstance(Locale.ROOT).apply { setText(value) }
            var count = 0
            while (iterator.next() != BreakIterator.DONE) {
                if (++count > MAX_GRAPHEMES) return count
            }
            return count
        }

        // Character.isEmoji()는 0~9, #, *도 이모지 속성이라 쓰지 않는다
        private fun isForbidden(codePoint: Int): Boolean =
            Character.isExtendedPictographic(codePoint) || // 그림 문자 전체 (😀 ❤ ☀ © ® ™ …)
                Character.isEmojiModifier(codePoint) || // 피부색 🏻~🏿
                codePoint == 0x200D || // ZWJ: 👨‍👩‍👧 같은 결합
                codePoint in 0xFE00..0xFE0F || // 이모지 표시 선택자 (❤️의 FE0F)
                codePoint == 0x20E3 || // 키캡 1️⃣
                codePoint in 0x1F1E6..0x1F1FF || // 국기를 만드는 지역 표시 문자
                codePoint in 0xE0020..0xE007F || // 태그 문자 (지역 깃발)
                Character.getType(codePoint).let {
                    it == Character.CONTROL.toInt() ||
                        it == Character.PRIVATE_USE.toInt() ||
                        it == Character.SURROGATE.toInt()
                }
    }
}
