package kindl.api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver
import java.util.Locale

/** 오류 문구는 Accept-Language로 고른다. 지원하지 않는 언어는 en. */
@Configuration
class LocaleConfig {

    @Bean
    fun localeResolver(): LocaleResolver = AcceptHeaderLocaleResolver().apply {
        supportedLocales = SUPPORTED
        setDefaultLocale(Locale.ENGLISH)
    }

    companion object {
        val SUPPORTED: List<Locale> = listOf(Locale.KOREAN, Locale.ENGLISH)
    }
}
