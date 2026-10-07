package kindl.domain.room.vo

import java.security.SecureRandom

/**
 * 8자리 초대 코드. 헷갈리는 0 O 1 I를 뺀 32문자 → 32^8 ≈ 1.1조 개.
 * ID가 아니라 업무 키다. 지워진 모임의 코드도 재사용하지 않는다(uk_rooms_code).
 */
@JvmInline
value class InviteCode private constructor(val value: String) {

    companion object {
        const val LENGTH = 8
        private const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        private val random = SecureRandom()

        fun generate(): InviteCode =
            InviteCode(String(CharArray(LENGTH) { ALPHABET[random.nextInt(ALPHABET.length)] }))

        fun parse(raw: String): InviteCode? =
            raw.trim().uppercase().takeIf { code -> code.length == LENGTH && code.all { it in ALPHABET } }
                ?.let(::InviteCode)
    }
}
