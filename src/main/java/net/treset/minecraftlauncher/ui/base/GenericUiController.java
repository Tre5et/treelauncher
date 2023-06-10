package net.treset.minecraftlauncher.ui.base;

import javafx.stage.Stage;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.util.UiLoader;

import java.io.IOException;

public class GenericUiController implements UiController {
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

    public static <T extends GenericUiController> T showOnStage(Stage stage, String fxml, String title) throws IOException {
        T controller = UiLoader.loadFxmlOnStage(fxml, stage, title);
        controller.beforeShow(stage);
        stage.show();
        controller.afterShow(stage);
        return controller;
    }
}
