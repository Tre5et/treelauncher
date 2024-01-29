import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

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

            api("io.github.kevinnzou:compose-webview-multiplatform:1.8.4")
            implementation("dev.datlag:kcef:2024.01.07.1")

            implementation("org.jetbrains.jewel:jewel-int-ui-standalone:0.13.0")
            implementation("org.jetbrains.jewel:jewel-int-ui-decorated-window:0.13.0")

            implementation("com.darkrockstudios:mpfilepicker:3.1.0")
            implementation("be.digitalia.compose.htmlconverter:htmlconverter:0.9.4")
        }
    }
}


val version = "2.0.0"

compose.desktop {
    application {
        mainClass = "net.treset.treelauncher.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "TreeLauncher"
            packageVersion = version
            includeAllModules = true
            appResourcesRootDir = project.file("resources")
            vendor = "TreSet"

            windows {
                iconFile = project.file("icon_default.ico")
                menu = true
                perUserInstall = true
                upgradeUuid = "d7cd48ff-3946-4744-b772-dfcdbff7d4f2"
            }
        }

        jvmArgs("--add-opens", "java.desktop/sun.awt=ALL-UNNAMED")
        jvmArgs("--add-opens", "java.desktop/java.awt.peer=ALL-UNNAMED")

        if (System.getProperty("os.name").contains("Mac")) {
            jvmArgs("--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED")
            jvmArgs("--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED")
        }

        buildTypes.release.proguard {
            configurationFiles.from("compose-desktop.pro")
        }
    }
}

task("replaceVersion") {
    val file = project.file("src/commonMain/kotlin/net/treset/treelauncher/localization/Strings.kt")

    var found = false

    val lines = file.readLines()
    println(lines)

    file.writeText(
        lines.joinToString(System.lineSeparator()) { line ->
            val match = "(?<=val version: \\(\\) -> String = \\{ \\\")(.*)(?=\\\" \\})".toRegex().find(line)
            match?.let {
                println(it.value)
                found = true
                line.replace(it.value, version)
            } ?: line
        }
    )

    if(!found) {
        throw IllegalStateException("Could not find version string in Strings.kt")
    }
}