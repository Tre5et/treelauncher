package net.treset.minecraftlauncher.ui.generic;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.ui.base.UiElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

public class PopupElement extends UiElement {
    public enum ButtonType {
        POSITIVE,
        NEUTRAL,
        NEGATIVE
    }

    public enum PopupType {
        NONE,
        SUCCESS,
        WARNING,
        ERROR
    }

    public static class PopupButton {
        private final Button button;

        public PopupButton(ButtonType type, String text, String id, Consumer<String> onPressed) {
            button = new Button(LauncherApplication.stringLocalizer.get(text));
            button.getStyleClass().add(getStyleClass(type));
            button.setOnAction(event -> onPressed.accept(id));
        }

        private String getStyleClass(ButtonType type) {
            return switch (type) {
                case POSITIVE -> "positive";
                case NEUTRAL -> "neutral";
                case NEGATIVE -> "negative";
                default -> "";
            };
        }

        public Button getButton() {
            return button;
        }

        public void setDisabled(boolean disabled) {
            button.setDisable(disabled);
        }
    }

    @FXML private GridPane rootPane;
    @FXML private GridPane gpContainer;
    @FXML private Label lbTitle;
    @FXML private Label lbMessage;
    @FXML private HBox hbControlContainer;
    @FXML private Label lbError;
    @FXML private HBox hbInputContainer;

    private ArrayList<PopupButton> activeButtons;
    private boolean disabled = false;
    private PopupType popupType = PopupType.NONE;

    @Override
    public void beforeShow(Stage stage) {
        hbControlContainer.getChildren().clear();
        hbControlContainer.getChildren().addAll(activeButtons.stream().map(PopupButton::getButton).toList());
    }

    @Override
    public void afterShow(Stage stage) {
    }

    public void setContent(String title, String message) {
        lbTitle.setText(LauncherApplication.stringLocalizer.get(title));
        lbMessage.setText(LauncherApplication.stringLocalizer.get(message));
    }

    public void addButtons(PopupButton... buttons) {
        activeButtons.addAll(Arrays.stream(buttons).toList());
        activeButtons.forEach(button -> button.setDisabled(disabled));
    }

    public void clearControls() {
        activeButtons = new ArrayList<>();
        hbInputContainer.getChildren().clear();
        lbError.setText("");
    }

    public void setControlsDisabled(boolean disabled) {
        this.disabled = disabled;
        if(activeButtons != null){
            activeButtons.forEach(button -> button.setDisabled(disabled));
            hbInputContainer.setDisable(disabled);
        }
    }

    public void setType(PopupType type) {
        popupType = type;
        updateType();
    }

    private void updateType() {
        gpContainer.getStyleClass().remove("success");
        gpContainer.getStyleClass().remove("warning");
        gpContainer.getStyleClass().remove("error");
        switch (popupType) {
            case SUCCESS -> gpContainer.getStyleClass().add("success");
            case WARNING -> gpContainer.getStyleClass().add("warning");
            case ERROR -> gpContainer.getStyleClass().add("error");
        }
    }

    public void setTitle(String title, Object... args) {
        lbTitle.setText(LauncherApplication.stringLocalizer.getFormatted(title, args));
    }

    public void setMessage(String message, Object... args) {
        lbMessage.setText(LauncherApplication.stringLocalizer.getFormatted(message, args));
    }

    TextField textField;
    public void setTextInput(String prompt) {
        hbInputContainer.getChildren().clear();
        textField = new TextField();
        textField.setPromptText(LauncherApplication.stringLocalizer.get(prompt));
        hbInputContainer.getChildren().add(textField);
    }

    public String getTextInputContent() {
        if(textField != null) {
            return textField.getText();
        }
        return null;
    }

    public void setErrorMessage(String message) {
        lbError.setText(LauncherApplication.stringLocalizer.get(message));
    }

    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }
}
