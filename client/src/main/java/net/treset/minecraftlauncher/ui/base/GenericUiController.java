package net.treset.minecraftlauncher.ui.base;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import net.treset.minecraftlauncher.ui.generic.PopupElement;
import net.treset.minecraftlauncher.util.UiUtil;

import java.io.IOException;

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
        T controller = UiUtil.loadFxmlOnStage(fxml, stage, title);
        controller.beforeShow(stage);
        stage.show();
        controller.afterShow(stage);
        return controller;
    }
}
