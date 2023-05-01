package net.treset.minecraftlauncher.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FileUtil {
    private static Logger LOGGER = LogManager.getLogger(FileUtil.class);

    public static String loadFile(String path) {
        Path filePath = Paths.get(path);
        if(!filePath.toFile().isFile()) {
            LOGGER.debug("Unable to load file: doesn't exist: path=" + path);
            return null;
        }
        try {
            return Files.readString(filePath);
        } catch (IOException e) {
            LOGGER.debug("Unable to load file: path=" + path, e);
            return null;
        }
    }

    public static boolean deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (! Files.isSymbolicLink(f.toPath())) {
                    deleteDir(f);
                }
            }
        }
        return file.delete();
    }

    public static boolean copyDirectory(String source, String destination) {
        try {
            Files.walk(Paths.get(source))
                    .forEach(sourceF -> {
                        Path destinationF = Paths.get(destination, sourceF.toString()
                                .substring(source.length()));
                        try {
                            Files.copy(sourceF, destinationF, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            LOGGER.debug("Unable to copy directory: source=" + source, e);
            return false;
        }
        return true;
    }
}
