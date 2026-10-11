package proov.domain.auth.exception

import proov.core.code.FailureCode

enum class AuthFailure(
    override val status: Int,
    override val message: String,
) : FailureCode {
    AUTH_REQUIRED(401, "로그인이 필요해요"),
    TOKEN_EXPIRED(401, "로그인이 만료됐어요"),
    TOKEN_INVALID(401, "로그인 정보가 올바르지 않아요. 다시 로그인해 주세요"),
    REFRESH_INVALID(401, "로그인이 만료됐어요. 다시 로그인해 주세요"),
    REFRESH_REUSED(401, "보안을 위해 로그아웃됐어요. 다시 로그인해 주세요"),
    REFRESH_RACE(409, "다시 시도해 주세요"),
    SIGNUP_TOKEN_INVALID(401, "가입 시간이 지났어요. 다시 로그인해 주세요"),
    SOCIAL_PROVIDER_UNSUPPORTED(400, "지원하지 않는 로그인 방식이에요"),
    SOCIAL_TOKEN_INVALID(401, "소셜 로그인에 실패했어요. 다시 시도해 주세요"),
    SOCIAL_PROVIDER_UNAVAILABLE(503, "로그인 서비스에 잠시 문제가 있어요"),
    ALREADY_SIGNED_UP(409, "이미 가입된 계정이에요. 로그인해 주세요"),
    ;

    override val code: String get() = name
}
