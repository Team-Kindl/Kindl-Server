package kindl.api.security

/**
 * 로그인 유저 ID를 받는다. 탈퇴한 유저의 토큰이면 USER_NOT_FOUND(401).
 * 컨트롤러가 SecurityContextHolder를 직접 만지지 않게 한다.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class CurrentUser
