package kindl.api.security

import com.nimbusds.jose.jwk.source.ImmutableSecret
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/** HS256 키 하나로 인코더·디코더를 만든다. 디코더는 HS256으로 고정해 alg 바꿔치기를 막는다. */
internal object JwtKeys {

    fun secretKey(raw: String): SecretKey = SecretKeySpec(raw.toByteArray(Charsets.UTF_8), "HmacSHA256")

    fun encoder(key: SecretKey): JwtEncoder = NimbusJwtEncoder(ImmutableSecret<SecurityContext>(key))

    fun decoder(key: SecretKey): NimbusJwtDecoder =
        NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build()

}
