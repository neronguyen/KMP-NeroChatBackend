plugins {
    id("spring-boot-service")
}

dependencies {
    implementation(libs.jwt.api)
    runtimeOnly(libs.jwt.impl)
    runtimeOnly(libs.jwt.jackson)
}
