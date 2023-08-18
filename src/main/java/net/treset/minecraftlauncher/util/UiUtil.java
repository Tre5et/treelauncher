package net.treset.minecraftlauncher.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.ui.base.UiController;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class UiUtil {
    public static FXMLLoader getFXMLLoader(String fxmlPath) {
        FXMLLoader loader = new FXMLLoader();
        URL xmlUrl = UiUtil.class.getResource("/fxml/"+fxmlPath+".fxml");
        loader.setLocation(xmlUrl);
        loader.setResources(LauncherApplication.stringLocalizer.getStringBundle());
        return loader;
    }
    public static <T> T loadFXML(String fxmlPath) throws IOException {
        return loadFXML(getFXMLLoader(fxmlPath));
    }

    public static <T> T loadFXML(FXMLLoader fxmlLoader) throws IOException {
        return fxmlLoader.load();
    }

    public static <T extends UiController> T loadFxmlOnStage(String fxmlPath, Stage stage, String title, Object... args) throws IOException {
        FXMLLoader loader = getFXMLLoader(fxmlPath);
        Parent root = loadFXML(loader);

        stage.setTitle(LauncherApplication.stringLocalizer.getFormatted(title, args));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        return loader.getController();
    }

    public static void openBrowser(String url) throws IOException, URISyntaxException {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            desktop.browse(new URL(url).toURI());
        }
    }

    public static void openFolder(String path) {
        try {
            Desktop.getDesktop().open(new File(path));
        } catch (IOException e) {
            LauncherApplication.displayError(e);
        }
    }
}
