package net.treset.minecraftlauncher.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.treset.mc_version_loader.launcher.LauncherInstanceDetails;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.config.Config;
import net.treset.minecraftlauncher.data.LauncherFiles;
import net.treset.minecraftlauncher.launching.GameLauncher;
import net.treset.minecraftlauncher.ui.UiLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;


public class LoginUiController {
    private static final Logger LOGGER = LogManager.getLogger(LoginUiController.class);

    @FXML
    public Button loginButton;
    @FXML
    public Label statusLabel;

    @FXML
    public void onLoginButtonClicked() {
        loginButton.setDisable(true);
        statusLabel.setText(LauncherApplication.stringLocalizer.get("login.label.authenticating"));
        LauncherApplication.userAuth.authenticate(Config.AUTH_FILE, this::onLoginDone);
    }

    private void onLoginDone(Boolean success) {
        if(success) {
            statusLabel.setText(LauncherApplication.stringLocalizer.get("login.label.success", LauncherApplication.userAuth.getMinecraftUser().name()));
            LOGGER.info("Login success, username=" + LauncherApplication.userAuth.getMinecraftUser().name());

            LauncherFiles files = new LauncherFiles();
            files.reloadAll();
            Pair<LauncherManifest, LauncherInstanceDetails> instance = files.getInstanceComponents().get(0);
            GameLauncher gameLauncher = new GameLauncher(instance, files, LauncherApplication.userAuth.getMinecraftUser(), List.of(this::onGameExit)));
            gameLauncher.launch();
            return;
        } else {
            loginButton.setDisable(false);
            statusLabel.setText(LauncherApplication.stringLocalizer.get("login.label.failure"));
            LOGGER.warn("Login failed");
        }

    }

    private void onGameExit(String error) {
        LOGGER.debug("Game exited: " + error);
    }

    public static LoginUiController showOnStage(Stage stage) throws IOException {
        LoginUiController controller = UiLoader.loadFxmlOnStage("LoginUi", stage, "login.title").getController();
        stage.show();
        return controller;
    }
}
