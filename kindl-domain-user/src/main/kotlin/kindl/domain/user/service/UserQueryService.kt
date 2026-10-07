package kindl.domain.user.service

import kindl.core.error.KindlException
import kindl.core.extension.orThrow
import kindl.domain.user.dto.result.NicknameAvailability
import kindl.domain.user.dto.result.UserResult
import kindl.domain.user.error.UserError
import kindl.domain.user.repository.UserRepository
import kindl.domain.user.vo.Nickname
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

/**
 * 쿼리 한 번짜리 조회라 @Transactional을 걸지 않는다.
 * 레포지토리 메서드가 이미 readOnly 트랜잭션으로 돌고, 바깥에 하나 더 열면
 * SET SESSION TRANSACTION READ ONLY · COMMIT 왕복만 늘어난다.
 * 여러 쿼리를 한 시점으로 읽어야 하면 호출하는 Facade가 트랜잭션을 연다.
 */
@Service
class UserQueryService(
    private val users: UserRepository,
) {
    fun checkNickname(raw: String): NicknameAvailability {
        val nickname = try {
            Nickname.of(raw)
        } catch (e: KindlException) {
            return NicknameAvailability.unavailable(e.errorCode)
        }
        return if (users.existsByNicknameKey(nickname.key)) {
            NicknameAvailability.unavailable(UserError.NICKNAME_TAKEN)
        } else {
            NicknameAvailability.AVAILABLE
        }
    }

    fun getActive(userId: String): UserResult =
        users.findByIdOrNull(userId).orThrow(UserError.USER_NOT_FOUND).let(UserResult::from)

    /** 매 요청 인증용. 엔티티를 읽지 않고 존재만 본다. */
    fun requireActive(userId: String) {
        if (!users.existsById(userId)) throw KindlException(UserError.USER_NOT_FOUND)
    }
}
