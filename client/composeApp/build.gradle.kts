
import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

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
val projectName = "TreeLauncher"
val projectVendor = "TreSet"
val resourcesDir = project.file("resources")
val iconIco = project.file("icon_default.ico")
val uuid = "d7cd48ff-3946-4744-b772-dfcdbff7d4f2"

compose.desktop {
    application {
        mainClass = "net.treset.treelauncher.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = projectName
            packageVersion = version
            includeAllModules = true
            appResourcesRootDir = resourcesDir
            vendor = projectVendor

            windows {
                iconFile = iconIco
                menu = true
                perUserInstall = true
                upgradeUuid = uuid
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

fun launcherTask(
    name: String,
    dependencies: List<String> = listOf(),
    onRegister: Task.() -> Unit = {},
    onExecute: Task.() -> Unit = {}
) {
    tasks.register(name) {
        group = "treelauncher"
        dependencies.forEach { dependsOn(it) }

        onRegister()

        doLast {
            onExecute()
        }
    }
}

launcherTask(
    "replaceVersion",
    onRegister = {
        var found = false
        project.tasks.forEach { task ->
            if(task.name == "checkRuntime") {
                task.mustRunAfter(this)
                found = true
            }
        }
        if(!found) {
            throw IllegalStateException("Could not find checkRuntime task to run replaceVersion before")
        }
    }
) {
    val file = project.file("src/commonMain/kotlin/net/treset/treelauncher/localization/Strings.kt")

    var found = false

    val lines = file.readLines()

    file.writeText(
        lines.joinToString(System.lineSeparator()) { line ->
            val match = "(?<=val version: \\(\\) -> String = \\{ \\\")(.*)(?=\\\" \\})".toRegex().find(line)
            match?.let { result ->
                found = true
                if(result.value != version) {
                    line.replace(result.value, version).also {
                        println("Replaced version: ${result.value} -> $version")
                    }
                } else {
                    line
                }
            } ?: line
        }
    )

    if(!found) {
        throw IllegalStateException("Could not find version string in Strings.kt")
    }
}

launcherTask(
    "zipDist",
    listOf("createDistributable")
) {
    val folder = project.file("build/compose/binaries/main/app/$projectName")
    val zipFile = project.file("build/dist/${version}/$projectName-$version.zip")

    println("Zipping dist: ${folder.absolutePath} -> ${zipFile.absolutePath}")

    zipFile.parentFile.mkdirs()
    if(zipFile.exists()) {
        zipFile.delete()
    }
    ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
        folder.walkTopDown().forEach { file ->
            val zipFileName = file.absolutePath.removePrefix(folder.absolutePath).removePrefix("/")
            val entry = ZipEntry("$zipFileName${(if (file.isDirectory) "/" else "" )}")
            zos.putNextEntry(entry)
            if (file.isFile) {
                file.inputStream().use { fis -> fis.copyTo(zos) }
            }
        }
    }

    println("Zipped dist")
}

launcherTask(
    "moveMsi",
    listOf("packageMsi")
) {
    val src = project.file("build/compose/binaries/main/msi/$projectName-$version.msi")
    val target = project.file("build/dist/$version/$projectName-$version.msi")

    println("Moving msi: ${src.absolutePath} -> ${target.absolutePath}")

    target.parentFile.mkdirs()
    src.copyTo(target, true)

    println("Moved msi")
}

launcherTask(
    "createDist",
    listOf(
        "replaceVersion",
        "zipDist",
        "moveMsi"
    )
)
