package kindl.api.common.response

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * 앱은 status가 아니라 code로 분기한다. message는 Accept-Language 문구라 그대로 띄워도 된다.
 * 입력 검증 실패는 errors에 필드별 원인을 한 번에 담는다.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class FailureResponse(
    override val status: Int,
    override val code: String,
    override val message: String,
    val errors: List<FieldError>? = null,
) : ApiResponse {

    data class FieldError(
        val field: String,
        val reason: String,
    )
}
