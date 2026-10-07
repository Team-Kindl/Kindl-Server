// 공통 엔티티(Base*)의 Q클래스는 이 모듈에서 만들어 쓰는 쪽(room)의 Q클래스가 참조한다
plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    api(project(":kindl-core"))
    api("io.github.openfeign.querydsl:querydsl-jpa:7.0")
    ksp("io.github.openfeign.querydsl:querydsl-ksp-codegen:7.0")
}
