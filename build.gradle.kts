import org.panteleyev.jpackage.ImageType

plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.13"
    id ("com.github.johnrengelman.shadow") version "7.1.0"
    id ("org.panteleyev.jpackageplugin") version "1.5.2"
}

val group = "net.treset"
val version = "0.2.0"
val mainClassName = "net.treset.minecraftlauncher.Main"

val mcAuthenticatorVersion = "3.0.5"
val mcVersionLoaderVersion = "0.3.3"
val log4jVersion = "2.20.0"
val ikonliVersion = "12.3.1"

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



val buildDir = "./jpackage"

task("copyJar", Copy::class) {
    dependsOn("shadowJar")
    from("./build/libs/minecraft-launcher-all.jar").into("$buildDir/jars")
}

tasks.jpackage {
    dependsOn("copyJar")

    input = "$buildDir/jars"
    destination = "$buildDir/dist"

    appName = "TreeLauncher"
    appVersion = version
    vendor = "treset"
    runtimeImage = System.getProperty("java.home")
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
