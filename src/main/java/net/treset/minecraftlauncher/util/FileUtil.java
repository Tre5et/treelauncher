package net.treset.minecraftlauncher.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class FileUtil {
    private static Logger LOGGER = LogManager.getLogger(FileUtil.class);

    public static String loadFile(String path) throws IOException {
        Path filePath = Paths.get(path);
        if(!filePath.toFile().isFile()) {
            throw new IOException("Unable to load file: doesn't exist: path=" + path);
        }
        return Files.readString(filePath);
    }

    public static void copyContents(String srcDir, String dstDir, Function<String, Boolean> copyChecker, StandardCopyOption... options) throws IOException {
        File src = new File(srcDir);
        if(!src.isDirectory()) {
            throw new IOException("Unable to copy contents: source is not a directory: srcDir=" + srcDir);
        }

        createDir(dstDir);

        File[] files = src.listFiles();
        if(files == null) {
            throw new IOException("Unable to copy contents: unable to list files: srcDir=" + srcDir);
        }

        for(File file : files) {
            if(!copyChecker.apply(file.getName())) {
                continue;
            }
            if(file.isDirectory()) {
                FileUtil.copyDirectory(file.getAbsolutePath(), dstDir + file.getName() + "/", options);
            } else {
                FileUtil.copyFile(file.getAbsolutePath(), dstDir + file.getName(), options);
            }
        }
    }

    public static void deleteDir(File file) throws IOException {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (! Files.isSymbolicLink(f.toPath())) {
                    deleteDir(f);
                }
            }
        }
        if (! file.delete()) {
            throw new IOException("Unable to delete file: file=" + file);
        }
    }

    public static void copyDirectory(String source, String destination, CopyOption... options) throws IOException {
        createDir(destination);
        try (Stream<Path> stream = Files.walk(Paths.get(source))) {
            stream.forEach(sourceF -> {
                        Path destinationF = Paths.get(destination, sourceF.toString().substring(source.length()));
                        try {
                            Files.copy(sourceF, destinationF, options);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            throw new IOException("Unable to copy directory: source=" + source + ", destination=" + destination, e);
        }
    }

    public static void copyFile(String source, String destination, CopyOption... options) throws IOException {
        createDir(new File(destination).getParent());
        Files.copy(Path.of(source), Path.of(destination), options);
    }

    public static void createDir(String path) throws IOException {
        File dir = new File(path);
        if(!dir.isDirectory() && !dir.mkdirs()) {
            throw new IOException("Unable to create directory: path=" + path);
        }
    }

    public static void writeFile(String path, String content) throws IOException {
        Files.writeString(Paths.get(path), content);
    }
}
