package net.treset.treelauncher.backend.config

import net.treset.mc_version_loader.launcher.LauncherFeature
import net.treset.mc_version_loader.launcher.LauncherLaunchArgument
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.backend.util.string.PatternString

class Config(baseDir: String, val updateUrl: String?) {
    var baseDir: LauncherFile = LauncherFile.of(baseDir)
    val syncFileName = "data.sync"
    val metaDir: LauncherFile = LauncherFile.of(baseDir, ".launcher")
    val authFile: LauncherFile = LauncherFile.of(metaDir, "secrets.auth")
    val settingsFile: LauncherFile = LauncherFile.of(".launcher", "settings.json")
    val manifestFileName = "manifest.json"
    val includedFilesDirName = ".included_files"
    val instanceDefaultFeatures: Array<LauncherFeature> = arrayOf()
    val instanceDefaultIncludedFiles: Array<PatternString> = arrayOf()
    val instanceDefaultIgnoredFiles: Array<PatternString> = PatternString.toPattern(".*backup.*", ".*BACKUP.*")
    val instanceDefaultJvmArguments: Array<LauncherLaunchArgument> = arrayOf()
    val instanceDefaultDetails = "instance.json"
    val optionsDefaultIncludedFiles: Array<PatternString> = PatternString.toPattern("options.txt", "usercache.json")
    val modsDefaultIncludedFiles: Array<PatternString> = PatternString.toPattern(
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
    val savesDefaultIncludedFiles: Array<PatternString> =
        PatternString.toPattern("servers.dat", "realms_persistence.json")
    val resourcepacksDefaultIncludedFiles: Array<PatternString> = arrayOf()
    val versionsDefaultDetails = "version.json"
    val minecraftDefaultGameArguments: Array<LauncherLaunchArgument> = arrayOf(
        LauncherLaunchArgument("--resourcePackDir", null, null, null, null),
        LauncherLaunchArgument("\${resourcepack_directory}", null, null, null, null)
    )
    val minecraftDefaultFileName = "client.jar"
    val minecraftDefaultJvmArguments: Array<LauncherLaunchArgument> = arrayOf()
    val fabricDefaultGameArguments: Array<LauncherLaunchArgument> = arrayOf()
    val fabricDefaultJvmArguments: Array<LauncherLaunchArgument> = arrayOf()
    val forgeDefaultGameArguments: Array<LauncherLaunchArgument> = arrayOf()
    val forgeDefaultJvmArguments: Array<LauncherLaunchArgument> = arrayOf()
    val fabricDefaultClientFileName = "fabric-client.jar"
    val modrinthUserAgent = "TreSet/treelauncher/v2.2.4"
    val curseforgeApiKey = "$2a$10$3rdQBL3FRS2RSSS4MF5F5uuOQpFr5flAzUCAdBvZDEfu1fIXFq.DW"
}

private lateinit var config: Config
fun appConfig(): Config = config
fun setAppConfig(newConfig: Config) {
    config = newConfig
}