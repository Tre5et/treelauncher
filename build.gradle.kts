import org.panteleyev.jpackage.ImageType

plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.13"
    id ("com.github.johnrengelman.shadow") version "7.1.0"
    id ("org.panteleyev.jpackageplugin") version "1.5.2"
}

val group = "net.treset"
val version = "0.4.0"
val mainClassName = "net.treset.minecraftlauncher.Main"

val mcAuthenticatorVersion = "3.0.5"
val mcVersionLoaderVersion = "1.2.1"
val log4jVersion = "2.20.0"
val ikonliVersion = "12.3.1"

checkVersion()

fun checkVersion() {
    val stringFile= File("src/main/resources/lang/strings.properties")
    val content = stringFile.readLines()
    var matches = false
    for(line in content) {
        if (line.startsWith("launcher.version=")) {
            if (line.substringAfter("launcher.version=").startsWith(version)) {
                matches = true
                break
            }
        }
    }
    if(!matches) {
        throw Exception("Version in strings.properties does not match version in build.gradle.kts")
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.u-team.info")
    }
    maven {
        url = uri("https://raw.githubusercontent.com/Tre5et/maven/main/")
    }
}

val ikonliPacks = listOf(
    "ikonli-bootstrapicons-pack"
)

dependencies {
    implementation("net.hycrafthd", "minecraft_authenticator", mcAuthenticatorVersion)
    implementation("net.treset", "mc-version-loader", mcVersionLoaderVersion)
    implementation("org.apache.logging.log4j", "log4j-api", log4jVersion)
    implementation("org.apache.logging.log4j", "log4j-core", log4jVersion)
    implementation("org.kordamp.ikonli", "ikonli-javafx", ikonliVersion)
    for (pack in ikonliPacks) {
        implementation("org.kordamp.ikonli", pack, ikonliVersion)
    }
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = mainClassName
    }
}

application {
    mainClass.set(mainClassName)
}


javafx {
    version = "17"
    modules("javafx.controls", "javafx.fxml", "javafx.web")
}

val javaLocation = System.getProperty("java.home")

val buildDir = "./build/libs"
val jpackageDir = "./jpackage"

task("copyJar", Copy::class) {
    dependsOn("shadowJar")
    from("$buildDir/minecraft-launcher-all.jar").into("$jpackageDir/jars")
    from("./app/updater.jar").into("$jpackageDir/jars")
}

tasks.jpackage {
    dependsOn("copyJar")

    input = "$jpackageDir/jars"
    destination = "$jpackageDir/dist"

    appName = "TreeLauncher"
    appVersion = version
    vendor = "treset"
    runtimeImage = javaLocation
    mainJar = "minecraft-launcher-all.jar"
    icon = "src/main/resources/img/icon.ico"
    mainClass = mainClassName

    javaOptions = listOf("-Dfile.encoding=UTF-8")

    windows {
        type = ImageType.MSI
        winMenu = true
        winShortcutPrompt = true
        winDirChooser = true
        winPerUserInstall = true
        winUpgradeUuid = "d7cd48ff-3946-4744-b772-dfcdbff7d4f2"
    }
}

val distDir = "./dist"

task("createUpdateFiles", Exec::class) {
    val files = File("versionManifests").listFiles()
    if(files == null) {
        throw Exception("No versionManifests found")
    }
    var matches = false
    for(file in files) {
        if(file.name.substringBeforeLast(".json").equals(version)) {
            matches = true
            break
        }
    }
    if(!matches) {
        throw Exception("No versionManifest for current version found")
    }
    commandLine("updateConverter\\node", "updateConverter\\index.js")
}

task("cleanDist", Delete::class) {
    delete("$distDir/res")
}

task("copyAppData", Copy::class) {
    dependsOn("shadowJar")
    from("$buildDir/minecraft-launcher-all.jar", "./app/updater.jar").into("$distDir/res/app")
}

task("copyRuntime", Copy::class) {
    from(javaLocation).into("$distDir/res/runtime")
}

task("copyDistRes", Copy::class) {
    dependsOn("cleanDist", "copyAppData", "copyRuntime")
    from("distRes/run.bat").into("$distDir/res")
}

task("copyJpackage", Copy::class) {
    dependsOn("jpackage")
    from("$jpackageDir/dist/TreeLauncher-$version.msi").into("$distDir/$version")
}

task("copyShadowJar", Copy::class) {
    dependsOn("shadowJar")
    from("$buildDir/minecraft-launcher-all.jar").into("$distDir/$version")
    rename("minecraft-launcher-all.jar", "TreeLauncher-$version.jar")
}

task("createDist", Zip::class) {
    dependsOn("createUpdateFiles", "copyDistRes", "copyJpackage", "copyShadowJar")
    archiveFileName.set("TreeLauncher-$version.zip")
    destinationDirectory.set(file("$distDir/$version"))

    from("$distDir/res")
}
