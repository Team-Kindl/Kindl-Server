package proov.core.text

import java.text.BreakIterator
import java.text.Normalizer
import java.util.Locale

object DisplayText {
    private val SPACES = Regex("\\s+")

    fun normalize(raw: String): String =
        Normalizer.normalize(raw, Normalizer.Form.NFC).trim().replace(SPACES, " ")

    fun graphemeCount(value: String, limit: Int = Int.MAX_VALUE): Int {
        val iterator = BreakIterator.getCharacterInstance(Locale.ROOT).apply { setText(value) }
        var count = 0
        while (iterator.next() != BreakIterator.DONE) {
            if (++count > limit) return count
        }
        return count
    }

    fun containsForbiddenChar(value: String): Boolean = value.codePoints().anyMatch(::isForbidden)

    private fun isForbidden(codePoint: Int): Boolean =
        Character.isExtendedPictographic(codePoint) ||
            Character.isEmojiModifier(codePoint) ||
            codePoint == 0x200D ||
            codePoint in 0xFE00..0xFE0F ||
            codePoint == 0x20E3 ||
            codePoint in 0x1F1E6..0x1F1FF ||
            codePoint in 0xE0020..0xE007F ||
            Character.getType(codePoint).let {
                it == Character.CONTROL.toInt() ||
                    it == Character.PRIVATE_USE.toInt() ||
                    it == Character.SURROGATE.toInt()
            }
}
