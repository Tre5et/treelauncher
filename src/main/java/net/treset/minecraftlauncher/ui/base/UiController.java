package net.treset.minecraftlauncher.ui.base;

import javafx.stage.Stage;

public interface UiController {
    void triggerHomeAction();
    void beforeShow(Stage stage);
    void afterShow(Stage stage);
}
