package net.treset.minecraftlauncher.config;

import net.treset.mc_version_loader.json.SerializationException;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.resources.localization.StringLocalizer;
import net.treset.minecraftlauncher.util.file.LauncherFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;

public class GlobalConfigLoader {
    private static final Logger LOGGER = LogManager.getLogger(GlobalConfigLoader.class);

    private static final LauncherFile file = LauncherFile.of("app", "launcher.conf");

    public static Config loadConfig() throws IllegalStateException, IOException {
        if(!file.exists()) {
            LOGGER.info("No config found, creating default");
            file.write(
                    "path=data" + System.lineSeparator()
                            + "update_url=https://raw.githubusercontent.com/Tre5et/treelauncher/main/client/update/"
            );
        }
        String contents = file.readString();
        if(contents == null) {
            throw new IllegalStateException("Unable to load launcher.conf");
        }
        String[] lines = contents.split("\n");
        String path = null;
        boolean debug = false;
        String updateUrl = null;
        for(String line : lines) {
            if(line.startsWith("path=")) {
                path = line.substring(5).replace("\r", "").replace("\n", "");
            } else if(line.startsWith("debug=")) {
                debug = Boolean.parseBoolean(line.substring(6));
            } else if(line.startsWith("update_url=")) {
                updateUrl = line.substring(11).replace("\r", "").replace("\n", "");
            }
        }
        if(path == null || path.isBlank() || updateUrl == null || updateUrl.isBlank()) {
            throw new IllegalStateException("Invalid config: path=" + path + ", updateUrl=" + updateUrl);
        }
        LOGGER.info("Loaded config: path={}, debug={}", path, debug);
        return new Config(path, debug, updateUrl);
    }

    public static void updateLanguage(StringLocalizer.Language language) throws IOException {
        String contents = file.readString();
        String[] lines = contents.split("\n");
        StringBuilder newContents = new StringBuilder();
        boolean found = false;
        for(String line : lines) {
            if(line.startsWith("language=")) {
                newContents.append("language=").append(language.name()).append("\n");
                break;
            } else {
                newContents.append(line).append("\n");
            }
        }
        if(!found) {
            newContents.append("language=").append(language.name()).append("\n");
        }

        file.write(newContents.toString());
    }

    public static void updatePath(File path, boolean removeOld) throws IOException {
        LauncherFile dstDir = LauncherFile.of(path);
        LOGGER.info("Updating path: path={}, removeOld={}", dstDir.getAbsolutePath(), removeOld);

        if(!dstDir.isDirectory()) {
            throw new IOException("Path is not a directory");
        }
        if(LauncherApplication.config.BASE_DIR.isChildOf(dstDir)) {
            throw new IOException("Path is a child of the current directory");
        }

        String contents = file.readString();
        String[] lines = contents.split("\n");
        StringBuilder newContents = new StringBuilder();
        for(String line : lines) {
            if(line.startsWith("path=")) {
                newContents.append("path=").append(dstDir.getAbsolutePath()).append("/").append("\n");
                break;
            } else {
                newContents.append(line).append("\n");
            }
        }

        if(dstDir.isDirEmpty()) {
            LOGGER.debug("Copying files to new directory");
            LauncherApplication.config.BASE_DIR.copyTo(dstDir, StandardCopyOption.REPLACE_EXISTING);
        } else {
            if(!hasMainMainfest(dstDir)) {
                throw new IOException("Directory is not empty and doesn't contain manifest file");
            }

            LOGGER.debug("Not copying files to new directory because it is not empty");
        }

        LOGGER.debug("Updating config");
        LauncherFile oldPath = LauncherApplication.config.BASE_DIR;
        LauncherApplication.config.BASE_DIR = dstDir;
        file.write(newContents.toString());

        if(removeOld) {
            LOGGER.debug("Removing old directory");
            oldPath.remove();
        }
    }

    public static boolean hasMainMainfest(LauncherFile path) {
        String contents;
        try {
            contents = LauncherFile.of(path, LauncherApplication.config.MANIFEST_FILE_NAME).readString();
        } catch (IOException e) {
            return false;
        }
        if(contents == null || contents.isBlank()) {
            return false;
        }
        LauncherManifest manifest;
        try {
            manifest = LauncherManifest.fromJson(contents);
        } catch (SerializationException e) {
            return false;
        }
        return manifest != null && manifest.getType() == LauncherManifestType.LAUNCHER;
    }
}
