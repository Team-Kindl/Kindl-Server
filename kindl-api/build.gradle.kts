plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":kindl-core"))
    implementation(project(":kindl-domain-support"))
    implementation(project(":kindl-domain-user"))
    implementation(project(":kindl-domain-auth"))
    implementation(project(":kindl-domain-room"))
    implementation(project(":kindl-domain-promise"))
    implementation(project(":kindl-domain-verification"))
    implementation(project(":kindl-infra"))

    // Web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("tools.jackson.module:jackson-module-kotlin")

    // Security (JWT 발급·검증)
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    // DB
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-mysql")
    runtimeOnly("com.mysql:mysql-connector-j")

    // Monitoring · Docs
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-mysql")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")
    testImplementation(testFixtures(project(":kindl-domain-auth")))
}

tasks.bootJar {
    archiveFileName = "kindl-api.jar"
}

tasks.jar {
    enabled = false
}
