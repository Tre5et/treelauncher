package net.treset.minecraftlauncher.ui.login;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.config.Config;
import net.treset.minecraftlauncher.ui.base.GenericUiController;
import net.treset.minecraftlauncher.ui.MainController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;


public class LoginController extends GenericUiController {
    private static final Logger LOGGER = LogManager.getLogger(LoginController.class);

    @FXML
    public Button loginButton;
    @FXML
    public Button continueButton;
    @FXML
    public Label statusLabel;
    public int loginRetry = 0;

    @FXML
    public void onLoginButtonClicked() {
       triggerLogin(loginRetry > 1);
    }

    @FXML
    public void onContinueButtonClicked() {
        try {
            MainController.showOnStage(stage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void afterShow(Stage stage) {
        if(Config.AUTH_FILE.isFile()) {
            triggerLogin(false);
        }
    }

    private void triggerLogin(boolean ignoreFile) {
        loginButton.setDisable(true);
        statusLabel.setText(LauncherApplication.stringLocalizer.get("login.label.authenticating"));
        new Thread(() -> LauncherApplication.userAuth.authenticate(Config.AUTH_FILE, ignoreFile, this::onLoginDone)).start();
    }

    private void onLoginDone(Boolean success) {
        Platform.runLater(() -> loginDoneActions(success));
    }

    private void loginDoneActions(boolean success) {
        if(success) {
            statusLabel.setText(LauncherApplication.stringLocalizer.getFormatted("login.label.success", LauncherApplication.userAuth.getMinecraftUser().name()));
            LOGGER.debug("Login success, username=" + LauncherApplication.userAuth.getMinecraftUser().name());

            continueButton.setVisible(true);
        } else {
            loginButton.setDisable(false);
            statusLabel.setText(LauncherApplication.stringLocalizer.get("login.label.failure"));
            loginRetry++;
            LOGGER.warn("Login failed");
        }
    }

    public static LoginController showOnStage(Stage stage) throws IOException {
        return showOnStage(stage, "login/LoginScreen", "login.title");
    }
}