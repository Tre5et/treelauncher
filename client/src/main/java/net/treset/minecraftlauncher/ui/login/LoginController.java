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
import net.treset.minecraftlauncher.ui.generic.popup.PopupElement;
import net.treset.minecraftlauncher.update.UpdaterStatus;
import net.treset.minecraftlauncher.util.file.LauncherFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;


public class LoginController extends GenericUiController {
    private static final Logger LOGGER = LogManager.getLogger(LoginController.class);

    @FXML private Button btLogin;
    @FXML private CheckBox chRemember;
    @FXML private Button btContinue;
    @FXML private Label lbStatus;
    public int loginRetry = 0;

    private boolean updaterReady = false;
    private boolean loginReady = false;

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
        LauncherApplication.setPopupConsumer(this::showPopup);
        LauncherApplication.setCloseCallback(() -> !chRemember.isDisabled() || !btLogin.isDisabled());
    }

    @Override
    public void afterShow(Stage stage) {
        if(LauncherApplication.userAuth.hasFile()) {
            btLogin.setDisable(true);
            chRemember.setDisable(true);
            lbStatus.setText(LauncherApplication.stringLocalizer.get("login.label.authenticating"));
            try {
                if (handleUpdaterStatus()) {
                    new Thread(() -> LauncherApplication.userAuth.authenticateFromFile(this::onAutoLoginDone)).start();
                }
            } catch (IOException e) {
                LauncherApplication.displayError(new IOException("Unable to check updater status", e));
            }
        }
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
            loginReady = true;
            if(updaterReady) {
                Platform.runLater(this::onContinue);
            }
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

    private boolean handleUpdaterStatus() throws IOException {
        LauncherFile updaterFile = LauncherFile.of("updater.json");
        if(!updaterFile.isFile()) {
            updaterReady = true;
            return true;
        }

        UpdaterStatus status = UpdaterStatus.fromJson(updaterFile.readString());

        if(status.getExceptions() != null) {
            LOGGER.warn("Exceptions occurred during update: " + status.getMessage());
            for(String exception : status.getExceptions()) {
                LOGGER.warn(new IOException(exception));
            }
        }

        LauncherApplication.setPopup(
                buildPopup(status)
        );

        if(status.getStatus().getType() != PopupElement.PopupType.ERROR) {
            updaterFile.remove();
        }

        return status.getStatus().getType() != PopupElement.PopupType.ERROR;
    }

    private PopupElement buildPopup(UpdaterStatus status) {
        return new PopupElement(
                status.getStatus().getType(),
                status.getStatus().getTranslationKey() + ".title",
                status.getStatus().getTranslationKey() + ".message",
                List.of(
                        new PopupElement.PopupButton(
                                status.getStatus().getType() == PopupElement.PopupType.ERROR ? PopupElement.ButtonType.NEGATIVE : PopupElement.ButtonType.POSITIVE,
                                "updater.status." + (status.getStatus().getType() == PopupElement.PopupType.ERROR ? "quit" : "close"),
                                (e) -> {
                                    if(status.getStatus().getType() == PopupElement.PopupType.ERROR) {
                                        stage.close();
                                    } else {
                                        LauncherApplication.setPopup(null);
                                        if(loginReady) {
                                            Platform.runLater(this::onContinue);
                                        }
                                        updaterReady = true;
                                    }
                                }
                        )
                )
        );
    }

    public static LoginController showOnStage(Stage stage) throws IOException {
        return showOnStage(stage, "login/LoginScreen", "login.title");
    }
}
