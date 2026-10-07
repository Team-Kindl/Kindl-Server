package kindl.api.common

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotContainDuplicates
import kindl.core.error.CommonError
import kindl.core.error.ErrorCode
import kindl.domain.auth.error.AuthError
import kindl.domain.promise.error.PromiseError
import kindl.domain.room.error.RoomError
import kindl.domain.user.error.UserError
import kindl.domain.verification.error.VerificationError
import java.util.Properties

/** 번역 누락을 배포 전에 잡는다. 새 ErrorCode enum을 만들면 여기에 추가한다. */
class ErrorMessageKeysTest : DescribeSpec({

    val allCodes: List<ErrorCode> = listOf(
        CommonError.entries,
        AuthError.entries,
        UserError.entries,
        RoomError.entries,
        PromiseError.entries,
        VerificationError.entries,
    ).flatten()

    fun load(name: String) = Properties().apply {
        Thread.currentThread().contextClassLoader.getResourceAsStream(name)!!.reader(Charsets.UTF_8).use(::load)
    }

    describe("ErrorCode") {
        it("도메인끼리 코드 이름이 겹치지 않는다") {
            allCodes.map { it.code }.shouldNotContainDuplicates()
        }

        listOf("messages.properties", "messages_ko.properties").forEach { file ->
            it("모든 코드의 문구가 $file 에 있다") {
                val messages = load(file)
                allCodes.filter { messages.getProperty(it.messageKey).isNullOrBlank() }.map { it.code }.shouldBeEmpty()
            }
        }
    }
})
