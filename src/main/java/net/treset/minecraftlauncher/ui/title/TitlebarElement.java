package net.treset.minecraftlauncher.ui.title;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.treset.minecraftlauncher.ui.base.UiElement;

public class TitlebarElement extends UiElement {
    @FXML private VBox rootPane;
    @FXML private Stage stage;

    @Override
    public void beforeShow(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void afterShow(Stage stage) {}

    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }

    @FXML
    private void onMinimizeButtonClicked(MouseEvent mouseEvent) {
        stage.setIconified(true);
    }

    @FXML
    private void onMaximizeButtonClicked(MouseEvent mouseEvent) {
        stage.setMaximized(!stage.isMaximized());
    }

    @FXML
    private void onCloseButtonClicked(MouseEvent mouseEvent) {
        stage.close();
    }

    private double initX;
    private double initY;
    @FXML
    private void onMousePressed(MouseEvent e) {
        initX = e.getScreenX() - stage.getX();
        initY = e.getScreenY() - stage.getY();
    }

    @FXML
    private void onMouseDragged(MouseEvent e) {
        stage.setX(e.getScreenX() - initX);
        stage.setY(e.getScreenY() - initY);
    }
}
