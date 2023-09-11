package net.treset.minecraftlauncher.ui.generic.lists;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class FolderContentContainer extends ScrollPane {
    protected final List<ContentElement> elements = new ArrayList<>();

    protected final VBox container = new VBox();

    private File folder = null;

    private EventHandler<MouseEvent> onSelect = null;

    public FolderContentContainer() {
        this.getStylesheets().add("css/generic/FolderContentContainer.css");
        this.getStyleClass().add("element-container");

        this.container.getStyleClass().add("content-container");

        this.setContent(container);
    }

    public void setFolder(File folder) {
        if(!folder.exists() || !folder.isDirectory()) {
            throw new IllegalArgumentException("Folder does not exist or is not a folder");
        }
        this.folder = folder;

        new Thread(this::updateElements).start();
    }

    protected void updateElements() {
        elements.clear();
        File[] files = folder.listFiles();

        if(files == null) {
            return;
        }

        for(File file: files) {
            ContentElement element = createElement(file);
            if(element == null) {
                continue;
            }
            this.elements.add(element);
        }

        Platform.runLater(() -> {
            this.container.getChildren().clear();
            this.container.getChildren().addAll(elements);
        });
    }

    protected abstract ContentElement createElement(File file);

    protected void onSelect(MouseEvent event) {
        ContentElement source = (ContentElement) event.getSource();
        boolean select = !source.isSelected();
        for(ContentElement element: elements) {
            element.setSelected(false);
        }
        if(select) {
            source.setSelected(true);
        }
        if(getOnSelect() != null) {
            getOnSelect().handle(event);
        }
    }

    public ContentElement getSelected() {
        for(ContentElement element: elements) {
            if(element.isSelected()) {
                return element;
            }
        }
        return null;
    }

    public void clearSelect() {
        for(ContentElement element: elements) {
            element.setSelected(false);
        }
    }

    public EventHandler<MouseEvent> getOnSelect() {
        return onSelect;
    }

    public void setOnSelect(EventHandler<MouseEvent> onSelect) {
        this.onSelect = onSelect;
    }
}
