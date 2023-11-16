package net.treset.minecraftlauncher.ui.generic.lists;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class SelectorEntryElement<E extends SelectorEntryElement.ContentProvider> extends StackPane {
    public interface ContentProvider {
        String getTitle();
        String getDetails();
    }

    protected boolean selected = false;
    private E contentProvider;
    private BiConsumer<E, Boolean> selectionListener;

    protected final VBox vbContainer = new VBox();
    protected final HBox hbTitle = new HBox();
    protected final Label lbTitle = new Label();
    protected final Label lbDetails = new Label();
    protected final FontIcon icSync = new FontIcon();

    private boolean locked = false;


    public SelectorEntryElement(E contentProvider, BiConsumer<E, Boolean> selectionListener, Supplier<Boolean> syncStatus) {
        this.selectionListener = selectionListener;
        setContentProvider(contentProvider);

        this.getStylesheets().add("/css/generic/SelectorEntryElement.css");

        lbTitle.getStyleClass().add("element-title");

        lbDetails.getStyleClass().add("element-details");

        icSync.getStyleClass().addAll("icon", "sync");
        icSync.setIconSize(24);

        hbTitle.setAlignment(Pos.CENTER);
        hbTitle.setSpacing(10);
        hbTitle.getChildren().add(lbTitle);

        vbContainer.getStyleClass().add("element-container");
        vbContainer.setAlignment(Pos.CENTER);
        vbContainer.setOnMouseClicked(event -> onClick());
        vbContainer.setCursor(Cursor.HAND);
        if(syncStatus != null && syncStatus.get()) {
            hbTitle.getChildren().add(icSync);
        }
        vbContainer.getChildren().addAll(hbTitle, lbDetails);

        this.getStyleClass().add("element-root");
        this.getChildren().add(vbContainer);
    }

    public void onClick() {
        select(!selected, false, true);
    }

    public boolean select(boolean select, boolean force, boolean callback) {
        if(selected != select && (force || contentProvider != null && !locked)) {
            if(select) {
                vbContainer.getStyleClass().add("selected");
                lbTitle.getStyleClass().add("selected");
                lbDetails.getStyleClass().add("selected");
            } else {
                vbContainer.getStyleClass().remove("selected");
                lbTitle.getStyleClass().remove("selected");
                lbDetails.getStyleClass().remove("selected");
            }
            if(callback) {
                selectionListener.accept(contentProvider, select);
            }
            selected = select;
            return true;
        }
        return false;
    }

    public E getContentProvider() {
        return contentProvider;
    }

    public void setContentProvider(E contentProvider) {
        this.contentProvider = contentProvider;
        lbTitle.setText(contentProvider.getTitle());
        lbDetails.setText(contentProvider.getDetails());
    }

    public boolean isSelected() {
        return selected;
    }

    public BiConsumer<E, Boolean> getSelectionListener() {
        return selectionListener;
    }

    public void setSelectionListener(BiConsumer<E, Boolean> selectionListener) {
        this.selectionListener = selectionListener;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
