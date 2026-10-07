package kindl.support.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.util.Locale

/** BCP 47 언어 태그(예: ko-KR)로 저장한다. */
@Converter
class LocaleConverter : AttributeConverter<Locale, String> {
    override fun convertToDatabaseColumn(attribute: Locale?): String? = attribute?.toLanguageTag()

    override fun convertToEntityAttribute(dbData: String?): Locale? = dbData?.let(Locale::forLanguageTag)
}
