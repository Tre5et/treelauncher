package net.treset.minecraftlauncher.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
            LOGGER.log(Level.WARNING, "Unable to copy directory: source=" + source, e);
            return false;
        }
        return true;
    }
}
