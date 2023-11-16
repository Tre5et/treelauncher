package net.treset.minecraftlauncher.config;

import net.treset.mc_version_loader.launcher.LauncherFeature;
import net.treset.mc_version_loader.launcher.LauncherLaunchArgument;
import net.treset.minecraftlauncher.util.file.LauncherFile;
import net.treset.minecraftlauncher.util.string.PatternString;

import java.util.List;

public class Config {
    public LauncherFile BASE_DIR;
    public final String SYNC_FILENAME = "data.sync";
    public final LauncherFile META_DIR;
    public final LauncherFile LOG_DIR;
    public final LauncherFile AUTH_FILE;
    public final LauncherFile SETTINGS_FILE_NAME = LauncherFile.of(".launcher", "settings.json");
    public final String MANIFEST_FILE_NAME = "manifest.json";
    public final String INCLUDED_FILES_DIR = ".included_files";
    public final List<LauncherFeature> INSTANCE_DEFAULT_FEATURES = List.of();
    public final List<PatternString> INSTANCE_DEFAULT_INCLUDED_FILES = List.of();
    public final List<PatternString> INSTANCE_DEFAULT_IGNORED_FILES = PatternString.toPattern(".*backup.*", ".*BACKUP.*");
    public final List<LauncherLaunchArgument> INSTANCE_DEFAULT_JVM_ARGUMENTS = List.of();
    public final String INSTANCE_DEFAULT_DETAILS = "instance.json";
    public final List<PatternString> OPTIONS_DEFAULT_INCLUDED_FILES = PatternString.toPattern("options.txt", "usercache.json");
    public final List<PatternString> MODS_DEFAULT_INCLUDED_FILES = PatternString.toPattern(".fabric/", "config/", "schematics/", "syncmatics/", "shaderpacks/", "data/", ".bobby/", "XaeroWorldMap/", "XaeroWaypoints/", "itemscroller/", "irisUpdateInfo.json", "optionsviveprofiles.txt", "g4mespeed");
    public final String MODS_DEFAULT_DETAILS = "mods.json";
    public final List<PatternString> SAVES_DEFAULT_INCLUDED_FILES = PatternString.toPattern("servers.dat", "realms_persistence.json");
    public final List<PatternString> RESOURCEPACK_DEFAULT_INCLUDED_FILES = List.of();
    public final String VERSION_DEFAULT_DETAILS = "version.json";
    public final List<LauncherLaunchArgument> MINECRAFT_DEFAULT_GAME_ARGUMENTS = List.of(new LauncherLaunchArgument("--resourcePackDir", null, null, null, null), new LauncherLaunchArgument("${resourcepack_directory}", null, null, null, null));
    public final List<LauncherLaunchArgument> MINECRAFT_DEFAULT_JVM_ARGUMENTS = List.of();
    public final List<LauncherLaunchArgument> FABRIC_DEFAULT_GAME_ARGUMENTS = List.of();
    public final List<LauncherLaunchArgument> FABRIC_DEFAULT_JVM_ARGUMENTS = List.of();
    public final String FABRIC_DEFAULT_CLIENT_FILENAME = "fabric-client.jar";
    public final String MODRINTH_USER_AGENT = "TreSet/treelauncher/v1.0.0";
    public final String CURSEFORGE_API_KEY = "$2a$10$3rdQBL3FRS2RSSS4MF5F5uuOQpFr5flAzUCAdBvZDEfu1fIXFq.DW";
    public final boolean DEBUG;
    public final String UPDATE_URL;

    public Config(String BASE_DIR, boolean DEBUG, String updateUrl) {
        this.BASE_DIR = LauncherFile.of(BASE_DIR);
        META_DIR = LauncherFile.of(BASE_DIR, ".launcher");
        AUTH_FILE = LauncherFile.of(META_DIR, "secrets.auth");
        LOG_DIR = LauncherFile.of(META_DIR, "logs");

        this.DEBUG = DEBUG;
        this.UPDATE_URL = updateUrl;
    }
}
