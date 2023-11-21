import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    
    alias(libs.plugins.jetbrainsCompose)
}

kotlin {
    jvm("desktop")
    
    sourceSets {
        val desktopMain by getting
        
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)

            implementation("com.google.code.gson:gson:2.10.1")
            implementation("net.treset:mc-version-loader:2.0.0")
            implementation("net.hycrafthd:minecraft_authenticator:3.0.5")

            implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
            implementation("org.slf4j:slf4j-api:2.0.9")
            implementation("ch.qos.logback:logback-classic:1.4.11")
        }
    }
}


compose.desktop {
    application {
        mainClass = "net.treset.treelauncher.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "net.treset.treelauncher"
            packageVersion = "1.0.0"
        }
    }
}


