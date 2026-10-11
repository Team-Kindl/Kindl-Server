package proov.domain.room

import java.security.SecureRandom

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
