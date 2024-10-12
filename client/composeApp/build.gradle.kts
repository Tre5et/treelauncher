
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

plugins {
    alias(libs.plugins.kotlinMultiplatform)

    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
        }

        val mcdlModules = listOf(
            "assets",
            "auth",
            "fabric",
            "forge",
            "java",
            "minecraft",
            "mods",
            "quiltmc",
            "resourcepacks",
            "saves"
        )
        val mcdlVersion = "2.0.0"
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)

            implementation("com.google.code.gson:gson:2.10.1")

            implementation("dev.treset.mcdl:mcdl:$mcdlVersion")
            mcdlModules.forEach {
                implementation("dev.treset.mcdl:mcdl-$it:$mcdlVersion")
            }

            implementation("io.github.oshai:kotlin-logging:6.0.3")
            implementation("io.github.oshai:kotlin-logging-jvm:6.0.3")
            implementation("org.slf4j:slf4j-api:2.0.12")
            implementation("ch.qos.logback:logback-classic:1.5.3")

            implementation("org.jetbrains.jewel:jewel-int-ui-standalone-241:0.19.5")
            implementation("org.jetbrains.jewel:jewel-int-ui-decorated-window-241:0.19.5")

            implementation("com.darkrockstudios:mpfilepicker:3.1.0")
            implementation("be.digitalia.compose.htmlconverter:htmlconverter:0.9.5")

            implementation("com.github.JnCrMx:discord-game-sdk4j:ae841453bd58af8279d0d43d8c65f997523f976d")
        }
    }
}


val version = "3.0.0"
val projectName = "TreeLauncher"
val projectVendor = "TreSet"
val resourcesDir = project.file("resources")
val iconIco = project.file("icon_default.ico")
val uuid = "d7cd48ff-3946-4744-b772-dfcdbff7d4f2"

compose.desktop {
    application {
        mainClass = "net.treset.treelauncher.MainKt"

        javaHome = "${System.getProperty("user.home")}\\.jdks\\jbr-17.0.11"

        nativeDistributions {
            modules("java.instrument", "java.naming", "java.net.http", "java.sql", "jdk.management", "jdk.unsupported")
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = projectName
            packageVersion = version
            appResourcesRootDir = resourcesDir
            vendor = projectVendor

            windows {
                iconFile = iconIco
                menu = true
                perUserInstall = true
                upgradeUuid = uuid
                dirChooser = true
            }
        }

        jvmArgs("--add-opens", "java.desktop/sun.awt=ALL-UNNAMED")
        jvmArgs("--add-opens", "java.desktop/java.awt.peer=ALL-UNNAMED")
        jvmArgs += "-splash:app/resources/splash.png"

        if (System.getProperty("os.name").contains("Mac")) {
            jvmArgs("--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED")
            jvmArgs("--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED")
        }

        buildTypes.release.proguard {
            isEnabled.set(true)
            configurationFiles.from(project.file("compose-desktop.pro"))
        }
    }
}

