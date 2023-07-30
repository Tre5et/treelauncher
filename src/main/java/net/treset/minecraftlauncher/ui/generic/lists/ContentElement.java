package net.treset.minecraftlauncher.ui.generic.lists;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

public class ContentElement extends GridPane {
    protected final HBox iconContainer = new HBox();
    protected final HBox iconBackground = new HBox();
    protected final ImageView ivIcon = new ImageView();
    protected final VBox contentContainer = new VBox();
    protected final Label title = new Label();
    protected final Label details = new Label();

    public ContentElement(Image icon, String title, String details) {
        this.getStylesheets().add("css/generic/lists/ContentElement.css");
        this.getStyleClass().add("element-container");

        ColumnConstraints constraints0 = new ColumnConstraints();
        constraints0.setHgrow(Priority.NEVER);
        this.getColumnConstraints().add(constraints0);
        ColumnConstraints constraints1 = new ColumnConstraints();
        constraints1.setHgrow(Priority.ALWAYS);
        this.getColumnConstraints().add(constraints1);

        this.ivIcon.setImage(icon);
        this.ivIcon.setFitHeight(64);
        this.ivIcon.setFitWidth(64);
        this.ivIcon.setPreserveRatio(true);
        this.iconBackground.getStyleClass().add("icon-container");
        this.iconBackground.setAlignment(Pos.CENTER);
        this.iconBackground.getChildren().add(this.ivIcon);
        this.iconContainer.setFillHeight(false);
        this.iconContainer.setAlignment(Pos.CENTER);
        this.iconContainer.getChildren().add(this.iconBackground);
        this.add(this.iconContainer, 0, 0, 1, 3);

        this.title.setText(title);
        this.title.getStyleClass().add("element-title");

        this.details.setText(details);
        this.details.getStyleClass().add("element-details");

        this.contentContainer.getChildren().addAll(this.title, this.details);
        this.contentContainer.setAlignment(Pos.CENTER_LEFT);
        this.contentContainer.setPrefWidth(100);
        this.add(this.contentContainer, 1, 0, 1, 3);

    }

    public void setIcon(Image icon) {
        this.ivIcon.setImage(icon);
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public void setDetails(String details) {
        this.details.setText(details);
    }
}
