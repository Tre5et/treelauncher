package net.treset.minecraftlauncher.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
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
        if(source.endsWith("/") || source.endsWith("\\")) {
            source = source.substring(0, source.length() - 1);
        }
        try (Stream<Path> stream = Files.walk(Paths.get(source))) {
            String finalSource = source;
            List<IOException> exceptions = new ArrayList<>();
            stream.forEach(sourceF -> {
                        Path destinationF = Paths.get(destination, sourceF.toString().substring(finalSource.length()));
                        try {
                            Files.copy(sourceF, destinationF, options);
                        } catch (IOException e) {
                            exceptions.add(e);
                        }
                    });
            if(!exceptions.isEmpty()) {
                throw new IOException("Unable to copy directory: " + exceptions.size() + " file copies failed: source=" + source + ", destination=" + destination, exceptions.get(0));
            }
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

    public static boolean isDirEmpty(File dir) throws IOException {
        if(!dir.isDirectory()) {
            throw new IOException("Unable to check if directory is empty: not a directory: dir=" + dir);
        }
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir.toPath())) {
            return !dirStream.iterator().hasNext();
        }
    }

    public static boolean dirContains(File dir, String fileName) throws IOException {
        if(!dir.isDirectory()) {
            throw new IOException("Unable to check if directory is empty: not a directory: dir=" + dir);
        }
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir.toPath())) {
            for(Path path : dirStream) {
                if(path.getFileName().toString().equals(fileName)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean isChildOf(File parent, File child) throws IOException {
        if(!parent.isDirectory()) {
            throw new IOException("Unable to check if directory is empty: not a directory: dir=" + parent);
        }
        return child.toPath().startsWith(parent.toPath());
    }
}
