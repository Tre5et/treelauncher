package net.treset.minecraftlauncher.ui.generic;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ContentElement extends GridPane {
    protected final HBox iconContainer = new HBox();
    protected final ImageView icon = new ImageView();
    protected final VBox contentContainer = new VBox();
    protected final Label title = new Label();
    protected final Label details = new Label();

    public ContentElement(Image icon, String title, String details) {
        this.getStylesheets().add("css/generic/ContentElement.css");
        this.getStyleClass().add("element-container");

        this.icon.setImage(icon);
        this.icon.setFitHeight(64);
        this.icon.setFitWidth(64);
        this.icon.setPreserveRatio(true);

        this.title.setText(title);
        this.title.getStyleClass().add("element-title");

        this.details.setText(details);
        this.details.getStyleClass().add("element-details");

        this.iconContainer.getChildren().add(this.icon);
        this.iconContainer.getStyleClass().add("icon-container");
        this.add(this.iconContainer, 0, 0, 1, 3);


        this.contentContainer.getChildren().addAll(this.title, this.details);
        this.contentContainer.setAlignment(Pos.CENTER_LEFT);
        this.add(this.contentContainer, 1, 0, 1, 3);

    }
}
