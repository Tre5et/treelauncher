package net.treset.minecraftlauncher.util;

import net.treset.mc_version_loader.json.JsonParsable;
import net.treset.mc_version_loader.launcher.LauncherDetails;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.minecraftlauncher.util.file.LauncherFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FileInitializer {
    private static final Logger LOGGER = LogManager.getLogger(FileInitializer.class);

    private final LauncherFile directory;
    private final List<File> dirs;
    private final List<InitializingManifest> files;

    public FileInitializer(LauncherFile directory) throws IllegalArgumentException {
        if(directory == null || !directory.isDirectory() && !directory.mkdirs()) {
            throw new IllegalArgumentException("Cannot create directory");
        }
        this.directory = directory;
        this.dirs = List.of(
                new File(directory, "game_data"),
                new File(directory, "instance_data"),
                new File(directory, "java_data"),
                new File(directory, "options_data"),
                new File(directory, "resourcepack_data"),
                new File(directory, "version_data"),
                new File(directory, "libraries"),
                new File(directory, "assets")
        );
        this.files = List.of(
                new InitializingManifest(new LauncherManifest("launcher", null, null, "launcher.json", null, null, null, null), "manifest.json"),
                new InitializingManifest(new LauncherDetails(
                        null,
                        "assets",
                        "game_data",
                        "game",
                        "instance_component",
                        "instance_data",
                        "instances",
                        "java_component",
                        "java_data",
                        "javas",
                        "libraries",
                        "mods_component",
                        "mods",
                        "options_component",
                        "options_data",
                        "options",
                        "resourcepack_component",
                        "resourcepack_data",
                        "resourcepacks",
                        "saves_component",
                        "saves",
                        "version_component",
                        "version_data",
                        "versions"
                ), "launcher.json"),
                new InitializingManifest("game", null, List.of("mods.json", "saves.json"), "game_data", "manifest.json"),
                new InitializingManifest("mods", "mods", "game_data", "mods.json"),
                new InitializingManifest("saves", "saves", "game_data", "saves.json"),
                new InitializingManifest("instances", "instance", "instance_data", "manifest.json"),
                new InitializingManifest("javas", "java", "java_data", "manifest.json"),
                new InitializingManifest("options", "options", "options_data", "manifest.json"),
                new InitializingManifest("resourcepacks", "resourcepacks", "resourcepack_data", "manifest.json"),
                new InitializingManifest("versions", "version", "version_data", "manifest.json")
        );
    }

    public void create() throws IOException {
        LOGGER.info("Creating default files: directory=" + directory);

        if(!directory.isDirectory() || !directory.isDirEmpty()) {
            throw new IOException("Directory is not empty");
        }

        for(File dir : dirs) {
            if(!dir.mkdirs()) {
                throw new IOException("Cannot create directory=" + dir.getAbsolutePath());
            }
        }

        for(InitializingManifest file : files) {
            file.make();
        }

        // this is a terrible hack for the packaging
        removeUpdaterFromClasspath();
    }

    private void removeUpdaterFromClasspath() {
        LauncherFile cfg = LauncherFile.of("app", "TreeLauncher.cfg");
        if(cfg.exists()) {
            try {
                String content = cfg.readString();
                content = content.replace("app.classpath=$APPDIR\\updater.jar", "");
                cfg.write(content);
            } catch(IOException e) {
                LOGGER.error("Failed to remove updater classpath", e);
            }
        }
    }

    public class InitializingManifest {
        private final LauncherFile file;
        private final JsonParsable manifest;

        public InitializingManifest(LauncherFile file, JsonParsable manifest) {
            this.file = file;
            this.manifest = manifest;
        }

        public InitializingManifest(JsonParsable manifest, String... path) {
            this(LauncherFile.of(directory, path), manifest);
        }

        public InitializingManifest(String type, String prefix, List<String> components, String... path) {
            this(new LauncherManifest(type, null, null, null, prefix, null, null, components), path);
        }

        public InitializingManifest(String type, String prefix, String... path) {
            this(type, prefix, List.of(), path);
        }

        public void make() throws IOException {
            file.write(manifest);
        }
    }
}
