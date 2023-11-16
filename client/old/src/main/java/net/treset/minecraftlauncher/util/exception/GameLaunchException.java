package net.treset.minecraftlauncher.util.exception;

public class GameLaunchException extends Exception {
    public GameLaunchException(String message, Exception cause) {
        super(message, cause);
    }

    public GameLaunchException(String message) {
        super(message);
    }
}
