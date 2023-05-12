package net.treset.minecraftlauncher.ui.controller.instances;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class InstanceElementController {

    @FXML
    public GridPane instanceElement;
    @FXML
    public Label title;
    @FXML
    public Label details;

    @FXML
    public void onInstanceClicked() {
        title.getStyleClass().add("selected");
        details.getStyleClass().add("selected");
    }
}
