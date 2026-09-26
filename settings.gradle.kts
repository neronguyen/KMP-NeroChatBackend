pluginManagement {
    includeBuild("build-logic")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
rootProject.name = "NeroChatBackendSpringBoot"

include("app")
include("common")
include("chat")
include("user")
include("notification")
