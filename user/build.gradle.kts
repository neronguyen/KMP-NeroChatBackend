plugins {
    id("spring-boot-service")
}

dependencies {
    implementation(libs.spring.boot.starter.security)

    implementation(libs.jwt.api)
    runtimeOnly(libs.jwt.impl)

    testImplementation(kotlin("test"))
}
