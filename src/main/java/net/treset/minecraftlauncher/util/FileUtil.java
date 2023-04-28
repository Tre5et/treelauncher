package net.treset.minecraftlauncher.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileUtil {
    private static Logger LOGGER = Logger.getLogger(FileUtil.class.getName());

    public static String loadFile(String path) {
        Path filePath = Paths.get(path);
        if(!filePath.toFile().isFile()) {
            LOGGER.log(Level.WARNING, "Unable to load file: doesn't exist: path=" + path);
            return null;
        }
        try {
            return Files.readString(filePath);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unable to load file: path=" + path, e);
            return null;
        }
    }
}
