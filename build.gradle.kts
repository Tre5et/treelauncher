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

val mcAuthenticatorVersion = "3.0.4"
val mcVersionLoaderVersion = "0.3.1"
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

dependencies {
    implementation("net.hycrafthd", "minecraft_authenticator", mcAuthenticatorVersion)
    implementation("net.treset", "mc-version-loader", mcVersionLoaderVersion)
    implementation("org.apache.logging.log4j", "log4j-api", log4jVersion)
    implementation("org.apache.logging.log4j", "log4j-core", log4jVersion)
    implementation("org.kordamp.ikonli", "ikonli-javafx", ikonliVersion)
    implementation("org.kordamp.ikonli", "ikonli-material2-pack", ikonliVersion)
    implementation("org.kordamp.ikonli", "ikonli-fontawesome5-pack", ikonliVersion)
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

task("copyResources", Copy::class) {
    from("./packageRes").into("$buildDir/jars")
}

task("copyJar", Copy::class) {
    dependsOn("shadowJar")
    from("./build/libs/minecraft-launcher-all.jar").into("$buildDir/jars")
}

tasks.jpackage {
    dependsOn("copyJar", "copyResources")

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
