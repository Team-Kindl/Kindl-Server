package kindl.domain.promise.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import kindl.core.type.VerificationMethod

/** 인증 방식 집합을 "PHOTO,TEXT"처럼 정렬된 문자열로 저장한다. */
@Converter
class VerificationMethodsConverter : AttributeConverter<Set<VerificationMethod>, String> {
    override fun convertToDatabaseColumn(attribute: Set<VerificationMethod>?): String? =
        attribute?.sorted()?.joinToString(",") { it.name }

    override fun convertToEntityAttribute(dbData: String?): Set<VerificationMethod>? =
        dbData?.split(',')?.filter { it.isNotBlank() }?.map(VerificationMethod::valueOf)?.toSet()
}
