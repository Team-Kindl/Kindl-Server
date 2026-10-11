plugins {
    kotlin("plugin.jpa")
    id("com.google.devtools.ksp")
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain:domain-user"))
    implementation(project(":domain:domain-auth"))
    implementation(project(":domain:domain-room"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")

    implementation("io.github.openfeign.querydsl:querydsl-jpa:7.0")
    ksp("io.github.openfeign.querydsl:querydsl-ksp-codegen:7.0")
}
