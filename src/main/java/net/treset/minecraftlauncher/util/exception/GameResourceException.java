package net.treset.minecraftlauncher.util.exception;

public class GameResourceException extends Exception {
    public GameResourceException(String message, Exception cause) {
        super(message, cause);
    }

    public GameResourceException(String message) {
        super(message);
    }
}
