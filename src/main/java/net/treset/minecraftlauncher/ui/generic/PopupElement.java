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
            switch(type) {
                case POSITIVE:
                    return "positive";
                case NEUTRAL:
                    return "neutral";
                case NEGATIVE:
                    return "negative";
                default:
                    return "";
            }
        }

        public Button getButton() {
            return button;
        }

        public void setDisabled(boolean disabled) {
            button.setDisable(disabled);
        }
    }

    @FXML private GridPane rootPane;
    @FXML private GridPane popupContainer;
    @FXML private Label titleLabel;
    @FXML private Label messageLabel;
    @FXML private HBox buttonContainer;
    @FXML private Label errorMessage;
    @FXML private HBox textFieldContainer;

    private ArrayList<PopupButton> activeButtons;
    private boolean disabled = false;
    private PopupType popupType = PopupType.NONE;

    @Override
    public void beforeShow(Stage stage) {
        buttonContainer.getChildren().clear();
        buttonContainer.getChildren().addAll(activeButtons.stream().map(PopupButton::getButton).toList());
    }

    @Override
    public void afterShow(Stage stage) {
    }

    public void setContent(String title, String message) {
        titleLabel.setText(LauncherApplication.stringLocalizer.get(title));
        messageLabel.setText(LauncherApplication.stringLocalizer.get(message));
    }

    public void addButtons(PopupButton... buttons) {
        activeButtons.addAll(Arrays.stream(buttons).toList());
        activeButtons.forEach(button -> button.setDisabled(disabled));
    }

    public void clearControls() {
        activeButtons = new ArrayList<>();
        textFieldContainer.getChildren().clear();
        errorMessage.setText("");
    }

    public void setControlsDisabled(boolean disabled) {
        this.disabled = disabled;
        if(activeButtons != null){
            activeButtons.forEach(button -> button.setDisabled(disabled));
            textFieldContainer.setDisable(disabled);
        }
    }

    public void setType(PopupType type) {
        popupType = type;
        updateType();
    }

    private void updateType() {
        popupContainer.getStyleClass().remove("success");
        popupContainer.getStyleClass().remove("warning");
        popupContainer.getStyleClass().remove("error");
        switch (popupType) {
            case SUCCESS -> popupContainer.getStyleClass().add("success");
            case WARNING -> popupContainer.getStyleClass().add("warning");
            case ERROR -> popupContainer.getStyleClass().add("error");
        }
    }

    public void setTitle(String title, Object... args) {
        titleLabel.setText(LauncherApplication.stringLocalizer.getFormatted(title, args));
    }

    public void setMessage(String message, Object... args) {
        messageLabel.setText(LauncherApplication.stringLocalizer.getFormatted(message, args));
    }

    TextField textField;
    public void setTextInput(String prompt) {
        textFieldContainer.getChildren().clear();
        textField = new TextField();
        textField.setPromptText(LauncherApplication.stringLocalizer.get(prompt));
        textFieldContainer.getChildren().add(textField);
    }

    public String getTextInputContent() {
        if(textField != null) {
            return textField.getText();
        }
        return null;
    }

    public void setErrorMessage(String message) {
        errorMessage.setText(LauncherApplication.stringLocalizer.get(message));
    }

    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }
}
