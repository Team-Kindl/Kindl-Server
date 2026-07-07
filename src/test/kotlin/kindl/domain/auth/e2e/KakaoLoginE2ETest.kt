package kindl.domain.auth.e2e

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import kindl.KindlApplication
import kindl.domain.user.entity.OauthProvider
import kindl.domain.user.repository.UserRepository
import kindl.global.auth.security.JwtProvider
import org.springframework.boot.SpringApplication
import org.springframework.security.oauth2.jwt.JwtDecoder
import tools.jackson.databind.ObjectMapper
import java.awt.Desktop
import java.net.InetSocketAddress
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Duration
import java.util.Base64
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes

/**
 * 실제 Kakao 로그인 → 인가 코드 교환 → ID 토큰 검증 → 우리 JWT 발급을 검증하는 수동 E2E 테스트.
 *
 * 기본 test 태스크에서는 skip되며, `./gradlew kakaoLoginE2e`로만 실행한다.
 * Kakao REST API 키에는 [CALLBACK_URI]가 로그인 Redirect URI로 등록돼 있어야 한다.
 * 자격값은 환경변수 또는 Git에서 제외된 `.env.local`에서 읽는다.
 */
class KakaoLoginE2ETest : DescribeSpec({

    val isE2eTestEnabled = System.getProperty(E2E_ENABLED_PROPERTY) == "true"

    describe("실제 Kakao OAuth 로그인") {
        it("Kakao ID 토큰을 받아 로그인 API에서 우리 JWT를 발급한다")
            .config(enabled = isE2eTestEnabled, timeout = 5.minutes) {
                val e2eCredentials = E2eCredentials.load()
                val kakaoLoginResult = InteractiveKakaoLogin(e2eCredentials).login()

                val databaseName = "kindl_kakao_e2e_${UUID.randomUUID().toString().replace("-", "")}"
                val applicationContext = SpringApplication(KindlApplication::class.java).run(
                    "--spring.profiles.active=test",
                    "--server.port=0",
                    "--spring.main.banner-mode=off",
                    "--logging.level.root=WARN",
                    "--spring.datasource.url=jdbc:h2:mem:$databaseName;MODE=MySQL;DB_CLOSE_DELAY=-1",
                    "--spring.datasource.driver-class-name=org.h2.Driver",
                    "--spring.datasource.username=sa",
                    "--spring.datasource.password=",
                    "--spring.jpa.hibernate.ddl-auto=create-drop",
                    "--spring.jpa.show-sql=false",
                    "--oauth.kakao.client-ids=${e2eCredentials.restApiKey}",
                )

                try {
                    @Suppress("UNCHECKED_CAST")
                    val oidcDecoders = applicationContext.getBean("oidcDecoders") as Map<OauthProvider, JwtDecoder>
                    val decodedKakaoJwt = oidcDecoders.getValue(OauthProvider.KAKAO).decode(kakaoLoginResult.idToken)
                    decodedKakaoJwt.getClaimAsString("nonce") shouldBe kakaoLoginResult.nonce

                    val applicationPort = applicationContext.environment
                        .getRequiredProperty("local.server.port", Int::class.java)

                    val loginResponse = postLogin(applicationPort, kakaoLoginResult.idToken)
                    loginResponse.statusCode() shouldBe 202
                    val registrationKey = OBJECT_MAPPER.readTree(loginResponse.body())
                        .path("data").path("registrationKey").asString()
                    registrationKey.shouldNotBeBlank()

                    val signupResponse = postSignup(applicationPort, registrationKey, "kakao-e2e")
                    signupResponse.statusCode() shouldBe 201
                    val signupResponseBody = OBJECT_MAPPER.readTree(signupResponse.body())
                    signupResponseBody.path("code").asString() shouldBe "CREATED"
                    val accessToken = signupResponseBody.path("data").path("accessToken").path("token").asString()
                    val refreshToken = signupResponseBody.path("data").path("refreshToken").path("token").asString()
                    accessToken.shouldNotBeBlank()
                    refreshToken.shouldNotBeBlank()

                    val registeredUser = applicationContext.getBean(UserRepository::class.java)
                        .findByProviderAndProviderId(OauthProvider.KAKAO, decodedKakaoJwt.subject)
                    checkNotNull(registeredUser)

                    val jwtAuthentication = applicationContext.getBean(JwtProvider::class.java)
                        .getAuthentication(accessToken)
                    jwtAuthentication.name shouldBe registeredUser.id
                } finally {
                    applicationContext.close()
                }
            }
    }
}) {
    companion object {
        private const val E2E_ENABLED_PROPERTY = "kindl.kakao-e2e.enabled"
        private const val CALLBACK_PORT = 8766
        private const val CALLBACK_PATH = "/oauth2/callback"
        private const val CALLBACK_URI = "http://127.0.0.1:$CALLBACK_PORT$CALLBACK_PATH"
        private val OBJECT_MAPPER = ObjectMapper()
        private val HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()

        private fun postLogin(applicationPort: Int, idToken: String): HttpResponse<String> {
            val loginRequest = HttpRequest.newBuilder(
                URI.create("http://127.0.0.1:$applicationPort/api/v1/auth/login/kakao"),
            )
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""{"idToken":"$idToken"}"""))
                .build()
            return HTTP_CLIENT.send(loginRequest, HttpResponse.BodyHandlers.ofString())
        }

        private fun postSignup(applicationPort: Int, registrationKey: String, nickname: String): HttpResponse<String> {
            val signupRequest = HttpRequest.newBuilder(
                URI.create("http://127.0.0.1:$applicationPort/api/v1/auth/signup"),
            )
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(
                    HttpRequest.BodyPublishers.ofString(
                        """{"registrationKey":"$registrationKey","nickname":"$nickname","termsAgreed":true}""",
                    ),
                )
                .build()
            return HTTP_CLIENT.send(signupRequest, HttpResponse.BodyHandlers.ofString())
        }

        private data class E2eCredentials(
            val restApiKey: String,
            val clientSecret: String,
            val authorizationCode: String?,
            val nonce: String?,
        ) {
            companion object {
                fun load(): E2eCredentials {
                    val environmentFileValues = loadDotenv(Path.of(".env.local"))
                    return E2eCredentials(
                        restApiKey = value("KAKAO_E2E_REST_API_KEY", environmentFileValues),
                        clientSecret = value("KAKAO_E2E_CLIENT_SECRET", environmentFileValues),
                        authorizationCode = optionalValue("KAKAO_E2E_AUTHORIZATION_CODE", environmentFileValues),
                        nonce = optionalValue("KAKAO_E2E_NONCE", environmentFileValues),
                    )
                }

                private fun value(name: String, environmentFileValues: Map<String, String>): String =
                    System.getenv(name)?.takeIf { it.isNotBlank() }
                        ?: environmentFileValues[name]?.takeIf { it.isNotBlank() }
                        ?: error("$name 환경변수 또는 .env.local 값을 설정해 주세요.")

                private fun optionalValue(name: String, environmentFileValues: Map<String, String>): String? =
                    System.getenv(name)?.takeIf { it.isNotBlank() }
                        ?: environmentFileValues[name]?.takeIf { it.isNotBlank() }

                private fun loadDotenv(path: Path): Map<String, String> {
                    if (!Files.isRegularFile(path)) return emptyMap()
                    return Files.readAllLines(path)
                        .asSequence()
                        .map(String::trim)
                        .filter { it.isNotEmpty() && !it.startsWith("#") && "=" in it }
                        .associate { line ->
                            val (environmentVariableName, rawEnvironmentVariableValue) = line.split("=", limit = 2)
                            environmentVariableName.trim() to
                                rawEnvironmentVariableValue.trim().removeSurrounding("\"").removeSurrounding("'")
                        }
                }
            }
        }

        private data class KakaoLoginResult(
            val idToken: String,
            val nonce: String,
        )

        private class InteractiveKakaoLogin(
            private val e2eCredentials: E2eCredentials,
        ) {
            fun login(): KakaoLoginResult {
                e2eCredentials.authorizationCode?.let { authorizationCode ->
                    val nonce = checkNotNull(e2eCredentials.nonce) {
                        "KAKAO_E2E_AUTHORIZATION_CODE 사용 시 KAKAO_E2E_NONCE도 필요합니다."
                    }
                    return KakaoLoginResult(
                        idToken = exchangeCode(authorizationCode),
                        nonce = nonce,
                    )
                }

                val oauthState = secureRandomValue()
                val nonce = secureRandomValue()
                val authorizationCodeFuture = CompletableFuture<String>()
                val callbackExecutor = Executors.newSingleThreadExecutor()
                val callbackServer = HttpServer.create(InetSocketAddress("127.0.0.1", CALLBACK_PORT), 0).apply {
                    createContext(CALLBACK_PATH) { httpExchange ->
                        handleCallback(httpExchange, oauthState, authorizationCodeFuture)
                    }
                    this.executor = callbackExecutor
                    start()
                }

                try {
                    val authorizationUri = authorizationUri(oauthState, nonce)
                    println("KAKAO_E2E_AUTHORIZATION_URL=$authorizationUri")
                    check(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        "기본 브라우저를 열 수 없습니다. 데스크톱 환경에서 실행해 주세요."
                    }
                    Desktop.getDesktop().browse(authorizationUri)

                    val authorizationCodeValue = authorizationCodeFuture.get(3, TimeUnit.MINUTES)
                    return KakaoLoginResult(
                        idToken = exchangeCode(authorizationCodeValue),
                        nonce = nonce,
                    )
                } finally {
                    callbackServer.stop(0)
                    callbackExecutor.shutdownNow()
                }
            }

            private fun authorizationUri(oauthState: String, nonce: String): URI = URI.create(
                "https://kauth.kakao.com/oauth/authorize?" +
                    formEncode(
                        mapOf(
                            "client_id" to e2eCredentials.restApiKey,
                            "redirect_uri" to CALLBACK_URI,
                            "response_type" to "code",
                            "scope" to "openid",
                            "state" to oauthState,
                            "nonce" to nonce,
                            "prompt" to "login",
                        ),
                    ),
            )

            private fun exchangeCode(authorizationCode: String): String {
                val tokenExchangeRequest = HttpRequest.newBuilder(URI.create("https://kauth.kakao.com/oauth/token"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                    .POST(
                        HttpRequest.BodyPublishers.ofString(
                            formEncode(
                                mapOf(
                                    "grant_type" to "authorization_code",
                                    "client_id" to e2eCredentials.restApiKey,
                                    "redirect_uri" to CALLBACK_URI,
                                    "code" to authorizationCode,
                                    "client_secret" to e2eCredentials.clientSecret,
                                ),
                            ),
                        ),
                    )
                    .build()

                val tokenExchangeResponse = HTTP_CLIENT.send(tokenExchangeRequest, HttpResponse.BodyHandlers.ofString())
                check(tokenExchangeResponse.statusCode() == 200) {
                    "Kakao token 교환에 실패했습니다. HTTP ${tokenExchangeResponse.statusCode()}"
                }
                return OBJECT_MAPPER.readTree(tokenExchangeResponse.body()).path("id_token").asString()
                    .takeIf { it.isNotBlank() }
                    ?: error("Kakao token 응답에 id_token이 없습니다.")
            }

            private fun handleCallback(
                httpExchange: HttpExchange,
                expectedState: String,
                authorizationCode: CompletableFuture<String>,
            ) {
                val queryParameters = parseQuery(httpExchange.requestURI.rawQuery)
                val oauthError = queryParameters["error"]
                val returnedOauthState = queryParameters["state"]
                val oauthStateMatches = returnedOauthState != null && MessageDigest.isEqual(
                    expectedState.toByteArray(UTF_8),
                    returnedOauthState.toByteArray(UTF_8),
                )

                val (responseStatus, responseMessage) = when {
                    oauthError != null -> {
                        authorizationCode.completeExceptionally(
                            IllegalStateException("Kakao 로그인이 취소됐습니다: $oauthError"),
                        )
                        400 to "Kakao 로그인이 취소되었습니다. 이 창을 닫아도 됩니다."
                    }
                    !oauthStateMatches -> {
                        authorizationCode.completeExceptionally(IllegalStateException("OAuth state가 일치하지 않습니다."))
                        400 to "잘못된 OAuth 응답입니다. 이 창을 닫아도 됩니다."
                    }
                    queryParameters["code"].isNullOrBlank() -> {
                        authorizationCode.completeExceptionally(IllegalStateException("Authorization code가 없습니다."))
                        400 to "Authorization code가 없습니다. 이 창을 닫아도 됩니다."
                    }
                    else -> {
                        authorizationCode.complete(queryParameters.getValue("code"))
                        200 to "Kakao 로그인 완료. 테스트가 계속 진행 중이므로 이 창을 닫아도 됩니다."
                    }
                }

                val responseBody =
                    """<!doctype html><meta charset="utf-8"><title>Kindl Kakao E2E</title><p>$responseMessage</p>"""
                val responseBytes = responseBody.toByteArray(UTF_8)
                httpExchange.responseHeaders.set("Content-Type", "text/html; charset=utf-8")
                httpExchange.sendResponseHeaders(responseStatus, responseBytes.size.toLong())
                httpExchange.responseBody.use { responseOutputStream -> responseOutputStream.write(responseBytes) }
            }

            private fun parseQuery(rawQuery: String?): Map<String, String> =
                rawQuery.orEmpty()
                    .split("&")
                    .filter { it.isNotBlank() }
                    .associate { parameter ->
                        val (parameterName, encodedParameterValue) = parameter.split("=", limit = 2).let {
                            it[0] to it.getOrElse(1) { "" }
                        }
                        URLDecoder.decode(parameterName, UTF_8) to
                            URLDecoder.decode(encodedParameterValue, UTF_8)
                    }

            private fun secureRandomValue(): String {
                val randomBytes = ByteArray(32).also(SecureRandom()::nextBytes)
                return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
            }

            private fun formEncode(values: Map<String, String>): String =
                values.entries.joinToString("&") { (parameterName, parameterValue) ->
                    "${URLEncoder.encode(parameterName, UTF_8)}=${URLEncoder.encode(parameterValue, UTF_8)}"
                }
        }
    }
}
