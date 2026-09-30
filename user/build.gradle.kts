plugins {
    id("spring-boot-service")
}

dependencies {
    implementation(projects.common)

    implementation(libs.jwt.api)
    runtimeOnly(libs.jwt.impl)
    runtimeOnly(libs.jwt.jackson)
}
