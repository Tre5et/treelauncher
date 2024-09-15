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
        mavenLocal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://repo.u-team.info")
        maven("https://raw.githubusercontent.com/Tre5et/maven/main/")
        maven("https://jogamp.org/deployment/maven")
        maven("https://packages.jetbrains.team/maven/p/kpm/public/")
        maven("https://jitpack.io")

        flatDir {
            dirs("libs")
        }
    }
}

include(":composeApp")