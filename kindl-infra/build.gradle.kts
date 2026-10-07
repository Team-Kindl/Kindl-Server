// 외부 연동 포트 구현체 (소셜 토큰 검증 등)
dependencies {
    implementation(project(":kindl-core"))
    implementation(project(":kindl-domain-auth"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework:spring-web")
    implementation("org.springframework.security:spring-security-oauth2-jose")
}
