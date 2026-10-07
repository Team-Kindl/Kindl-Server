import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.3.21" apply false
    kotlin("plugin.spring") version "2.3.21" apply false
    kotlin("plugin.jpa") version "2.3.21" apply false
    id("org.springframework.boot") version "4.0.6" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("com.google.devtools.ksp") version "2.3.7" apply false
}

val kotlinVersion = "2.3.21"

allprojects {
    group = "kindl"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

// 도메인 모듈 공통: JPA 엔티티
val domainModules = setOf(
    "kindl-domain-support",
    "kindl-domain-user",
    "kindl-domain-auth",
    "kindl-domain-room",
    "kindl-domain-promise",
    "kindl-domain-verification",
)

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "java-library")

    configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
        imports {
            // Boot BOM이 Kotlin 컴파일러 클래스패스를 BOM 버전으로 내리지 않게 플러그인 버전에 맞춘다
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES) {
                bomProperty("kotlin.version", kotlinVersion)
            }
        }
    }

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
            jvmTarget = JvmTarget.JVM_21
        }
    }

    dependencies {
        "implementation"("org.jetbrains.kotlin:kotlin-reflect")

        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
        "testImplementation"("io.kotest:kotest-runner-junit5:5.9.1")
        "testImplementation"("io.kotest:kotest-assertions-core:5.9.1")
        "testImplementation"("io.kotest:kotest-framework-datatest:5.9.1")
        "testImplementation"("io.mockk:mockk:1.13.13")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        systemProperty("user.timezone", "UTC")
    }

    if (name in domainModules) {
        apply(plugin = "org.jetbrains.kotlin.plugin.jpa")

        configure<org.jetbrains.kotlin.allopen.gradle.AllOpenExtension> {
            annotation("jakarta.persistence.Entity")
            annotation("jakarta.persistence.MappedSuperclass")
            annotation("jakarta.persistence.Embeddable")
        }

        dependencies {
            "api"("org.springframework.boot:spring-boot-starter-data-jpa")
        }
    }
}
