import org.panteleyev.jpackage.ImageType

plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.13"
    id ("com.github.johnrengelman.shadow") version "7.1.0"
    id ("org.panteleyev.jpackageplugin") version "1.5.2"
}

val group = "net.treset"
val version = "0.1.0"
val mainClassName = "net.treset.minecraftlauncher.Main"

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.u-team.info")
    }
    maven {
        url = uri("https://raw.githubusercontent.com/Tre5et/maven/main/")
    }
}

dependencies {
    implementation("net.hycrafthd:minecraft_authenticator:3.0.4")
    implementation("net.treset:mc-version-loader:0.2.6")
    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation("org.kordamp.ikonli:ikonli-javafx:12.3.1")
    implementation("org.kordamp.ikonli:ikonli-material2-pack:12.3.1")
    implementation("org.kordamp.ikonli:ikonli-fontawesome5-pack:12.3.1")
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

task("copyDependencies", Copy::class) {
    from("./packageRes").into("$buildDir/jars")
}

task("copyJar", Copy::class) {
    dependsOn("shadowJar")
    from("./build/libs/minecraft-launcher-all.jar").into("$buildDir/jars")
}

tasks.jpackage {
    dependsOn("copyJar", "copyDependencies")

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
    }
}