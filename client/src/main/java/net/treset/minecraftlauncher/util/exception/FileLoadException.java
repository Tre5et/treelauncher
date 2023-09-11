package net.treset.minecraftlauncher.util.exception;

public class FileLoadException extends Exception {
    public FileLoadException(String message) {
        super(message);
    }
    public FileLoadException(String message, Exception parent) {
        super(message, parent);
    }
}
