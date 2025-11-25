plugins {
    id("spring-boot-service")
}

dependencies {
    implementation(libs.spring.boot.starter.security)
    testImplementation(kotlin("test"))
}
