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
 * 실제 Google 로그인 → ID 토큰 발급 → Google 서명 검증 → 우리 JWT 발급을 검증하는 수동 E2E 테스트.
 *
 * 기본 test 태스크에서는 skip되며, `./gradlew googleLoginE2e`로만 실행한다.
 * OAuth 클라이언트에는 [CALLBACK_URI]를 승인된 redirect URI로 등록해야 한다.
 * 자격값은 환경변수 또는 Git에서 제외된 `.env.local`에서 읽는다.
 */
class GoogleLoginE2ETest : DescribeSpec({

    val isE2eTestEnabled = System.getProperty(E2E_ENABLED_PROPERTY) == "true"

    describe("실제 Google OAuth 로그인") {
        it("Google ID 토큰을 받아 로그인 API에서 우리 JWT를 발급한다")
            .config(enabled = isE2eTestEnabled, timeout = 5.minutes) {
                val e2eCredentials = E2eCredentials.load()
                val googleLoginResult = InteractiveGoogleLogin(e2eCredentials).login()

                val databaseName = "kindl_google_e2e_${UUID.randomUUID().toString().replace("-", "")}"
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
                    "--oauth.google.client-ids=${e2eCredentials.clientId}",
                )

                try {
                    @Suppress("UNCHECKED_CAST")
                    val oidcDecoders = applicationContext.getBean("oidcDecoders") as Map<OauthProvider, JwtDecoder>
                    val decodedGoogleJwt = oidcDecoders.getValue(OauthProvider.GOOGLE).decode(googleLoginResult.idToken)
                    decodedGoogleJwt.getClaimAsString("nonce") shouldBe googleLoginResult.nonce

                    val applicationPort = applicationContext.environment
                        .getRequiredProperty("local.server.port", Int::class.java)

                    // 미가입 유저 → 202 + registrationKey
                    val loginResponse = postLogin(applicationPort, googleLoginResult.idToken)
                    loginResponse.statusCode() shouldBe 202
                    val registrationKey = OBJECT_MAPPER.readTree(loginResponse.body())
                        .path("data").path("registrationKey").asString()
                    registrationKey.shouldNotBeBlank()

                    // 회원가입 → 201 + 우리 JWT
                    val signupResponse = postSignup(applicationPort, registrationKey, "e2e-tester")
                    signupResponse.statusCode() shouldBe 201
                    val signupResponseBody = OBJECT_MAPPER.readTree(signupResponse.body())
                    signupResponseBody.path("code").asString() shouldBe "CREATED"
                    val accessToken = signupResponseBody.path("data").path("accessToken").path("token").asString()
                    val refreshToken = signupResponseBody.path("data").path("refreshToken").path("token").asString()
                    accessToken.shouldNotBeBlank()
                    refreshToken.shouldNotBeBlank()

                    val registeredUser = applicationContext.getBean(UserRepository::class.java)
                        .findByProviderAndProviderId(OauthProvider.GOOGLE, decodedGoogleJwt.subject)
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
        private const val E2E_ENABLED_PROPERTY = "kindl.google-e2e.enabled"
        private const val CALLBACK_PORT = 8765
        private const val CALLBACK_PATH = "/oauth2/callback"
        private const val CALLBACK_URI = "http://127.0.0.1:$CALLBACK_PORT$CALLBACK_PATH"
        private val OBJECT_MAPPER = ObjectMapper()
        private val HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()

        private fun postLogin(applicationPort: Int, idToken: String): HttpResponse<String> {
            val loginRequest = HttpRequest.newBuilder(
                URI.create("http://127.0.0.1:$applicationPort/api/v1/auth/login/google"),
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
            val clientId: String,
            val clientSecret: String,
        ) {
            companion object {
                fun load(): E2eCredentials {
                    val environmentFileValues = loadDotenv(Path.of(".env.local"))
                    return E2eCredentials(
                        clientId = value("GOOGLE_E2E_CLIENT_ID", environmentFileValues),
                        clientSecret = value("GOOGLE_E2E_CLIENT_SECRET", environmentFileValues),
                    )
                }

                private fun value(name: String, environmentFileValues: Map<String, String>): String =
                    System.getenv(name)?.takeIf { it.isNotBlank() }
                        ?: environmentFileValues[name]?.takeIf { it.isNotBlank() }
                        ?: error("$name 환경변수 또는 .env.local 값을 설정해 주세요.")

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

        private data class GoogleLoginResult(
            val idToken: String,
            val nonce: String,
        )

        private class InteractiveGoogleLogin(
            private val e2eCredentials: E2eCredentials,
        ) {
            fun login(): GoogleLoginResult {
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
                    check(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        "기본 브라우저를 열 수 없습니다. 데스크톱 환경에서 실행해 주세요."
                    }
                    Desktop.getDesktop().browse(authorizationUri)

                    val authorizationCodeValue = authorizationCodeFuture.get(3, TimeUnit.MINUTES)
                    return GoogleLoginResult(
                        idToken = exchangeCode(authorizationCodeValue),
                        nonce = nonce,
                    )
                } finally {
                    callbackServer.stop(0)
                    callbackExecutor.shutdownNow()
                }
            }

            private fun authorizationUri(oauthState: String, nonce: String): URI = URI.create(
                "https://accounts.google.com/o/oauth2/v2/auth?" +
                    formEncode(
                        mapOf(
                            "client_id" to e2eCredentials.clientId,
                            "redirect_uri" to CALLBACK_URI,
                            "response_type" to "code",
                            "scope" to "openid email profile",
                            "state" to oauthState,
                            "nonce" to nonce,
                            "access_type" to "online",
                            "prompt" to "select_account",
                        ),
                    ),
            )

            private fun exchangeCode(authorizationCode: String): String {
                val tokenExchangeRequest = HttpRequest.newBuilder(URI.create("https://oauth2.googleapis.com/token"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(
                        HttpRequest.BodyPublishers.ofString(
                            formEncode(
                                mapOf(
                                    "code" to authorizationCode,
                                    "client_id" to e2eCredentials.clientId,
                                    "client_secret" to e2eCredentials.clientSecret,
                                    "redirect_uri" to CALLBACK_URI,
                                    "grant_type" to "authorization_code",
                                ),
                            ),
                        ),
                    )
                    .build()

                val tokenExchangeResponse = HTTP_CLIENT.send(tokenExchangeRequest, HttpResponse.BodyHandlers.ofString())
                check(tokenExchangeResponse.statusCode() == 200) {
                    "Google token 교환에 실패했습니다. HTTP ${tokenExchangeResponse.statusCode()}"
                }
                return OBJECT_MAPPER.readTree(tokenExchangeResponse.body()).path("id_token").asString()
                    .takeIf { it.isNotBlank() }
                    ?: error("Google token 응답에 id_token이 없습니다.")
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
                            IllegalStateException("Google 로그인이 취소됐습니다: $oauthError"),
                        )
                        400 to "Google 로그인이 취소되었습니다. 이 창을 닫아도 됩니다."
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
                        200 to "Google 로그인 완료. 테스트가 계속 진행 중이므로 이 창을 닫아도 됩니다."
                    }
                }

                val responseBody =
                    """<!doctype html><meta charset="utf-8"><title>Kindl Google E2E</title><p>$responseMessage</p>"""
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
