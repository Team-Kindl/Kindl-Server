package kindl.global.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI {
        val bearerAuthenticationScheme = SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .`in`(SecurityScheme.In.HEADER)
            .name("Authorization")

        return OpenAPI()
            .info(
                Info()
                    .title("Kindl API")
                    .description("Kindl 서버 API 문서. 토큰 입력 시 'Bearer ' 제외하고 입력하세요.")
                    .version("v1"),
            )
            .components(Components().addSecuritySchemes(SECURITY_SCHEME_NAME, bearerAuthenticationScheme))
            .addSecurityItem(SecurityRequirement().addList(SECURITY_SCHEME_NAME))
    }

    companion object {
        private const val SECURITY_SCHEME_NAME = "BearerAuth"
    }
}
