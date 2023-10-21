package net.treset.minecraftlauncher.ui.base;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.ui.generic.popup.PopupElement;

import java.io.IOException;
import java.net.URL;

public class GenericUiController implements UiController {
    @FXML private StackPane root;

    protected Stage stage;

    @Override
    public void triggerHomeAction() {
    }

    @Override
    public void beforeShow(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void afterShow(Stage stage) {}

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void showPopup(PopupElement popupElement) {
        if(popupElement == null) {
            root.getChildren().remove(1);
            return;
        }
        if(root.getChildren().size() > 1) {
            root.getChildren().set(1, popupElement);
        } else {
            root.getChildren().add(popupElement);
        }
    }

    public static <T extends GenericUiController> T showOnStage(Stage stage, String fxml, String title) throws IOException {
        T controller = loadFxmlOnStage(fxml, stage, title);
        controller.beforeShow(stage);
        stage.show();
        controller.afterShow(stage);
        return controller;
    }

    public static <T extends UiController> T loadFxmlOnStage(String fxmlPath, Stage stage, String title, Object... args) throws IOException {
        FXMLLoader loader = getFXMLLoader(fxmlPath);
        Parent root = loadFXML(loader);

        stage.setTitle(LauncherApplication.stringLocalizer.getFormatted(title, args));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        return loader.getController();
    }

    public static FXMLLoader getFXMLLoader(String fxmlPath) {
        FXMLLoader loader = new FXMLLoader();
        URL xmlUrl = UiController.class.getResource("/fxml/" + fxmlPath + ".fxml");
        loader.setLocation(xmlUrl);
        loader.setResources(LauncherApplication.stringLocalizer.getStringBundle());
        return loader;
    }

    public static <T> T loadFXML(FXMLLoader fxmlLoader) throws IOException {
        return fxmlLoader.load();
    }
}
