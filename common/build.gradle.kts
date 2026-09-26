plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.spring)
}

dependencies {
    implementation(platform(libs.spring.boot.dependencies))
    implementation(libs.spring.boot.starter.amqp)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
}

kotlin {
    jvmToolchain(21)
}
