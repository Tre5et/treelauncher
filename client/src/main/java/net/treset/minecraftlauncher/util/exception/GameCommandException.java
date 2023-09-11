package net.treset.minecraftlauncher.util.exception;

public class GameCommandException extends Exception {
    public GameCommandException(String message, Exception cause) {
        super(message, cause);
    }

    public GameCommandException(String message) {
        super(message);
    }
}
