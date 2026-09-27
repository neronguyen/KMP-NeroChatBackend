plugins {
    id("spring-boot-service")
}

dependencies {
    implementation(projects.common)

    implementation(platform(libs.spring.boot.dependencies))
    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.spring.boot.starter.mail)
    implementation(libs.spring.boot.starter.thymeleaf)
}
