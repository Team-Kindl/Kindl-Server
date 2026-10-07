package kindl.api.common.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * enum을 String으로 받아 검증한다. enum 타입으로 바로 받으면 모르는 값이 역직렬화 예외로 터져
 * 어느 필드가 틀렸는지 담긴 400을 만들 수 없다. 대소문자는 구분하지 않고, null은 @NotNull이 맡는다.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [EnumValidator::class])
annotation class ValidEnum(
    val enumClass: KClass<out Enum<*>>,
    val message: String = "허용되지 않는 값입니다.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
