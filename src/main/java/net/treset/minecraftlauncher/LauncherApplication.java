package net.treset.minecraftlauncher;

import javafx.application.Application;
import javafx.stage.Stage;
import net.treset.minecraftlauncher.auth.UserAuth;
import net.treset.minecraftlauncher.file_loading.LauncherFiles;
import net.treset.minecraftlauncher.launching.GameLauncher;
import net.treset.minecraftlauncher.resources.localization.StringLocalizer;
import net.treset.minecraftlauncher.ui.controller.LoginUiController;

import java.io.IOException;
import java.net.URISyntaxException;


public class LauncherApplication extends Application {

    public static final UserAuth userAuth = new UserAuth();
    public static final StringLocalizer stringLocalizer = new StringLocalizer();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        LoginUiController.showOnStage(primaryStage);
    }
}