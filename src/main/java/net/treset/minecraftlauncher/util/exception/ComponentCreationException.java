package net.treset.minecraftlauncher.util.exception;

public class ComponentCreationException extends Exception {
    public ComponentCreationException(String message, Exception cause) {
        super(message, cause);
    }

    public ComponentCreationException(String message) {
        super(message);
    }
}
