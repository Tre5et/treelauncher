package net.treset.treelauncher.backend.config

import net.treset.mc_version_loader.launcher.LauncherFeature
import net.treset.mc_version_loader.launcher.LauncherLaunchArgument
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.backend.util.string.PatternString

class Config(BASE_DIR: String, val DEBUG: Boolean, val UPDATE_URL: String) {
    var BASE_DIR: LauncherFile
    val SYNC_FILENAME = "data.sync"
    val META_DIR: LauncherFile
    val LOG_DIR: LauncherFile
    val AUTH_FILE: LauncherFile
    val SETTINGS_FILE_NAME: LauncherFile = LauncherFile.of(".launcher", "settings.json")
    val MANIFEST_FILE_NAME = "manifest.json"
    val INCLUDED_FILES_DIR = ".included_files"
    val INSTANCE_DEFAULT_FEATURES: Array<LauncherFeature> = arrayOf()
    val INSTANCE_DEFAULT_INCLUDED_FILES: Array<PatternString> = arrayOf()
    val INSTANCE_DEFAULT_IGNORED_FILES: Array<PatternString> = PatternString.toPattern(".*backup.*", ".*BACKUP.*")
    val INSTANCE_DEFAULT_JVM_ARGUMENTS: Array<LauncherLaunchArgument> = arrayOf()
    val INSTANCE_DEFAULT_DETAILS = "instance.json"
    val OPTIONS_DEFAULT_INCLUDED_FILES: Array<PatternString> = PatternString.toPattern("options.txt", "usercache.json")
    val MODS_DEFAULT_INCLUDED_FILES: Array<PatternString> = PatternString.toPattern(
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
        "irisUpdateInfo.json",
        "optionsviveprofiles.txt",
        "g4mespeed"
    )
    val MODS_DEFAULT_DETAILS = "mods.json"
    val SAVES_DEFAULT_INCLUDED_FILES: Array<PatternString> =
        PatternString.toPattern("servers.dat", "realms_persistence.json")
    val RESOURCEPACK_DEFAULT_INCLUDED_FILES: Array<PatternString> = arrayOf()
    val VERSION_DEFAULT_DETAILS = "version.json"
    val MINECRAFT_DEFAULT_GAME_ARGUMENTS: Array<LauncherLaunchArgument> = arrayOf(
        LauncherLaunchArgument("--resourcePackDir", null, null, null, null),
        LauncherLaunchArgument("\${resourcepack_directory}", null, null, null, null)
    )
    val MINECRAFT_DEFAULT_JVM_ARGUMENTS: Array<LauncherLaunchArgument> = arrayOf()
    val FABRIC_DEFAULT_GAME_ARGUMENTS: Array<LauncherLaunchArgument> = arrayOf()
    val FABRIC_DEFAULT_JVM_ARGUMENTS: Array<LauncherLaunchArgument> = arrayOf()
    val FABRIC_DEFAULT_CLIENT_FILENAME = "fabric-client.jar"
    val MODRINTH_USER_AGENT = "TreSet/treelauncher/v1.0.0"
    val CURSEFORGE_API_KEY = "$2a$10$3rdQBL3FRS2RSSS4MF5F5uuOQpFr5flAzUCAdBvZDEfu1fIXFq.DW"

    init {
        this.BASE_DIR = LauncherFile.of(BASE_DIR)
        META_DIR = LauncherFile.of(BASE_DIR, ".launcher")
        AUTH_FILE = LauncherFile.of(META_DIR, "secrets.auth")
        LOG_DIR = LauncherFile.of(META_DIR, "logs")
    }
}

private lateinit var config: Config
fun appConfig(): Config = config
fun setAppConfig(newConfig: Config) {
    config = newConfig
}
