package net.treset.minecraftlauncher.ui.generic.lists;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import net.treset.mc_version_loader.saves.Server;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.util.ImageUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServersContentContainer extends ScrollPane {
    private final List<ContentElement> elements = new ArrayList<>();

    private final VBox container = new VBox();

    private EventHandler<MouseEvent> onSelect = null;

    public ServersContentContainer() {
        this.getStylesheets().add("css/generic/FolderContentContainer.css");
        this.getStyleClass().add("element-container");

        this.container.getStyleClass().add("content-container");

        this.setContent(container);
    }

    public void populate(File serversFile) {
        List<Server> servers;
        try {
            servers = Server.from(serversFile);
        } catch (IOException e) {
            LauncherApplication.displayError(e);
            return;
        }

        this.elements.clear();
        for(Server server: servers) {
            ContentElement element = new ContentElement(
                    server.getImage() == null ? null : ImageUtil.getImage(server.getImage()),
                    server.getName(),
                    server.getIp(),
                    this::onSelect
            );
            this.elements.add(element);
        }

        Platform.runLater(() -> this.container.getChildren().addAll(elements));
    }

    public void clear() {
        Platform.runLater(() -> this.container.getChildren().clear());
    }

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
