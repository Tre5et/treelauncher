package net.treset.minecraftlauncher.ui;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import net.treset.minecraftlauncher.Main;
import net.treset.minecraftlauncher.auth.UserAuth;
import net.treset.minecraftlauncher.config.Config;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginUi {
    private static final Logger LOGGER = Logger.getLogger(LoginUi.class.toString());

    private Button loginButton;
    private Text statusText;

    public void showOnStage(Stage stage) {
        stage.setTitle("Login");
        loginButton = new Button();
        loginButton.setText("Login with Microsoft");
        loginButton.setOnAction(event -> this.startAuthentication(Main.userAuth));
        statusText = new Text();
        statusText.setText("");
        Pane root = new StackPane();
        root.getChildren().add(loginButton);
        root.getChildren().add(statusText);
        stage.setScene(new Scene(root, 500, 300));
        stage.show();
    }

    public void startAuthentication(UserAuth userAuth) {
        loginButton.setDisable(true);
        statusText.setText("Authenticating...");
        userAuth.authenticate(Config.AUTH_FILE, this::onLoginDone);
    }

    public void onLoginDone(Boolean success) {
        if(success) {
            statusText.setText("Welcome " + Main.userAuth.getMinecraftUser().name() + "!");
            LOGGER.log(Level.INFO, "Login success");
        } else {
            loginButton.setDisable(false);
            statusText.setText("Login failed, try again!");
            LOGGER.log(Level.INFO, "Login failed");
        }

    }
}
