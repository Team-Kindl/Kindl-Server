package proov.storage.db.common

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.util.Locale

@Converter
class LocaleConverter : AttributeConverter<Locale, String> {
    override fun convertToDatabaseColumn(attribute: Locale?): String? = attribute?.toLanguageTag()

    override fun convertToEntityAttribute(dbData: String?): Locale? = dbData?.let(Locale::forLanguageTag)
}
