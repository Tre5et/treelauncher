package net.treset.minecraftlauncher.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.function.Function;

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

    public static boolean copyContents(String srcDir, String dstDir, Function<String, Boolean> copyChecker, StandardCopyOption... options) {
        File src = new File(srcDir);
        if(!src.isDirectory()) {
            LOGGER.debug("Unable to copy contents: source is not a directory: srcDir=" + srcDir);
            return false;
        }

        if(!createDir(dstDir)) {
            LOGGER.debug("Unable to copy contents: unable to create directory: dstDir=" + dstDir);
            return false;
        }

        File[] files = src.listFiles();
        if(files == null) {
            LOGGER.debug("Unable to copy contents: unable to list files: srcDir=" + srcDir);
            return false;
        }

        for(File file : files) {
            if(!copyChecker.apply(file.getName())) {
                continue;
            }
            if(file.isDirectory()) {
                if(!FileUtil.copyDirectory(file.getAbsolutePath(), dstDir + file.getName() + "/", options)) {
                    LOGGER.warn("Unable to copy files: unable to copy directory: name={}", file.getName());
                    return false;
                }
            } else {
                if(!FileUtil.copyFile(file.getAbsolutePath(), dstDir + file.getName(), options)) {
                    LOGGER.warn("Unable to copy files: unable to copy directory: name={}", file.getName());
                    return false;
                }
            }
        }
        return true;
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

    public static boolean copyDirectory(String source, String destination, CopyOption... options) {
        if(!createDir(destination)) {
            LOGGER.debug("Unable to copy directory: unable to create directory: destination=" + destination);
            return false;
        }

        try {
            Files.walk(Paths.get(source))
                    .forEach(sourceF -> {
                        Path destinationF = Paths.get(destination, sourceF.toString()
                                .substring(source.length()));
                        try {
                            Files.copy(sourceF, destinationF, options);
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

    public static boolean copyFile(String source, String destination, CopyOption... options) {
        if(!createDir(new File(destination).getParent())) {
            LOGGER.debug("Unable to copy file: unable to make parent dir: destination={}", destination);
            return false;
        }

        try {
            Files.copy(Path.of(source), Path.of(destination), options);
        } catch (IOException e) {
            LOGGER.debug("Unable to copy file: source={}, destination={}", source, destination, e);
            return false;
        }
        return true;
    }

    public static boolean createDir(String path) {
        File dir = new File(path);
        if(!dir.isDirectory() && !dir.mkdirs()) {
            LOGGER.debug("Unable to create directory: path={}", path);
            return false;
        }
        return true;
    }

    public static boolean writeFile(String path, String content) {
        try {
            Files.writeString(Paths.get(path), content);
        } catch (IOException e) {
            LOGGER.debug("Unable to write file: path={}", path, e);
            return false;
        }
        return true;
    }
}
