pluginManagement {
    includeBuild("build-logic")
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
rootProject.name = "NeroChatBackendSpringBoot"

include("app")
include("chat")
include("user")
include("notification")
