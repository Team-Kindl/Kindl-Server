package kindl.core.error

/**
 * 도메인 모듈마다 enum으로 구현한다. core는 인터페이스만 둔다.
 */
interface ErrorCode {
    /** 앱 분기용 고정 문자열. 한 번 내보내면 바꾸지 않는다. */
    val code: String
    val type: ErrorType

    /** messages_{ko,en}.properties의 키 */
    val messageKey: String get() = "error.$code"
}
