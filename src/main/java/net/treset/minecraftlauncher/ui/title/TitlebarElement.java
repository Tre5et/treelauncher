package net.treset.minecraftlauncher.ui.title;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import net.treset.minecraftlauncher.ui.base.UiElement;

public class TitlebarElement extends UiElement {
    @FXML private AnchorPane rootPane;

    @Override
    public void beforeShow(Stage stage) {}

    @Override
    public void afterShow(Stage stage) {}

    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }
}
