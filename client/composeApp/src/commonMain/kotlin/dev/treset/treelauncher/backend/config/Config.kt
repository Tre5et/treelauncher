package dev.treset.treelauncher.backend.config

import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.app
import dev.treset.treelauncher.backend.data.LauncherFeature
import dev.treset.treelauncher.backend.data.LauncherLaunchArgument
import dev.treset.treelauncher.backend.util.Version
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.util.configFile
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.IOException
import java.nio.file.StandardCopyOption
import kotlin.jvm.Throws

class Config(private val globalConfig: GlobalConfig, val updateUrl: String? = null) {
    val launcherVersion = Version(3,0,3)
    val dataVersion = Version(2,0,0)
    var baseDir: LauncherFile = LauncherFile.of(globalConfig.path)
    val syncFileName = "data.sync"
    val metaDir: LauncherFile = LauncherFile.of(baseDir, ".launcher")
    val tokenFile: LauncherFile = LauncherFile.of(metaDir, "tokens.json")
    val settingsFile: LauncherFile = LauncherFile.of(".launcher", "settings.json")
    val manifestFileName = "manifest.json"
    val nativesDirName = "natives"
    val includedFilesBackupDir = ".included_files.bak"
    val instanceDefaultFeatures: List<LauncherFeature> = listOf()
    val instanceDefaultIncludedFiles: List<String> = listOf()
    val instanceDefaultIgnoredFiles: List<String> = listOf()
    val instanceDefaultJvmArguments: List<LauncherLaunchArgument> = listOf()
    val optionsDefaultIncludedFiles: List<String> = listOf(
        "options.txt",
        "usercache.json"
    )
    val modsDefaultIncludedFiles: List<String> = listOf(
        "mods/",
        ".fabric/",
        "config/",
        "schematics/",
        "syncmatics/",
        "shaderpacks/",
        "data/",
        ".bobby/",
        "XaeroWorldMap/",
        "XaeroWaypoints/",
        "itemscroller/",
        "baritone/",
        "irisUpdateInfo.json",
        "optionsviveprofiles.txt",
        "g4mespeed"
    )
    val savesDefaultIncludedFiles: List<String> = listOf(
            "saves/",
            "servers.dat",
            "realms_persistence.json",
            "stats/",
            "backups/",
        )
    val resourcepacksDefaultIncludedFiles: List<String> = listOf(
            "resourcepacks/",
            "texturepacks/"
        )
    val javaDefaultIncludedFiles: List<String> = listOf()
    val versionDefaultIncludedFiles: List<String> = listOf()
    val minecraftDefaultGameArguments: List<LauncherLaunchArgument> = listOf()
    val minecraftDefaultFileName = "client.jar"
    val minecraftDefaultJvmArguments: List<LauncherLaunchArgument> = listOf(
        LauncherLaunchArgument("-Djava.library.path=\${natives_directory}"),
        LauncherLaunchArgument("-cp"),
        LauncherLaunchArgument("\${classpath}"),
    )
    val fabricDefaultGameArguments: List<LauncherLaunchArgument> = listOf()
    val fabricDefaultJvmArguments: List<LauncherLaunchArgument> = listOf()
    val forgeDefaultGameArguments: List<LauncherLaunchArgument> = listOf()
    val forgeDefaultJvmArguments: List<LauncherLaunchArgument> = listOf()
    val neoForgeDefaultGameArguments: List<LauncherLaunchArgument> = listOf()
    val neoForgeDefaultJvmArguments: List<LauncherLaunchArgument> = listOf(
        //TODO ?
        LauncherLaunchArgument("-XX:+UnlockExperimentalVMOptions")
    )
    val quiltDefaultGameArguments: List<LauncherLaunchArgument> = listOf()
    val quiltDefaultJvmArguments: List<LauncherLaunchArgument> = listOf()
    val fabricDefaultClientFileName = "fabric-client.jar"
    val modrinthUserAgent = "TreSet/treelauncher/$launcherVersion"
    val curseforgeApiKey = "$2a$10$3rdQBL3FRS2RSSS4MF5F5uuOQpFr5flAzUCAdBvZDEfu1fIXFq.DW"
    val msClientId = "389304a5-70a6-4013-907f-98c4eb4b51fb"

    @Throws(IOException::class)
    fun setBaseDir(newBaseDir: LauncherFile, copyFiles: Boolean, removeOld: Boolean) {
        LOGGER.info { "Updating path: path=${newBaseDir.absolutePath}" }

        try {
            GlobalConfig.validateDataPath(
                newBaseDir,
                appConfig().baseDir,
                !copyFiles
            )
        } catch (e: IllegalStateException) {
            throw IOException(e)
        }

        if(copyFiles) {
            if(removeOld) {
                LOGGER.info { "Moving files from ${appConfig().baseDir.absolutePath} to ${newBaseDir.absolutePath}..." }
                appConfig().baseDir.atomicMoveTo(newBaseDir, StandardCopyOption.REPLACE_EXISTING)
            } else {
                LOGGER.info { "Copying files from ${appConfig().baseDir.absolutePath} to ${newBaseDir.absolutePath}..." }
                appConfig().baseDir.copyTo(newBaseDir, StandardCopyOption.REPLACE_EXISTING)
            }
        }

        LOGGER.debug { "Updating config" }
        val oldDir = appConfig().baseDir
        baseDir = newBaseDir
        globalConfig.path = newBaseDir.absolutePath
        globalConfig.writeToFile(configFile.absolutePath)

        app().loadSettings()
        AppContext.recheckData()

        if (removeOld) {
            LOGGER.debug { "Removing old directory" }
            oldDir.remove()
        }
        LOGGER.info { "Successfully changed directory" }
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}

private lateinit var config: Config
fun appConfig(): Config = config
fun setAppConfig(newConfig: Config) {
    config = newConfig
}