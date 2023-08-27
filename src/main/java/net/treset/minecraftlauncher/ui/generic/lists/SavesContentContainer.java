package net.treset.minecraftlauncher.ui.generic.lists;

import javafx.application.Platform;
import javafx.scene.control.Label;
import net.treset.mc_version_loader.saves.Save;
import net.treset.mc_version_loader.saves.Server;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.util.ImageUtil;
import net.treset.minecraftlauncher.util.QuickPlayData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SavesContentContainer extends FolderContentContainer {
    private File serversFile;
    private final List<ContentElement> serverElements = new ArrayList<>();

    @Override
    protected ContentElement createElement(File file) {
        try {
            Save save = Save.from(file);
            return new ContentElement(save.getImage() == null ? null : ImageUtil.getImage(save.getImage()), save.getName(), save.getFileName(), this::onSelect);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void updateElements() {
        Label savesLabel = new Label(LauncherApplication.stringLocalizer.get("selector.saves.label.worlds"));
        savesLabel.getStyleClass().add("subtitle");

        super.updateElements();

        Platform.runLater(() -> this.container.getChildren().add(0, savesLabel));

        if(serversFile == null) {
            return;
        }

        List<Server> servers;
        try {
            servers = Server.from(serversFile);
        } catch (IOException e) {
            LauncherApplication.displayError(e);
            return;
        }

        this.serverElements.clear();
        for(Server server: servers) {
            ContentElement element = new ContentElement(
                    server.getImage() == null ? null : ImageUtil.getImage(server.getImage()),
                    server.getName(),
                    server.getIp(),
                    this::onSelect
            );
            this.serverElements.add(element);
        }

        this.elements.addAll(serverElements);

        Label serversLabel = new Label(LauncherApplication.stringLocalizer.get("selector.saves.label.servers"));
        serversLabel.getStyleClass().add("subtitle");

        Platform.runLater(() -> {
            this.container.getChildren().add(serversLabel);
            this.container.getChildren().addAll(serverElements);
        });
    }

    @Override
    public ContentElement getSelected() {
        ContentElement selectedWorld = super.getSelected();
        if(selectedWorld != null) {
            return selectedWorld;
        }

        for(ContentElement element: serverElements) {
            if(element.isSelected()) {
                return element;
            }
        }
        return null;
    }

    public QuickPlayData getQuickPlayData() {
        for(ContentElement element: elements) {
            if(element.isSelected()) {
                return new QuickPlayData(QuickPlayData.Type.WORLD, element.getDetails());
            }
        }
        for(ContentElement element: serverElements) {
            if(element.isSelected()) {
                return new QuickPlayData(QuickPlayData.Type.SERVER, element.getDetails());
            }
        }
        return null;
    }

    public void setServersFile(File file) {
        serversFile = file;
    }
}
