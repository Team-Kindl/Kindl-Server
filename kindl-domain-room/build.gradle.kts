// 조인 조회를 QueryDSL로 쓰는 도메인 모듈만 Q클래스를 만든다
plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    api(project(":kindl-core"))
    api(project(":kindl-domain-support"))
    ksp("io.github.openfeign.querydsl:querydsl-ksp-codegen:7.0")
}
