package kindl.core.text

import java.text.BreakIterator
import java.text.Normalizer
import java.util.Locale

/**
 * 사람이 보는 이름(닉네임·모임 이름·공약 제목)의 공통 규칙.
 * 길이는 그래핌(사람이 보는 글자) 단위로 세고, 이모지·제어 문자는 받지 않는다.
 */
object DisplayText {
    private val SPACES = Regex("\\s+")

    /** NFC 정규화, 앞뒤 공백 제거, 연속 공백은 한 칸 */
    fun normalize(raw: String): String =
        Normalizer.normalize(raw, Normalizer.Form.NFC).trim().replace(SPACES, " ")

    /** Java 20+ BreakIterator는 유니코드 확장 그래핌 클러스터 단위로 끊는다. limit을 넘으면 더 세지 않는다 */
    fun graphemeCount(value: String, limit: Int = Int.MAX_VALUE): Int {
        val iterator = BreakIterator.getCharacterInstance(Locale.ROOT).apply { setText(value) }
        var count = 0
        while (iterator.next() != BreakIterator.DONE) {
            if (++count > limit) return count
        }
        return count
    }

    fun containsForbiddenChar(value: String): Boolean = value.codePoints().anyMatch(::isForbidden)

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
