package net.treset.minecraftlauncher.util.ui;

import javafx.application.Platform;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.launching.GameLauncher;
import net.treset.minecraftlauncher.ui.generic.popup.PopupElement;
import net.treset.minecraftlauncher.util.UiUtil;
import net.treset.minecraftlauncher.util.exception.GameLaunchException;
import net.treset.minecraftlauncher.util.file.LauncherFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class GameLauncherHelper  {
    private static final Logger LOGGER = LogManager.getLogger(GameLauncherHelper.class);

    private GameLauncher gameLauncher;
    private Consumer<String> onGameExit;
    private Function<Boolean, Boolean> lockSetter;


    public GameLauncherHelper(GameLauncher gameLauncher, Consumer<String> onGameExit, Function<Boolean, Boolean> lockSetter) {
        this.gameLauncher = gameLauncher;
        this.onGameExit = onGameExit;
        this.lockSetter = lockSetter;
        gameLauncher.setExitCallbacks(List.of(this::onGameExit));
    }

    public void start() {
        lockSetter.apply(true);
        displayGamePreparing();
        try {
            gameLauncher.launch(false, this::onGameLaunchDone);
        } catch (GameLaunchException e) {
            onGameExit(null);
            displayGameLaunchFailed(e);
        }
    }

    private void onGameLaunchDone(Exception e) {
        if(e == null) {
            displayGameRunning();
        } else {
            onGameExit(null);
            displayGameLaunchFailed(e);
        }
    }

    private void displayGamePreparing() {
        LauncherApplication.setPopup(
                new PopupElement("selector.instance.launch.preparing.title", "selector.instance.launch.preparing.message")
        );
    }

    private void onGameExit(String error) {
        if(error != null) {
            Platform.runLater(() -> displayGameCrash(error));
        } else {
            Platform.runLater(() -> LauncherApplication.setPopup(null));
        }
        lockSetter.apply(false);
        onGameExit.accept(error);
    }

    private void displayGameLaunchFailed(Exception e) {
        LOGGER.error("Failed to launch game", e);
        LauncherApplication.setPopup(
                new PopupElement(
                        PopupElement.PopupType.ERROR,
                        "selector.instance.error.launch.title",
                        "selector.instance.error.launch.message",
                        List.of(
                                new PopupElement.PopupButton(
                                        PopupElement.ButtonType.POSITIVE,
                                        "error.close",
                                        event -> LauncherApplication.setPopup(null)
                                )
                        )
                )
        );
    }

    private void displayGameRunning() {
        LauncherApplication.setPopup(
                new PopupElement(
                        "selector.instance.game.running.title",
                        "selector.instance.game.running.message"
                )
        );
    }

    private void displayGameCrash(String error) {
        LauncherApplication.setPopup(
                new PopupElement(
                        PopupElement.PopupType.WARNING,
                        "selector.instance.game.crash.title",
                        LauncherApplication.stringLocalizer.getFormatted("selector.instance.game.crash.message", error.isBlank() ? "unknown error" : error),
                        List.of(
                                new PopupElement.PopupButton(
                                        PopupElement.ButtonType.POSITIVE,
                                        "selector.instance.game.crash.close",
                                        event -> LauncherApplication.setPopup(null)
                                ),
                                new PopupElement.PopupButton(
                                        PopupElement.ButtonType.POSITIVE,
                                        "selector.instance.game.crash.reports",
                                        event -> UiUtil.openFolder(LauncherFile.of(gameLauncher.getInstance().getInstance().getKey().getDirectory(), LauncherApplication.config.INCLUDED_FILES_DIR, "crash-reports"))
                                )
                        )
                )
        );
    }
}
