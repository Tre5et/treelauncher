package net.treset.minecraftlauncher.config;

import net.treset.mc_version_loader.launcher.LauncherFeature;
import net.treset.mc_version_loader.launcher.LauncherLaunchArgument;
import net.treset.minecraftlauncher.resources.localization.StringLocalizer;

import java.io.File;
import java.util.List;

public class Config {
    public final String BASE_DIR;
    public final String SPECIAL_FILES_FOLDER;
    public final String LOG_PATH;
    public final File AUTH_FILE;
    public final String MANIFEST_FILE_NAME = "manifest.json";
    public final String INCLUDED_FILES_DIR = ".included_files";
    public final List<LauncherFeature> INSTANCE_DEFAULT_FEATURES = List.of();
    public final List<String> INSTANCE_DEFAULT_INCLUDED_FILES = List.of();
    public final List<String> INSTANCE_DEFAULT_IGNORED_FILES = List.of(".*backup.*", ".*BACKUP.*");
    public final List<LauncherLaunchArgument> INSTANCE_DEFAULT_JVM_ARGUMENTS = List.of();
    public final String INSTANCE_DEFAULT_DETAILS = "instance.json";
    public final List<String> OPTIONS_DEFAULT_INCLUDED_FILES = List.of("options\\.txt", "usercache\\.json");
    public final List<String> MODS_DEFAULT_INCLUDED_FILES = List.of("\\.fabric/", "config/", "schematics/", "\\.bobby/", "XaeroWorldmap/", "XaeroWaypoints/", "itemscroller/");
    public final String MODS_DEFAULT_DETAILS = "mods.json";
    public final List<String> SAVES_DEFAULT_INCLUDED_FILES = List.of("servers\\.dat", "realms_persistence\\.json");
    public final List<String> RESOURCEPACK_DEFAULT_INCLUDED_FILES = List.of();
    public final String VERSION_DEFAULT_DETAILS = "version.json";
    public final List<LauncherLaunchArgument> MINECRAFT_DEFAULT_GAME_ARGUMENTS = List.of(new LauncherLaunchArgument("--resourcePackDir", null, null, null, null), new LauncherLaunchArgument("${resourcepack_directory}", null, null, null, null));
    public final List<LauncherLaunchArgument> MINECRAFT_DEFAULT_JVM_ARGUMENTS = List.of();
    public final List<LauncherLaunchArgument> FABRIC_DEFAULT_GAME_ARGUMENTS = List.of();
    public final List<LauncherLaunchArgument> FABRIC_DEFAULT_JVM_ARGUMENTS = List.of();
    public final String FABRIC_DEFAULT_CLIENT_FILENAME = "fabric-client.jar";
    public final boolean DEBUG;
    public final StringLocalizer.Language LANGUAGE;

    public Config(String BASE_DIR, boolean DEBUG, StringLocalizer.Language LANGUAGE) {
        this.BASE_DIR = BASE_DIR;
        SPECIAL_FILES_FOLDER = BASE_DIR + ".launcher/";
        AUTH_FILE = new File(SPECIAL_FILES_FOLDER + "secrets.auth");
        LOG_PATH = SPECIAL_FILES_FOLDER + "log/";

        this.DEBUG = DEBUG;
        this.LANGUAGE = LANGUAGE;
    }
}
