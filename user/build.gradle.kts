plugins {
    id("spring-boot-service")
}

dependencies {
    implementation(projects.common)

    implementation(libs.redisson.spring.boot.starter)
    implementation(libs.jwt.api)
    runtimeOnly(libs.jwt.impl)
    runtimeOnly(libs.jwt.jackson)
}
