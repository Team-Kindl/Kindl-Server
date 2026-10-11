package proov.api.common

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldNotContainDuplicates
import proov.api.common.exception.CommonFailure
import proov.domain.auth.exception.AuthFailure
import proov.domain.room.exception.RoomFailure
import proov.domain.user.exception.UserFailure

class FailureCodeTest : DescribeSpec({

    describe("FailureCode") {
        it("도메인끼리 코드 이름이 겹치지 않는다") {
            listOf(CommonFailure.entries, AuthFailure.entries, UserFailure.entries, RoomFailure.entries).flatten()
                .map { it.code }.shouldNotContainDuplicates()
        }
    }
})
