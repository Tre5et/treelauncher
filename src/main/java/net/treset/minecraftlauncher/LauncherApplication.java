package net.treset.minecraftlauncher;

import javafx.application.Application;
import javafx.stage.Stage;
import net.treset.minecraftlauncher.auth.UserAuth;
import net.treset.minecraftlauncher.resources.localization.StringLocalizer;
import net.treset.minecraftlauncher.ui.login.LoginController;

import java.io.IOException;


public class LauncherApplication extends Application {

    public static final UserAuth userAuth = new UserAuth();
    public static final StringLocalizer stringLocalizer = new StringLocalizer();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        System.setProperty("prism.lcdtext", "false");
        LoginController.showOnStage(primaryStage);
    }
}