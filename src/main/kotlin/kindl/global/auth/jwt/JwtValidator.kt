package kindl.global.auth.jwt

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import kindl.global.exception.CustomException
import kindl.global.exception.ErrorCode
import org.springframework.stereotype.Component

@Component
class JwtValidator(
    private val keyProvider: KeyProvider,
) {
    fun parse(token: String) =
        try {
            Jwts.parser()
                .verifyWith(keyProvider.getSigningKey())
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: ExpiredJwtException) {
            throw CustomException(ErrorCode.EXPIRED_TOKEN)
        } catch (e: JwtException) {
            throw CustomException(ErrorCode.INVALID_TOKEN)
        } catch (e: IllegalArgumentException) {
            throw CustomException(ErrorCode.INVALID_TOKEN)
        }
}
