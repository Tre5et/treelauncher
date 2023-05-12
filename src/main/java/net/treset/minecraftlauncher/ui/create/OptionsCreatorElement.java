package net.treset.minecraftlauncher.ui.create;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class OptionsCreatorElement {
    @FXML private VBox rootPane;
    @FXML private RadioButton radioCreate;
    @FXML private RadioButton radioUse;
    @FXML private RadioButton radioInherit;
    @FXML private Label createLabel;
    @FXML private Label inheritLabel;
    @FXML private TextField crateName;
    @FXML private TextField inheritName;
    @FXML private ChoiceBox<String> useChoice;
    @FXML private ChoiceBox<String> inheritChoice;


}
