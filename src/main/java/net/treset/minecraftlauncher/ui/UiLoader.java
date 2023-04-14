package net.treset.minecraftlauncher.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.ui.controller.LoginUiController;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class UiLoader {
    public static FXMLLoader loadFxmlOnStage(String fxmlPath, Stage stage, String title, Object... args) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        URL xmlUrl = UiLoader.class.getResource("/fxml/"+fxmlPath+".fxml");
        loader.setLocation(xmlUrl);
        loader.setResources(LauncherApplication.stringLocalizer.getStringBundle());
        Parent root = loader.load();

        stage.setTitle(LauncherApplication.stringLocalizer.get(title, args));
        stage.setScene(new Scene(root));
        return loader;
    }
}
