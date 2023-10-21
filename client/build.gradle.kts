import org.panteleyev.jpackage.ImageType

plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.13"
    id ("com.github.johnrengelman.shadow") version "7.1.0"
    id ("org.panteleyev.jpackageplugin") version "1.5.2"
}

val group = "net.treset"
val version = "0.7.2"
val mainClassName = "net.treset.minecraftlauncher.Main"

val mcAuthenticatorVersion = "3.0.5"
val mcVersionLoaderVersion = "1.4.0"
val log4jVersion = "2.20.0"
val ikonliVersion = "12.3.1"

updateVersion()

fun updateVersion() {
    val stringFile= File("src/main/resources/lang/strings.properties")
    val content = stringFile.readLines()
    var matches = false
    var newContent = ""
    for(line in content) {
        if (line.startsWith("launcher.version=")) {
            matches = true
            newContent += "launcher.version=$version\n"
        } else {
            newContent += line + "\n"
        }
    }
    if(!matches) {
        throw Exception("Version in strings.properties not found")
    }
    stringFile.writeText(newContent)

    val configFile = File("src/main/java/net/treset/minecraftlauncher/config/Config.java")
    val configContent = configFile.readLines()
    var configMatches = false
    var newConfigContent = ""
    for(line in configContent) {
        if (line.contains("MODRINTH_USER_AGENT")) {
            configMatches = true
            newConfigContent += "    public final String MODRINTH_USER_AGENT = \"TreSet/treelauncher/v$version\";\n"
        } else {
            newConfigContent += line + "\n"
        }
    }
    if(!configMatches) {
        throw Exception("Version in Config.java not found")
    }
    configFile.writeText(newConfigContent)
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

val javaHome = System.getProperty("java.home")

val buildDir = "./build/libs"
val jpackageDir = "./jpackage"
val distDir = "./dist"

task("copyJar", Copy::class) {
    dependsOn("shadowJar")
    from("$buildDir/treelauncher-client-all.jar").into("$jpackageDir/jars")
    from("./app/updater.jar").into("$jpackageDir/jars")
}

tasks.jpackage {
    dependsOn("copyJar", "makeRuntime")

    input = "$jpackageDir/jars"
    destination = "$jpackageDir/dist"

    appName = "TreeLauncher"
    appVersion = version
    vendor = "treset"
    runtimeImage = "$distDir/res/runtime"
    mainJar = "treelauncher-client-all.jar"
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

task("makeRuntime", Exec::class) {
    commandLine("$javaHome\\bin\\jlink", "--module-path", "$javaHome\\jmods", "--add-modules", "ALL-MODULE-PATH", "--output", "$distDir\\res\\runtime")
}

task("cleanDist", Delete::class) {
    delete("$distDir/res")
}

task("copyAppData", Copy::class) {
    dependsOn("shadowJar")
    from("$buildDir/treelauncher-client-all.jar", "./app/updater.jar").into("$distDir/res/app")
}

task("copyDistRes", Copy::class) {
    dependsOn("cleanDist", "copyAppData", "makeRuntime")
    from("distRes/run.bat").into("$distDir/res")
}

task("copyJpackage", Copy::class) {
    dependsOn("jpackage")
    from("$jpackageDir/dist/TreeLauncher-$version.msi").into("$distDir/$version")
}

task("copyShadowJar", Copy::class) {
    dependsOn("shadowJar")
    from("$buildDir/treelauncher-client-all.jar").into("$distDir/$version")
    rename("treelauncher-client-all.jar", "TreeLauncher-$version.jar")
}

task("createDist", Zip::class) {
    dependsOn("createUpdateFiles", "copyDistRes", "copyJpackage", "copyShadowJar")
    archiveFileName.set("TreeLauncher-$version.zip")
    destinationDirectory.set(file("$distDir/$version"))

    from("$distDir/res")
}
