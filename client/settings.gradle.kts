rootProject.name = "TreeLauncher"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven {
            url = uri("https://repo.u-team.info")
        }
        maven {
            url = uri("https://raw.githubusercontent.com/Tre5et/maven/main/")
        }
        maven("https://jogamp.org/deployment/maven")
    }
}

include(":composeApp")