tasks {
    withType<JavaExec> {
        // afterEvaluate is needed because the Compose Gradle Plugin
        // register the task in the afterEvaluate block
        afterEvaluate {
            javaLauncher = project.javaToolchains.launcherFor {
                languageVersion = JavaLanguageVersion.of(17)
                vendor = JvmVendorSpec.JETBRAINS
            }
            setExecutable(javaLauncher.map { it.executablePath.asFile.absolutePath }.get())
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
    "createDist",
    listOf(
        "replaceVersion",
        "zipDist",
        "moveMsi",
        "makeUpdate"
    )
)

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
    val stringsFile = project.file("src/commonMain/kotlin/net/treset/treelauncher/localization/Strings.kt")
    var found = false
    val stringsLines = stringsFile.readLines()
    stringsFile.writeText(
        stringsLines.joinToString(System.lineSeparator()) { line ->
            val match = "(?<=val version: \\(\\) -> String = \\{ \\\")([0123456789.]*)(?=\\\" \\})".toRegex().find(line)
            match?.let { result ->
                found = true
                if(result.value != version) {
                    line.replace(result.value, version).also {
                        println("Replaced version in strings: ${result.value} -> $version")
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


    val configFile = project.file("src/commonMain/kotlin/net/treset/treelauncher/backend/config/Config.kt")
    found = false
    val configLines = configFile.readLines()
    configFile.writeText(
        configLines.joinToString(System.lineSeparator()) { line ->
            val match = "(?<=val modrinthUserAgent = \\\"TreSet/treelauncher/v)(.*)(?=\\\")".toRegex().find(line)
            match?.let { result ->
                found = true
                if(result.value != version) {
                    line.replace(result.value, version).also {
                        println("Replaced version in Config: ${result.value} -> $version")
                    }
                } else {
                    line
                }
            } ?: line
        }
    )
    if(!found) {
        throw IllegalStateException("Could not find version string in Config.kt")
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
    "makeUpdate",
    listOf("zipDist")
) {
    println("Calculating difference")

    println("Enter old distributable directory (enter to skip):")
    val oldPath = readln()

    val newDir = project.file("build/dist/${version}/$projectName-$version")
    if(newDir.exists()) {
        newDir.deleteRecursively()
    }
    newDir.mkdirs()
    ZipFile(project.file("build/dist/${version}/$projectName-$version.zip")).use { zip ->
        zip.entries().asSequence().forEach { entry ->
            zip.getInputStream(entry).use { input ->
                if (entry.isDirectory) {
                    val d = File(newDir, entry.name)
                    if (!d.exists()) d.mkdirs()
                } else {
                    val f = File(newDir, entry.name)
                    if (f.parentFile?.exists() != true) f.parentFile?.mkdirs()

                    f.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }

    if(oldPath.isEmpty()) {
        println("Skipping update generation")
    } else {
        val oldDir = File(oldPath)

        val result = scanDir(oldDir, newDir)

        val sb = StringBuilder("  {\n    \"id\": \"$version\",\n    \"changes\": [\n")
        for (deleted in result.first) {
            sb.append("      {\n")
            sb.append("        \"mode\": \"DELETE\",\n")
            sb.append("        \"path\": \"${deleted.replace("\\", "/")}\",\n")
            sb.append("        \"updater\": true\n")
            sb.append("      },\n")
        }
        project.file("build/dist/$version/update/latest/").deleteRecursively()
        for (added in result.second) {
            val file = newDir.resolve(added)
            if (file.isFile) {
                val toFile = project.file("build/dist/$version/update/latest/$added")
                toFile.parentFile.mkdirs()
                file.copyTo(toFile, true)
            }
            sb.append("      {\n")
            sb.append("        \"mode\": \"FILE\",\n")
            sb.append("        \"path\": \"${added.replace("\\", "/")}\",\n")
            sb.append("        \"updater\": true\n")
            sb.append("      },\n")
        }
        sb.deleteRange(sb.length - 2, sb.length)
        sb.append("\n    ]\n  }")

        project.file("build/dist/$version/update/difference.json").writeText(sb.toString())

        println("Wrote difference to build/dist/$version/update/difference.json")
    }
}

fun scanDir(old: File, new: File): Pair<Set<String>, Set<String>> {
    val oldFiles = mutableSetOf<String>()
    val newFiles = mutableSetOf<String>()

    old.walkBottomUp().forEach { file ->
        if(file.isFile) {
            val path = old.toPath().relativize(file.toPath()).toString()
            if (!path.startsWith("logs") && !path.startsWith("data")) {
                oldFiles.add(path)
            }
        }
    }

    new.walkBottomUp().forEach { file ->
        if(file.isFile) {
            val path = new.toPath().relativize(file.toPath()).toString()
            if (!path.startsWith("logs") && !path.startsWith("data")) {
                newFiles.add(path)
            }
        }
    }

    val deleted = oldFiles.minus(newFiles).toMutableSet()
    val added = newFiles.minus(oldFiles).toMutableSet()

    val common = oldFiles.intersect(newFiles)

    val md = MessageDigest.getInstance("MD5")

    for (path in common) {
        val oldFile = old.resolve(path)
        val newFile = new.resolve(path)

        if(!md.digest(oldFile.readBytes()).contentEquals(md.digest(newFile.readBytes()))) {
            added.add(path)
        }
    }

    val toRemoveAdded = mutableSetOf<String>()
    for (path in added) {
        val newFile = new.resolve(path)
        if(!newFile.isFile) {
            toRemoveAdded.add(path)
        }
    }
    added.removeAll(toRemoveAdded)

    val toRemoveDeleted = mutableSetOf<String>()
    for (path in deleted) {
        val oldFile = old.resolve(path)
        if(oldFile.isDirectory) {
            if(
                oldFile.walkTopDown().all {
                    deleted.contains(old.toPath().relativize(it.toPath()).toString())
                }
            ) {
                toRemoveDeleted.addAll(deleted.filter { it.startsWith(path) && it != path })
            }
        }
    }
    deleted.removeAll(toRemoveDeleted)

    return Pair(deleted, added)
}

class Update(
    var id: String?,
    var changes: List<Change>?,
    var message: String?,
    var latest: Boolean?
) {
    enum class Mode {
        FILE,
        DELETE,
        REGEX,
        LINE
    }

    class Change(
        var path: String,
        var mode: Mode,
        var elements: List<Element>,
        var updater: Boolean
    ) {
        class Element(
            var pattern: String,
            var value: String,
            var meta: String,
            var isReplace: Boolean
        )
    }
}
