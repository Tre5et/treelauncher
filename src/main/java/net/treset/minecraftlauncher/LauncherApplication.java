package net.treset.minecraftlauncher;

import javafx.application.Application;
import javafx.stage.Stage;
import net.treset.mc_version_loader.VersionLoader;
import net.treset.mc_version_loader.files.JavaFileDownloader;
import net.treset.mc_version_loader.files.Sources;
import net.treset.mc_version_loader.java.JavaFile;
import net.treset.mc_version_loader.minecraft.MinecraftLibrary;
import net.treset.mc_version_loader.minecraft.MinecraftVersionDetails;
import net.treset.mc_version_loader.launcher.LauncherVersionDetails;
import net.treset.minecraftlauncher.auth.UserAuth;
import net.treset.minecraftlauncher.file_loading.LauncherFiles;
import net.treset.minecraftlauncher.launching.GameLauncher;
import net.treset.minecraftlauncher.resources.localization.StringLocalizer;
import net.treset.minecraftlauncher.ui.controller.LoginUiController;
import net.treset.minecraftlauncher.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


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