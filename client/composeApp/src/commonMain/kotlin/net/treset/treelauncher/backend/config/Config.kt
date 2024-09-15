package net.treset.treelauncher.backend.config

import net.treset.treelauncher.backend.data.LauncherFeature
import net.treset.treelauncher.backend.data.LauncherLaunchArgument
import net.treset.treelauncher.backend.util.Version
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.backend.util.string.PatternString

class Config(baseDir: String, val updateUrl: String?) {
    val dataVersion: Version = Version(2, 0, 0)
    var baseDir: LauncherFile = LauncherFile.of(baseDir)
    val syncFileName = "data.sync"
    val metaDir: LauncherFile = LauncherFile.of(baseDir, ".launcher")
    val tokenFile: LauncherFile = LauncherFile.of(metaDir, "tokens.json")
    val settingsFile: LauncherFile = LauncherFile.of(".launcher", "settings.json")
    val manifestFileName = "manifest.json"
    val nativesDirName = "natives"
    val includedFilesBackupDir = ".included_files.bak"
    val instanceDefaultFeatures: Array<LauncherFeature> = arrayOf()
    val instanceDefaultIncludedFiles: Array<PatternString> = arrayOf()
    val instanceDefaultIgnoredFiles: Array<PatternString> = arrayOf()
    val instanceDefaultJvmArguments: Array<LauncherLaunchArgument> = arrayOf()
    val instanceDefaultDetails = "instance.json"
    val optionsDefaultIncludedFiles: Array<PatternString> = PatternString.toPattern(
        "options.txt",
        "usercache.json"
    )
    val modsDefaultIncludedFiles: Array<PatternString> = PatternString.toPattern(
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
    val modsDefaultDetails = "mods.json"
    val savesDefaultIncludedFiles: Array<PatternString> = PatternString.toPattern(
            "saves/",
            "servers.dat",
            "realms_persistence.json",
            "stats/",
            "backups/",
        )
    val resourcepacksDefaultIncludedFiles: Array<PatternString> = PatternString.toPattern(
            "resourcepacks/",
            "texturepacks/"
        )
    val versionsDefaultDetails = "version.json"
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
    val fabricDefaultClientFileName = "fabric-client.jar"
    val modrinthUserAgent = "TreSet/treelauncher/v2.6.0"
    val curseforgeApiKey = "$2a$10$3rdQBL3FRS2RSSS4MF5F5uuOQpFr5flAzUCAdBvZDEfu1fIXFq.DW"
    val msClientId = "389304a5-70a6-4013-907f-98c4eb4b51fb"
}

private lateinit var config: Config
fun appConfig(): Config = config
fun setAppConfig(newConfig: Config) {
    config = newConfig
}