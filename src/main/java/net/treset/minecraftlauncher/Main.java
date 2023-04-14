package net.treset.minecraftlauncher;

import javafx.application.Application;
import javafx.stage.Stage;
import net.treset.minecraftlauncher.auth.UserAuth;
import net.treset.minecraftlauncher.ui.LoginUi;


public class Main extends Application {

    public static final UserAuth userAuth = new UserAuth();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        new LoginUi().showOnStage(primaryStage);
    }
}