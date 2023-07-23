package net.treset.minecraftlauncher.ui.generic.lists;

import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class FolderContentContainer extends ScrollPane {
    private final List<ContentElement> elements = new ArrayList<>();

    private final VBox container = new VBox();

    private File folder = null;

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
}
