package net.treset.minecraftlauncher.ui.generic;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import net.treset.minecraftlauncher.LauncherApplication;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Objects;

public class ButtonElement extends HBox {
    FontIcon icon = new FontIcon();
    Label label = new Label();

    private EventHandler<MouseEvent> onClick;

    private String iconClass;
    private String text;

    private boolean noMargin = false;

    public ButtonElement() {
        getStylesheets().add("/css/generic/ButtonElement.css");

        VBox.setVgrow(this, Priority.NEVER);
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("button-container");
        VBox.setMargin(this, new Insets(10));
        setCursor(Cursor.HAND);
        setSpacing(10);

        icon.getStyleClass().add("icon");
        icon.setIconSize(36);

        label.getStyleClass().add("element-title");

        getChildren().addAll(icon, label);
    }

    public EventHandler<MouseEvent> getOnClick() {
        return onClick;
    }

    public void setOnClick(EventHandler<MouseEvent> onClick) {
        this.onClick = onClick;
        onMouseClickedProperty().set(onClick);
    }

    public String getIconClass() {
        return iconClass;
    }

    public void setIconClass(String iconClass) {
        if(Objects.equals(this.iconClass, iconClass)) return;
        icon.getStyleClass().remove(this.iconClass);
        this.iconClass = iconClass;
        icon.getStyleClass().add(iconClass);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        label.setText(LauncherApplication.stringLocalizer.get(text));
    }

    public boolean isNoMargin() {
        return noMargin;
    }

    public void setNoMargin(boolean noMargin) {
        this.noMargin = noMargin;
        if(noMargin) {
            VBox.setMargin(this, new Insets(0));
        } else {
            VBox.setMargin(this, new Insets(10));
        }
    }
}
