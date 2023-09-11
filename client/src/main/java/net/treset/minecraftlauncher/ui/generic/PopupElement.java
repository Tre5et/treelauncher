package net.treset.minecraftlauncher.ui.generic;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import net.treset.minecraftlauncher.LauncherApplication;

import java.util.List;
import java.util.function.Consumer;

public class PopupElement extends GridPane {
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

        public PopupButton(ButtonType type, String text, EventHandler<ActionEvent> onPressed) {
            this(type, text, onPressed, false);
        }

        public PopupButton(ButtonType type, String text, EventHandler<ActionEvent> onPressed, boolean disabled) {
            button = new Button(LauncherApplication.stringLocalizer.get(text));
            button.getStyleClass().add(getStyleClass(type));
            button.setOnAction(onPressed);
            button.setDisable(disabled);
        }

        private String getStyleClass(ButtonType type) {
            return switch (type) {
                case POSITIVE -> "positive";
                case NEUTRAL -> "neutral";
                case NEGATIVE -> "negative";
            };
        }

        public Button getButton() {
            return button;
        }

        public void setDisabled(boolean disabled) {
            button.setDisable(disabled);
        }
    }

    public static class PopupTextInput implements PopupControl {
        private final TextField textField;

        public PopupTextInput(String prompt) {
            this(prompt, null, null);
        }

        public PopupTextInput(String prompt, String defaultValue) {
            this(prompt, defaultValue, null);
        }

        public PopupTextInput(String prompt, Consumer<String> onUpdate) {
            this(prompt, null, onUpdate);
        }

        public PopupTextInput(String prompt, String defaultValue, Consumer<String> onUpdate) {
            textField = new TextField();
            textField.setPromptText(prompt == null ? "" : LauncherApplication.stringLocalizer.get(prompt));
            textField.setText(defaultValue == null ? "" : defaultValue);
            textField.textProperty().addListener(onUpdate == null ? (a,b,c) -> {} : (observable, oldValue, newValue) -> onUpdate.accept(newValue));
        }

        public Control getControl() {
            return textField;
        }

        public String getText() {
            return textField.getText();
        }

        public void setDisabled(boolean disabled) {
            textField.setDisable(disabled);
        }
    }

    public static class PopupComboBox<T> implements PopupControl {
        private final ComboBox<T> comboBox;

        public PopupComboBox(List<T> elements) {
            comboBox = new ComboBox<>();
            comboBox.getItems().addAll(elements);
        }

        public PopupComboBox(List<T> elements, T selected) {
            comboBox = new ComboBox<>();
            comboBox.getItems().addAll(elements);
            comboBox.getSelectionModel().select(selected);
        }

        public PopupComboBox(List<T> elements, int selected) {
            comboBox = new ComboBox<>();
            comboBox.getItems().addAll(elements);
            comboBox.getSelectionModel().select(selected);
        }

        public T getSelected() {
            return comboBox.getSelectionModel().getSelectedItem();
        }
        public int getSelectedIndex() {
            return comboBox.getSelectionModel().getSelectedIndex();
        }

        public void select(T value) {
            comboBox.getSelectionModel().select(value);
        }

        public void select(int index) {
            comboBox.getSelectionModel().select(index);
        }

        @Override
        public Control getControl() {
            return comboBox;
        }

        @Override
        public void setDisabled(boolean disabled) {
            comboBox.setDisable(disabled);
        }
    }

    public interface PopupControl {
        Control getControl();
        void setDisabled(boolean disabled);
    }

    private final VBox vbContainer = new VBox();
    private Label lbTitle;
    private Label lbMessage;
    private VBox vbControls;
    private HBox hbButtons;
    private Label lbError;

    public PopupElement(String title, String message) {
        this(PopupType.NONE, title, message, null, null);
    }

    public PopupElement(PopupType type, String title, String message) {
        this(type, title, message, null, null);
    }

    public PopupElement(PopupType type, String title, String message, List<PopupButton> buttons) {
        this(type, title, message, null, buttons);
    }

    public PopupElement(PopupType type, String title, String message, List<PopupControl> controls, List<PopupButton> buttons) {
        this.getStylesheets().add("/css/generic/PopupElement.css");
        this.getStyleClass().add("popup-background");
        ColumnConstraints c1 = new ColumnConstraints();
        ColumnConstraints c2 = new ColumnConstraints();
        ColumnConstraints c3 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        c2.setHgrow(Priority.NEVER);
        c3.setHgrow(Priority.ALWAYS);
        this.getColumnConstraints().addAll(c1, c2, c3);
        RowConstraints r1 = new RowConstraints();
        RowConstraints r2 = new RowConstraints();
        RowConstraints r3 = new RowConstraints();
        r1.setVgrow(Priority.ALWAYS);
        r2.setVgrow(Priority.NEVER);
        r3.setVgrow(Priority.ALWAYS);
        this.getRowConstraints().addAll(r1, r2, r3);

        setType(type);
        setTitle(title);
        setMessage(message);
        setControls(controls);
        setButtons(buttons);

        vbContainer.getStyleClass().add("popup-container");

        GridPane.setColumnIndex(vbContainer, 1);
        GridPane.setRowIndex(vbContainer, 1);
        this.getChildren().add(vbContainer);
    }

    public void setContent(String title, String message) {
        setTitle(title);
        setMessage(message);
    }

    public void setTitle(String title) {
        if(lbTitle == null && title != null) {
            lbTitle = new Label(LauncherApplication.stringLocalizer.get(title));
            lbTitle.getStyleClass().add("title");
            vbContainer.getChildren().add(0, lbTitle);
        } else if(title == null) {
            vbContainer.getChildren().remove(lbTitle);
            lbTitle = null;
        } else {
            lbTitle.setText(LauncherApplication.stringLocalizer.get(title));
        }
    }

    public void setMessage(String message) {
        if(lbMessage == null && message != null) {
            lbMessage = new Label(LauncherApplication.stringLocalizer.get(message));
            lbMessage.getStyleClass().add("message");
            vbContainer.getChildren().add(lbTitle == null ? 0 : 1, lbMessage);
        } else if(message == null) {
            vbContainer.getChildren().remove(lbMessage);
            lbMessage = null;
        } else {
            lbMessage.setText(LauncherApplication.stringLocalizer.get(message));
        }
    }

    public void setControls(List<PopupControl> controls) {
        if(vbControls == null && controls != null) {
            vbControls = new VBox();
            vbControls.setAlignment(Pos.CENTER);
            vbControls.getChildren().addAll(controls.stream().map(PopupControl::getControl).toList());
            vbContainer.getChildren().add(vbControls);
        } else if(controls == null) {
            vbContainer.getChildren().remove(vbControls);
            vbControls = null;
        } else {
            vbControls.getChildren().clear();
            vbControls.getChildren().addAll(controls.stream().map(PopupControl::getControl).toList());
        }
    }

    public void setButtons(List<PopupButton> buttons) {
        if(hbButtons == null && buttons != null) {
            hbButtons = new HBox();
            hbButtons.getStyleClass().add("button-container");
            hbButtons.getChildren().addAll(buttons.stream().map(PopupButton::getButton).toList());
            vbContainer.getChildren().add(hbButtons);
        } else if(buttons == null) {
            vbContainer.getChildren().remove(hbButtons);
            hbButtons = null;
        } else {
            hbButtons.getChildren().clear();
            hbButtons.getChildren().addAll(buttons.stream().map(PopupButton::getButton).toList());
        }
    }

    public void setError(String error) {
        if(lbError == null && error != null) {
            lbError = new Label(LauncherApplication.stringLocalizer.get(error));
            lbError.getStyleClass().add("error");
            vbContainer.getChildren().add(lbError);
        } else if(error == null) {
            vbContainer.getChildren().remove(lbError);
            lbError = null;
        } else {
            lbError.setText(LauncherApplication.stringLocalizer.get(error));
        }
    }

    public void setControlsDisabled(boolean disabled) {
        if(vbControls != null) {
            vbControls.getChildren().forEach(control -> control.setDisable(disabled));
        }
    }

    public void setButtonsDisabled(boolean disabled) {
        if(hbButtons != null) {
            hbButtons.getChildren().forEach(button -> button.setDisable(disabled));
        }
    }

    public void setType(PopupType type) {
        vbContainer.getStyleClass().remove("success");
        vbContainer.getStyleClass().remove("warning");
        vbContainer.getStyleClass().remove("error");
        switch (type) {
            case SUCCESS -> vbContainer.getStyleClass().add("success");
            case WARNING -> vbContainer.getStyleClass().add("warning");
            case ERROR -> vbContainer.getStyleClass().add("error");
        }
    }
}
