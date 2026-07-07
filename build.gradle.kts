plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.spring") version "2.3.21"
    kotlin("plugin.jpa") version "2.3.21"
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.google.devtools.ksp") version "2.3.7"
}

group = "kindl"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // TSID
    implementation("io.hypersistence:hypersistence-utils-hibernate-70:3.10.3")

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // MySQL
    runtimeOnly("com.mysql:mysql-connector-j")

    // QueryDSL
    implementation("io.github.openfeign.querydsl:querydsl-jpa:7.0")
    ksp("io.github.openfeign.querydsl:querydsl-ksp-codegen:7.0")

    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Monitoring
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")

    // Security
    implementation("org.springframework.boot:spring-boot-starter-security")

    // OAuth2 / OIDC (Google ID token 검증)
    implementation("org.springframework.security:spring-security-oauth2-jose")

    // Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("com.h2database:h2")

    // Kotest + MockK
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")
    testImplementation("io.mockk:mockk:1.13.13")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register<Test>("googleLoginE2e") {
    group = "verification"
    description = "브라우저에서 실제 Google 로그인 후 백엔드 로그인 API를 끝까지 검증합니다."
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    useJUnitPlatform()
    filter {
        includeTestsMatching("kindl.domain.auth.e2e.GoogleLoginE2ETest")
    }
    systemProperty("kindl.google-e2e.enabled", "true")
    testLogging {
        events("passed", "failed", "skipped")
        showStandardStreams = true
    }
    outputs.upToDateWhen { false }
}

tasks.register<Test>("kakaoLoginE2e") {
    group = "verification"
    description = "브라우저에서 실제 Kakao 로그인 후 백엔드 로그인 API를 끝까지 검증합니다."
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    useJUnitPlatform()
    filter {
        includeTestsMatching("kindl.domain.auth.e2e.KakaoLoginE2ETest")
    }
    systemProperty("kindl.kakao-e2e.enabled", "true")
    testLogging {
        events("passed", "failed", "skipped")
        showStandardStreams = true
    }
    outputs.upToDateWhen { false }
}
