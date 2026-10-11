plugins {
    `java-test-fixtures`
}

dependencies {
    api(project(":core"))
    implementation(project(":domain:domain-user"))
    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-tx")
}
