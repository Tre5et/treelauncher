package net.treset.minecraftlauncher.ui.controller;

import javafx.stage.Stage;
import net.treset.minecraftlauncher.ui.UiLoader;

import java.io.IOException;

public class GenericUiController implements UiController {
    protected Stage stage;
    @Override
    public void beforeShow(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void afterShow(Stage stage) {
    }

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
