package net.treset.minecraftlauncher.ui.manager;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.treset.mc_version_loader.files.FileUtils;
import net.treset.mc_version_loader.files.ModFileDownloader;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherMod;
import net.treset.mc_version_loader.launcher.LauncherModsDetails;
import net.treset.mc_version_loader.mods.ModData;
import net.treset.mc_version_loader.mods.ModVersionData;
import net.treset.minecraftlauncher.ui.base.UiElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ModsManagerElement extends UiElement {
    private static final Logger LOGGER = LogManager.getLogger(ModsManagerElement.class);

    @FXML private AnchorPane rootPane;
    @FXML private VBox currentModsBox;
    @FXML private VBox currentModsContainer;

    private Pair<LauncherManifest, LauncherModsDetails> details;
    private List<Pair<ModListElement, AnchorPane>> elements;

    public void setLauncherMods(Pair<LauncherManifest, LauncherModsDetails> details) {
        this.details = details;
    }


    @Override
    public void beforeShow(Stage stage){
        if(details == null || details.getValue().getMods() == null)
            return;
        elements = new ArrayList<>();
        for(LauncherMod m : details.getValue().getMods()) {
            try {
                elements.add(ModListElement.from(m, details.getValue().getModsVersion(), this::onInstallButtonClicked));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        currentModsContainer.getChildren().clear();
        for(Pair<ModListElement, AnchorPane> element : elements) {
            element.getKey().beforeShow(stage);
            currentModsContainer.getChildren().add(element.getValue());
        }
    }

    @Override
    public void afterShow(Stage stage) {
        for(Pair<ModListElement, AnchorPane> element : elements) {
            element.getKey().afterShow(stage);
        }
    }

    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }

    public void onInstallButtonClicked(ModVersionData versionData, LauncherMod modData, ModListElement source) {
        File oldFile = new File(details.getKey().getDirectory() + modData.getFileName());
        if(oldFile.exists() && !oldFile.delete()) {
            LOGGER.warn("Failed to delete old mod file");
            return;
        }
        LauncherMod newMod = ModFileDownloader.downloadModFile(versionData, new File(details.getKey().getDirectory()), modData.isEnabled());
        if(newMod == null) {
            LOGGER.warn("Failed to download mod file");
            return;
        }
        ArrayList<LauncherMod> mods = new ArrayList<>(details.getValue().getMods());
        mods.remove(modData);
        mods.add(newMod);
        details.getValue().setMods(mods);
        source.setMod(newMod);
        source.beforeShow(null);
        source.afterShow(null);
        details.getValue().writeToFile(details.getKey().getDirectory() + details.getKey().getDetails());
    }
}
