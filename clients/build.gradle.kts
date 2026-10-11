dependencies {
    implementation(project(":core"))
    implementation(project(":domain:domain-auth"))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework:spring-web")
    implementation("org.springframework.security:spring-security-oauth2-jose")
}
