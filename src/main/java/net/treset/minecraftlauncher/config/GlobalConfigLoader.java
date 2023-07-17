package net.treset.minecraftlauncher.config;

import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.resources.localization.StringLocalizer;
import net.treset.minecraftlauncher.util.FileUtil;
import net.treset.minecraftlauncher.util.FormatUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;

public class GlobalConfigLoader {
    private static final Logger LOGGER = LogManager.getLogger(GlobalConfigLoader.class);

    private static final String filePath = FormatUtil.absoluteFilePath("app", "launcher.conf");

    public static Config loadConfig() throws IllegalStateException, IOException {
        if(!new File(filePath).exists()) {
            LOGGER.info("No config found, creating default");
            FileUtil.writeFile(filePath,
                    "path=data" + System.lineSeparator()
                    + "update_url=https://raw.githubusercontent.com/Tre5et/minecraft-launcher/main/update/"
            );
        }
        String contents = FileUtil.loadFile(filePath);
        if(contents == null) {
            throw new IllegalStateException("Unable to load launcher.conf");
        }
        String[] lines = contents.split("\n");
        String path = null;
        boolean debug = false;
        String updateUrl = null;
        StringLocalizer.Language language = null;
        for(String line : lines) {
            if(line.startsWith("path=")) {
                path = line.substring(5).replace("\r", "").replace("\n", "");
            } else if(line.startsWith("debug=")) {
                debug = Boolean.parseBoolean(line.substring(6));
            } else if(line.startsWith("update_url=")) {
                updateUrl = line.substring(11).replace("\r", "").replace("\n", "");
            } else if(line.startsWith("language=")) {
                String input = line.substring(9).replace("\r", "").replace("\n", "");
                language = StringLocalizer.Language.fromId(input);
            }
        }
        if(path == null || path.isBlank() || updateUrl == null || updateUrl.isBlank()) {
            throw new IllegalStateException("Invalid config: path=" + path + ", updateUrl=" + updateUrl);
        }
        LOGGER.info("Loaded config: path={}, debug={}", path, debug);
        return new Config(path, debug, updateUrl, language);
    }

    public static void updateLanguage(StringLocalizer.Language language) throws IOException {
        String contents = FileUtil.loadFile(filePath);
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

        FileUtil.writeFile(filePath, newContents.toString());
    }

    public static void updatePath(File path, boolean removeOld) throws IOException {
        LOGGER.info("Updating path: path={}, removeOld={}", path.getAbsolutePath(), removeOld);
        if(!path.isDirectory()) {
            throw new IOException("Path is not a directory");
        }
        if(FileUtil.isChildOf(new File(LauncherApplication.config.BASE_DIR), path)) {
            throw new IOException("Path is a child of the current directory");
        }

        String contents = FileUtil.loadFile(filePath);
        String[] lines = contents.split("\n");
        StringBuilder newContents = new StringBuilder();
        for(String line : lines) {
            if(line.startsWith("path=")) {
                newContents.append("path=").append(path.getAbsolutePath()).append("/").append("\n");
                break;
            } else {
                newContents.append(line).append("\n");
            }
        }

        if(FileUtil.isDirEmpty(path)) {
            LOGGER.debug("Copying files to new directory");
            FileUtil.copyDirectory(LauncherApplication.config.BASE_DIR, path.getAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);
        } else {
            if(!manifestExists(path)) {
                throw new IOException("Directory is not empty and doesn't contain manifest file");
            }

            LOGGER.debug("Not copying files to new directory because it is not empty");
        }

        LOGGER.debug("Updating config");
        String oldPath = LauncherApplication.config.BASE_DIR;
        LauncherApplication.config.BASE_DIR = path.getAbsolutePath() + "/";
        FileUtil.writeFile(filePath, newContents.toString());

        if(removeOld) {
            LOGGER.debug("Removing old directory");
            FileUtil.deleteDir(new File(oldPath));
        }
    }

    public static boolean manifestExists(File path) throws IOException {
        if(!FileUtil.dirContains(path, LauncherApplication.config.MANIFEST_FILE_NAME)) {
            return false;
        }
        String contents = FileUtil.loadFile(FormatUtil.absoluteFilePath(path.getAbsolutePath(), LauncherApplication.config.MANIFEST_FILE_NAME));
        if(contents == null || contents.isBlank()) {
            return false;
        }
        LauncherManifest manifest = LauncherManifest.fromJson(contents);
        return manifest != null && manifest.getType() == LauncherManifestType.LAUNCHER;
    }
}
