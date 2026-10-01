package kindl.domain.user.vo

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import kindl.core.error.KindlException
import kindl.domain.user.error.UserError
import java.text.Normalizer

class NicknameTest : DescribeSpec({

    describe("이모지와 이모지 부품") {
        withData("수달😀", "❤️", "👨‍👩‍👧", "🇰🇷", "1️⃣", "수달🏻", "수달©") { raw ->
            shouldThrow<KindlException> { Nickname.of(raw) }.errorCode shouldBe UserError.NICKNAME_INVALID_CHAR
        }
    }

    describe("허용되는 입력") {
        it("숫자와 샵은 유니코드 이모지 속성이 있어도 허용한다") {
            Nickname.of("수달#123").value shouldBe "수달#123"
        }

        it("앞뒤 공백을 지우고 연속 공백은 한 칸으로 줄인다") {
            Nickname.of("  성실한   수달 ").value shouldBe "성실한 수달"
        }
    }

    describe("길이는 사람이 보는 글자(그래핌)로 센다") {
        it("자모가 분리된 입력도 NFC로 합쳐 센다") {
            val decomposed = Normalizer.normalize("한글수달", Normalizer.Form.NFD)
            Nickname.of(decomposed).value shouldBe "한글수달"
        }

        it("15자는 허용, 16자는 거부한다") {
            Nickname.of("가".repeat(15)).value.length shouldBe 15
            shouldThrow<KindlException> { Nickname.of("가".repeat(16)) }.errorCode shouldBe UserError.NICKNAME_LENGTH
        }

        it("결합 문자를 쓰는 글자는 코드 포인트가 여러 개여도 한 글자다") {
            // 힌디어 "कि" = क + ि (코드 포인트 2개, 그래핌 1개)
            Nickname.of("कि".repeat(15)).value.codePointCount(0, 30) shouldBe 30
        }

        it("공백만 있으면 거부한다") {
            shouldThrow<KindlException> { Nickname.of("   ") }.errorCode shouldBe UserError.NICKNAME_LENGTH
        }
    }

    describe("금칙어와 중복 키") {
        it("전각·대소문자를 바꿔 쓴 금칙어도 막는다") {
            shouldThrow<KindlException> { Nickname.of("ＡＤＭＩＮ짱") }.errorCode shouldBe UserError.NICKNAME_BLOCKED
        }

        it("중복 키는 NFKC + 로케일 없는 소문자다") {
            Nickname.of("Ｏｔｔｅｒ").key shouldBe Nickname.of("otter").key
        }
    }
})
