package net.treset.minecraftlauncher.ui.generic;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import net.treset.minecraftlauncher.LauncherApplication;

public class ActionBar extends HBox {
    private final IconButton btPlay = new IconButton();
    private final IconButton btEdit = new IconButton();
    private final IconButton btFolder = new IconButton();
    private final IconButton btDelete = new IconButton();
    private final IconButton btSync = new IconButton();
    private final Label lbTitle = new Label();

    private boolean showPlay = false;
    private boolean showEdit = true;
    private boolean showFolder = true;
    private boolean showDelete = true;
    private boolean showSync = false;
    private String defaultLabel = "menu.label";
    private EventHandler<ActionEvent> onPlay;
    private EventHandler<ActionEvent> onEdit;
    private EventHandler<ActionEvent> onFolder;
    private EventHandler<ActionEvent> onDelete;
    private EventHandler<ActionEvent> onSync;

    public ActionBar() {
        getStyleClass().add("title-container");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(10);
        setPadding(new Insets(0, 10, 0, 10));

        btPlay.setIconSize(42);
        btPlay.getStyleClass().add("play");
        btPlay.getStyleClass().add("highlight");
        btPlay.setTooltipText(LauncherApplication.stringLocalizer.get("menu.tooltip.play"));
        btEdit.setIconSize(36);
        btEdit.getStyleClass().add("edit");
        btEdit.setTooltipText(LauncherApplication.stringLocalizer.get("menu.tooltip.edit"));
        btFolder.setIconSize(36);
        btFolder.getStyleClass().add("folder");
        btFolder.getStyleClass().add("neutral");
        btFolder.setTooltipText(LauncherApplication.stringLocalizer.get("menu.tooltip.folder"));
        btDelete.setIconSize(36);
        btDelete.getStyleClass().add("delete");
        btDelete.getStyleClass().add("negative");
        btDelete.setTooltipText(LauncherApplication.stringLocalizer.get("menu.tooltip.delete"));
        btSync.setIconSize(36);
        btSync.getStyleClass().add("upload");
        btSync.getStyleClass().add("neutral");
        btSync.setTooltipText(LauncherApplication.stringLocalizer.get("menu.tooltip.sync"));
        lbTitle.getStyleClass().add("title");
        setLabel(defaultLabel);
        updateButtons();
    }

    public boolean isShowPlay() {
        return showPlay;
    }

    public void setShowPlay(boolean showPlay) {
        this.showPlay = showPlay;
        updateButtons();
    }

    public boolean isShowEdit() {
        return showEdit;
    }

    public void setShowEdit(boolean showEdit) {
        this.showEdit = showEdit;
        updateButtons();
    }

    public boolean isShowFolder() {
        return showFolder;
    }

    public void setShowFolder(boolean showFolder) {
        this.showFolder = showFolder;
        updateButtons();
    }

    public boolean isShowDelete() {
        return showDelete;
    }

    public void setShowDelete(boolean showDelete) {
        this.showDelete = showDelete;
        updateButtons();
    }

    public boolean isShowSync() {
        return showSync;
    }

    public void setShowSync(boolean showSync) {
        this.showSync = showSync;
        updateButtons();
    }

    public String getDefaultLabel() {
        return defaultLabel;
    }

    public void setDefaultLabel(String defaultLabel) {
        if(lbTitle.getText().equals(LauncherApplication.stringLocalizer.get(this.defaultLabel))) {
            setLabel(defaultLabel);
        }
        this.defaultLabel = defaultLabel;
    }

    public EventHandler<ActionEvent> getOnPlay() {
        return onPlay;
    }

    public void setOnPlay(EventHandler<ActionEvent> onPlay) {
        this.onPlay = onPlay;
        btPlay.setOnAction(onPlay);
    }

    public EventHandler<ActionEvent> getOnEdit() {
        return onEdit;
    }

    public void setOnEdit(EventHandler<ActionEvent> onEdit) {
        this.onEdit = onEdit;
        btEdit.setOnAction(onEdit);
    }

    public EventHandler<ActionEvent> getOnFolder() {
        return onFolder;
    }

    public void setOnFolder(EventHandler<ActionEvent> onFolder) {
        this.onFolder = onFolder;
        btFolder.setOnAction(onFolder);
    }

    public EventHandler<ActionEvent> getOnDelete() {
        return onDelete;
    }

    public void setOnDelete(EventHandler<ActionEvent> onDelete) {
        this.onDelete = onDelete;
        btDelete.setOnAction(onDelete);
    }

    public EventHandler<ActionEvent> getOnSync() {
        return onSync;
    }

    public void setOnSync(EventHandler<ActionEvent> onSync) {
        this.onSync = onSync;
        btSync.setOnAction(onSync);
    }

    public void setLabel(String label) {
        lbTitle.setText(LauncherApplication.stringLocalizer.get(label));
    }

    public void clearLabel() {
        setLabel(defaultLabel);
    }

    private void updateButtons() {
        getChildren().clear();
        if(showPlay) {
            getChildren().add(btPlay);
        }
        getChildren().add(lbTitle);
        if(showEdit) {
            getChildren().add(btEdit);
        }
        if(showFolder) {
            getChildren().add(btFolder);
        }
        if(showDelete) {
            getChildren().add(btDelete);
        }
        if(showSync) {
            getChildren().add(btSync);
        }
    }
}
