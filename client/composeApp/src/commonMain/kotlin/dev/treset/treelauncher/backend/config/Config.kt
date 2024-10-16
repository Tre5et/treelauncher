package dev.treset.treelauncher.backend.config

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
    val dataVersion: Version = Version(2, 0, 0)
    var baseDir: LauncherFile = LauncherFile.of(globalConfig.path)
    val syncFileName = "data.sync"
    val metaDir: LauncherFile = LauncherFile.of(baseDir, ".launcher")
    val tokenFile: LauncherFile = LauncherFile.of(metaDir, "tokens.json")
    val settingsFile: LauncherFile = LauncherFile.of(".launcher", "settings.json")
    val manifestFileName = "manifest.json"
    val nativesDirName = "natives"
    val includedFilesBackupDir = ".included_files.bak"
    val instanceDefaultFeatures: Array<LauncherFeature> = arrayOf()
    val instanceDefaultIncludedFiles: Array<String> = arrayOf()
    val instanceDefaultIgnoredFiles: Array<String> = arrayOf()
    val instanceDefaultJvmArguments: Array<LauncherLaunchArgument> = arrayOf()
    val optionsDefaultIncludedFiles: Array<String> = arrayOf(
        "options.txt",
        "usercache.json"
    )
    val modsDefaultIncludedFiles: Array<String> = arrayOf(
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
    val savesDefaultIncludedFiles: Array<String> = arrayOf(
            "saves/",
            "servers.dat",
            "realms_persistence.json",
            "stats/",
            "backups/",
        )
    val resourcepacksDefaultIncludedFiles: Array<String> = arrayOf(
            "resourcepacks/",
            "texturepacks/"
        )
    val javaDefaultIncludedFiles: Array<String> = arrayOf()
    val versionDefaultIncludedFiles: Array<String> = arrayOf()
    val minecraftDefaultGameArguments: Array<LauncherLaunchArgument> = arrayOf()
    val minecraftDefaultFileName = "client.jar"
    val minecraftDefaultJvmArguments: Array<LauncherLaunchArgument> = arrayOf(
        LauncherLaunchArgument("-Djava.library.path=\${natives_directory}"),
        LauncherLaunchArgument("-cp"),
        LauncherLaunchArgument("\${classpath}"),
    )
    val fabricDefaultGameArguments: Array<LauncherLaunchArgument> = arrayOf()
    val fabricDefaultJvmArguments: Array<LauncherLaunchArgument> = arrayOf()
    val forgeDefaultGameArguments: Array<LauncherLaunchArgument> = arrayOf()
    val forgeDefaultJvmArguments: Array<LauncherLaunchArgument> = arrayOf()
    val quiltDefaultGameArguments: Array<LauncherLaunchArgument> = arrayOf()
    val quiltDefaultJvmArguments: Array<LauncherLaunchArgument> = arrayOf()
    val fabricDefaultClientFileName = "fabric-client.jar"
    val modrinthUserAgent = "TreSet/treelauncher/v3.0.0"
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