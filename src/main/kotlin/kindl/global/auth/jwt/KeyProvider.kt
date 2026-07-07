package kindl.global.auth.jwt

import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import javax.crypto.SecretKey

@Component
class KeyProvider(
    jwtProperties: JwtProperties,
) {
    private val signingKey: SecretKey =
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray(StandardCharsets.UTF_8))

    fun getSigningKey(): SecretKey = signingKey
}
