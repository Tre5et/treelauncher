package net.treset.minecraftlauncher.ui.generic;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.effect.BlendMode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import net.treset.minecraftlauncher.LauncherApplication;


public class Titlebar extends VBox {
    private final GridPane gpTitle = new GridPane();
    private final Label lbTitle = new Label();
    private final IconButton btNews = new IconButton();
    private EventHandler<ActionEvent> onNews;

    public Titlebar() {
        super();

        lbTitle.setText(LauncherApplication.stringLocalizer.get("launcher.name"));
        lbTitle.getStyleClass().add("title");
        lbTitle.setAlignment(Pos.BOTTOM_CENTER);
        lbTitle.setContentDisplay(ContentDisplay.BOTTOM);
        lbTitle.setTextAlignment(TextAlignment.CENTER);

        btNews.getStyleClass().add("news");
        btNews.setIconSize(32);
        btNews.setBlendMode(BlendMode.DIFFERENCE);

        ColumnConstraints side = new ColumnConstraints();
        side.setPercentWidth(7);
        side.setHalignment(HPos.LEFT);
        side.setHgrow(Priority.NEVER);
        ColumnConstraints middle = new ColumnConstraints();
        middle.setHgrow(Priority.ALWAYS);
        middle.setHalignment(HPos.CENTER);
        GridPane.setColumnIndex(lbTitle, 1);
        GridPane.setColumnIndex(btNews, 2);

        gpTitle.setAlignment(Pos.BOTTOM_CENTER);
        gpTitle.setPrefHeight(48);
        gpTitle.getStyleClass().add("title-container");
        gpTitle.getColumnConstraints().addAll(side, middle, side);
        gpTitle.getChildren().add(lbTitle);

        setAlignment(Pos.BOTTOM_CENTER);
        setPrefHeight(48);
        getStyleClass().add("element");
        getStylesheets().add("/css/title/Titlebar.css");
        getChildren().add(gpTitle);
    }

    public EventHandler<ActionEvent> getOnNews() {
        return onNews;
    }

    public void setOnNews(EventHandler<ActionEvent> onNews) {
        this.onNews = onNews;
        btNews.setOnAction(onNews);
        if(onNews != null) {
            if(!gpTitle.getChildren().contains(btNews)) {
                gpTitle.getChildren().add(btNews);
            }
        } else {
            gpTitle.getChildren().remove(btNews);
        }
    }
}
