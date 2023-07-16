package net.treset.minecraftlauncher.ui.login;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.ui.MainController;
import net.treset.minecraftlauncher.ui.base.GenericUiController;
import net.treset.minecraftlauncher.ui.title.TitlebarElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;


public class LoginController extends GenericUiController {
    private static final Logger LOGGER = LogManager.getLogger(LoginController.class);

    @FXML private TitlebarElement icTitlebarController;
    @FXML private Button btLogin;
    @FXML private CheckBox chRemember;
    @FXML private Button btContinue;
    @FXML private Label lbStatus;
    public int loginRetry = 0;

    @FXML
    public void onLogin() {
       triggerLogin(chRemember.isSelected() && loginRetry < 1);
    }

    @FXML
    public void onContinue() {
        try {
            MainController.showOnStage(stage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void beforeShow(Stage stage) {
        super.beforeShow(stage);
        icTitlebarController.beforeShow(stage);
    }

    @Override
    public void afterShow(Stage stage) {
        if(LauncherApplication.userAuth.hasFile()) {
            btLogin.setDisable(true);
            chRemember.setDisable(true);
            lbStatus.setText(LauncherApplication.stringLocalizer.get("login.label.authenticating"));
            new Thread(() -> LauncherApplication.userAuth.authenticateFromFile(this::onAutoLoginDone)).start();
        }
        icTitlebarController.afterShow(stage);
    }

    private void triggerLogin(boolean remember) {
        btLogin.setDisable(true);
        chRemember.setDisable(true);
        lbStatus.setText(LauncherApplication.stringLocalizer.get("login.label.authenticating"));
        new Thread(() -> LauncherApplication.userAuth.authenticate(remember, this::onLoginDone)).start();
    }

    private void onLoginDone(Boolean success) {
        Platform.runLater(() -> loginDoneActions(success));
    }

    private void onAutoLoginDone(boolean success) {
        if(success) {
            Platform.runLater(this::onContinue);
        } else {
            Platform.runLater(() -> {
                btLogin.setDisable(false);
                chRemember.setDisable(false);
                lbStatus.setText(LauncherApplication.stringLocalizer.get("login.label.failure"));
                loginRetry++;
                LOGGER.warn("Login failed");
            });
        }
    }

    private void loginDoneActions(boolean success) {
        if(success) {
            lbStatus.setText(LauncherApplication.stringLocalizer.getFormatted("login.label.success", LauncherApplication.userAuth.getMinecraftUser().name()));
            LOGGER.debug("Login success, username=" + LauncherApplication.userAuth.getMinecraftUser().name());
            btContinue.setVisible(true);
        } else {
            btLogin.setDisable(false);
            lbStatus.setText(LauncherApplication.stringLocalizer.get("login.label.failure"));
            loginRetry++;
            LOGGER.warn("Login failed");
        }
    }

    public static LoginController showOnStage(Stage stage) throws IOException {
        return showOnStage(stage, "login/LoginScreen", "login.title");
    }
}
