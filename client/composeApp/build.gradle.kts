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
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)

            implementation("com.google.code.gson:gson:2.10.1")
            implementation("net.treset:mc-version-loader:2.0.2")
            implementation("net.hycrafthd:minecraft_authenticator:3.0.5")

            implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
            implementation("org.slf4j:slf4j-api:2.0.9")
            implementation("ch.qos.logback:logback-classic:1.4.11")

            api("io.github.kevinnzou:compose-webview-multiplatform:1.6.0")
            implementation("me.friwi:jcefmaven:116.0.19.1")

            implementation("org.jetbrains.jewel:jewel-int-ui-standalone:0.13.0")
            implementation("org.jetbrains.jewel:jewel-int-ui-decorated-window:0.13.0")

            implementation("com.darkrockstudios:mpfilepicker:3.1.0")
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
            includeAllModules = true
        }
    }
}

afterEvaluate {
    tasks.withType<JavaExec> {
        if (System.getProperty("os.name").contains("Mac")) {
            jvmArgs("--add-opens", "java.desktop/sun.awt=ALL-UNNAMED")
            jvmArgs("--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED")
            jvmArgs("--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED")
        }
    }
}


