package net.treset.minecraftlauncher.ui.manager;

import javafx.application.Platform;
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
import net.treset.mc_version_loader.mods.ModVersionData;
import net.treset.minecraftlauncher.ui.base.UiElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ModsManagerElement extends UiElement {
    private static final Logger LOGGER = LogManager.getLogger(ModsManagerElement.class);

    @FXML private AnchorPane rootPane;
    @FXML private VBox currentModsBox;
    @FXML private VBox currentModsContainer;
    @FXML private ModsSearchElement modSearchController;

    private Pair<LauncherManifest, LauncherModsDetails> details;
    private List<Pair<ModListElement, AnchorPane>> elements;

    public void setLauncherMods(Pair<LauncherManifest, LauncherModsDetails> details) {
        this.details = details;
    }

    @FXML
    private void onAddButtonClicked() {
        currentModsBox.setVisible(false);
        modSearchController.setCurrentMods(details.getValue().getMods());
        modSearchController.setVisible(true);
    }

    @Override
    public void beforeShow(Stage stage){
        if(details == null || details.getValue().getMods() == null)
            return;
        modSearchController.init(details.getValue().getModsVersion(), details.getValue().getModsType(), this::onInstallButtonClicked, this::onSearchBackClicked, details.getValue().getMods());
        reloadMods();
        currentModsBox.setVisible(true);
        modSearchController.setVisible(false);
    }

    private void reloadMods() {
        elements = new ArrayList<>();
        for(LauncherMod m : details.getValue().getMods()) {
            try {
                elements.add(ModListElement.from(m, details.getValue().getModsVersion(), this::onInstallButtonClicked, this::onDisableButtonClicked, this::onDeleteButtonClicked));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        elements.sort(Comparator.comparing(e -> e.getKey().getMod().getName()));
        currentModsContainer.getChildren().clear();
        for(Pair<ModListElement, AnchorPane> element : elements) {
            element.getKey().beforeShow(null);
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
        downloadMod(versionData, modData, source);}

    public boolean onDisableButtonClicked(boolean disabled, LauncherMod modData) {
        modData.setEnabled(!disabled);
        File modFile = new File(details.getKey().getDirectory() + modData.getFileName() + (disabled ? "" : ".disabled"));
        File newFile = new File(details.getKey().getDirectory(), modData.getFileName() + (disabled ? ".disabled" : ""));
        try {
            Files.move(modFile.toPath(), newFile.toPath());
        } catch(IOException e) {
            LOGGER.warn("Failed to rename mod file");
            return false;
        }
        return details.getValue().writeToFile(details.getKey().getDirectory() + details.getKey().getDetails());
    }

    public void onDeleteButtonClicked(LauncherMod modData, ModListElement source) {
        ArrayList<LauncherMod> mods = new ArrayList<>(details.getValue().getMods());
        mods.remove(modData);

        File modFile = new File(details.getKey().getDirectory(), modData.getFileName() + (modData.isEnabled() ? "" : ".disabled"));
        if(!modFile.delete()) {
            LOGGER.warn("Failed to delete mod file");
            return;
        }

        details.getValue().setMods(mods);
        details.getValue().writeToFile(details.getKey().getDirectory() + details.getKey().getDetails());

        Pair<ModListElement, AnchorPane> element = null;
        for(Pair<ModListElement, AnchorPane> e : elements) {
            if(e.getKey().equals(source)) {
                element = e;
                break;
            }
        }
        if(element != null) {
            currentModsContainer.getChildren().remove(element.getValue());
        }
    }

    private void downloadMod(ModVersionData versionData, LauncherMod modData, ModListElement source) {
        source.setDownloading(true);
        new Thread(() -> {
            if (modData != null) {
                File oldFile = new File(details.getKey().getDirectory() + modData.getFileName());
                if (oldFile.exists() && !oldFile.delete()) {
                    LOGGER.warn("Failed to delete old mod file");
                    Platform.runLater(() -> {
                        source.beforeShow(null);
                        source.afterShow(null);
                    });
                    return;
                }
            }
            LauncherMod newMod = ModFileDownloader.downloadModFile(versionData, new File(details.getKey().getDirectory()), true);
            if (newMod == null) {
                LOGGER.warn("Failed to download mod file");
                Platform.runLater(() -> {
                    source.beforeShow(null);
                    source.afterShow(null);
                });
                return;
            }
            ArrayList<LauncherMod> mods = new ArrayList<>(details.getValue().getMods());
            if (modData != null) {
                mods.remove(modData);
            }
            mods.add(newMod);
            details.getValue().setMods(mods);
            source.setMod(newMod);
            details.getValue().writeToFile(details.getKey().getDirectory() + details.getKey().getDetails());
            Platform.runLater(() -> {
                source.beforeShow(null);
                source.afterShow(null);
            });
        }).start();
    }

    public void onSearchBackClicked() {
        currentModsBox.setVisible(true);
        modSearchController.setVisible(false);
        reloadMods();
        afterShow(null);
    }
}
