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

public class CreateSelectable extends HBox {

    private EventHandler<MouseEvent> onClick;

    public CreateSelectable() {
        VBox.setVgrow(this, Priority.NEVER);
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("create-container");
        VBox.setMargin(this, new Insets(10));
        setCursor(Cursor.HAND);
        setSpacing(10);

        FontIcon icon = new FontIcon();
        icon.getStyleClass().add("icon");
        icon.getStyleClass().add("add");
        icon.setIconSize(36);

        Label label = new Label(LauncherApplication.stringLocalizer.get("components.label.create"));
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
}
