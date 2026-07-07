package kindl.global.auth.oauth

/**
 * 미가입 유저의 검증된 신원을 회원가입 완료 전까지 임시 보관한다.
 * (운영: Redis, 테스트: 인메모리)
 */
interface RegistrationStore {
    fun save(registrationInfo: RegistrationInfo): String

    /** 가입 정보를 원자적으로 조회하고 즉시 삭제한다. 동일 키는 한 번만 사용할 수 있다. */
    fun consume(registrationKey: String): RegistrationInfo?
}
