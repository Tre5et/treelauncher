package net.treset.minecraftlauncher.config;

import java.io.File;
import java.util.List;

public class Config {
    public static final File AUTH_FILE = new File("secrets.auth");
    public static final String BASE_DIR = "D:/Hannes/Coding/gits/minecraft-launcher/launcher_mockup/";
    public static final String MANIFEST_FILE_NAME = "manifest.json";
    public static final String INCLUDED_FILES_DIR = ".included_files";
    public static final List<String> INSTANCE_DEFAULT_INCLUDED_FILES = List.of();
    public static final List<String> INSTANCE_DEFAULT_IGNORED_FILES = List.of(".*backup.*", ".*BACKUP.*");
    public static final List<String> VERSION_DEFAULT_INCLUDED_FILES = List.of();
    public static final List<String> MODS_DEFAULT_INCLUDED_FILES = List.of("\\.fabric/", "config/", "schematics/", "\\.bobby/", "XaeroWorldmap/", "XaeroWaypoints/", "itemscroller/");
    public static final List<String> SAVES_DEFAULT_INCLUDED_FILES = List.of("servers\\.dat", "realms_persistence\\.json");
    public static final List<String> RESOURCEPACK_DEFAULT_INCLUDED_FILES = List.of();
}
