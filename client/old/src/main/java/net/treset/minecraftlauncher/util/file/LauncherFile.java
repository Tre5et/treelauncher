package net.treset.minecraftlauncher.util.file;

import net.treset.mc_version_loader.json.JsonParsable;
import net.treset.minecraftlauncher.LauncherApplication;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class LauncherFile extends File {
    public LauncherFile(String pathname) {
        super(pathname);
    }

    public byte[] read() throws IOException {
        if(!isFile()) throw new IOException("File does not exist: " + getAbsolutePath());
        return Files.readAllBytes(toPath());
    }

    public String readString() throws IOException {
        return new String(read());
    }

    public boolean isChildOf(File parent) {
        return parent.isDirectory() && getAbsolutePath().startsWith(parent.getAbsolutePath());
    }

    public void copyTo(LauncherFile dst, CopyOption... options) throws IOException {
        copyTo(dst, s -> true, options);
    }

    public void copyTo(LauncherFile dst, Function<String, Boolean> copyChecker, CopyOption... options) throws IOException {
        if(!exists()) throw new IOException("File does not exist: " + getAbsolutePath());
        if(isDirectory()) {
            dst.createDir();
            try (Stream<Path> stream = Files.walk(Path.of(getPath()))) {
                List<IOException> exceptions = new ArrayList<>();
                int sourceLength = getAbsolutePath().length();
                stream.forEach(sourceF -> {
                    if(!copyChecker.apply(sourceF.getFileName().toString()) || sourceF.toString().equals(getAbsolutePath())) {
                        return;
                    }
                    Path destinationF = Paths.get(dst.getPath(), sourceF.toString().substring(sourceLength));
                    try {
                        Files.copy(sourceF, destinationF, options);
                    } catch (IOException e) {
                        exceptions.add(e);
                    }
                });
                if(!exceptions.isEmpty()) {
                    throw new IOException("Unable to copy directory: " + exceptions.size() + " file copies failed: source=" + this + ", destination=" + dst, exceptions.get(0));
                }
            } catch (IOException e) {
                throw new IOException("Unable to copy directory: source=" + this + ", destination=" + dst, e);
            }

        } else {
            Files.copy(toPath(), dst.toPath(), options);
        }
    }

    public void moveTo(File dst, CopyOption... options) throws IOException {
        Files.move(toPath(), dst.toPath(), options);
    }

    public void write(byte[] content) throws IOException {
        if(!exists()) {
            createFile();
        }
        Files.write(toPath(), content);
    }

    public void write(String content) throws IOException {
        write(content.getBytes());
    }

    public void write(JsonParsable content) throws IOException {
        write(content.toJson());
    }

    public void createDir() throws IOException {
        if(!exists()) {
            Files.createDirectories(toPath());
        }
    }

    public void createFile() throws IOException {
        if(!exists()) {
            Files.createDirectories(getAbsoluteFile().getParentFile().toPath());
            Files.createFile(toPath());
        }
    }

    public void remove() throws IOException {
        if(!exists()) {
            throw new IOException("File does not exist: " + this);
        }
        if(isDirectory()) {
            ArrayList<Exception> exceptionQueue = new ArrayList<>();
            try (Stream<Path> pathStream = Files.walk(toPath())) {
                pathStream.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(f -> {
                            try {
                                Files.delete(f.toPath());
                            } catch(IOException e) {
                                exceptionQueue.add(e);
                            }
                        });
            }
            if(!exceptionQueue.isEmpty()) {
                throw new IOException("Failed to delete directory: " + this, exceptionQueue.get(0));
            }
        } else {
            Files.delete(toPath());
        }
    }

    public boolean isDirEmpty() throws IOException {
        if(!isDirectory()) {
            throw new IOException("File is not a directory: " + this);
        }
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(toPath())) {
            return !dirStream.iterator().hasNext();
        }
    }

    @Override
    public LauncherFile[] listFiles() {
        File[] files = super.listFiles();
        if(files == null) {
            return null;
        }
        return Arrays.stream(files).map(LauncherFile::of).toArray(LauncherFile[]::new);
    }

    public String hash() throws IOException {
        byte[] content = read();
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e); // this doesn't happen
        }

        byte[] encrypted = md.digest(content);
        StringBuilder encryptedString = new StringBuilder(new BigInteger(1, encrypted).toString(16));
        for(int i = encryptedString.length(); i < 32; i++) {
            encryptedString.insert(0, "0");
        }
        return encryptedString.toString();
    }

    public void open() throws IOException {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
            desktop.open(this);
        }
    }

    public static LauncherFile of(String... parts) {
        StringBuilder path = new StringBuilder();
        for(int i = 0; i < parts.length; i++) {
            if(parts[i] == null || parts[i].isBlank()) continue;
            path.append(parts[i]);
            if(i != parts.length -1 && !parts[i].endsWith(File.separator) && !parts[i].endsWith("/") && !parts[i].endsWith("\\"))
                path.append(File.separator);
        }
        return new LauncherFile(path.toString());
    }

    public static LauncherFile of(File file) {
        return new LauncherFile(file.getPath());
    }

    public static LauncherFile of(File file, String... parts) {
        String firstPath = file.getPath();
        String[] allParts = new String[parts.length + 1];
        allParts[0] = firstPath;
        System.arraycopy(parts, 0, allParts, 1, parts.length);
        return of(allParts);
    }

    public static LauncherFile of(File file, LauncherFile launcherFile) {
        return of(file, launcherFile.getPath());
    }

    public static LauncherFile ofRelative(String... parts) {
        return of(LauncherApplication.config.BASE_DIR, parts);
    }
}
