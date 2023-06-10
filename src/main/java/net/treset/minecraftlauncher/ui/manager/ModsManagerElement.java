package net.treset.minecraftlauncher.ui.manager;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.treset.mc_version_loader.VersionLoader;
import net.treset.mc_version_loader.exception.FileDownloadException;
import net.treset.mc_version_loader.files.ModFileDownloader;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherMod;
import net.treset.mc_version_loader.launcher.LauncherModDownload;
import net.treset.mc_version_loader.launcher.LauncherModsDetails;
import net.treset.mc_version_loader.minecraft.MinecraftVersion;
import net.treset.mc_version_loader.mods.ModData;
import net.treset.mc_version_loader.mods.ModVersionData;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.ui.base.UiElement;
import net.treset.minecraftlauncher.ui.generic.PopupElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ModsManagerElement extends UiElement {
    private static final Logger LOGGER = LogManager.getLogger(ModsManagerElement.class);

    @FXML private AnchorPane rootPane;
    @FXML private VBox currentModsBox;
    @FXML private VBox currentModsContainer;
    @FXML private ComboBox<String> versionSelector;
    @FXML private CheckBox snapshotsCheck;
    @FXML private Button reloadButton;
    @FXML private ModsSearchElement modSearchController;
    @FXML private PopupElement popupController;

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

    @FXML
    private void onSnapshotsChecked() {
        populateVersionChoice();
    }

    @FXML private void onReloadButtonClicked() {
        if(versionSelector.getSelectionModel().getSelectedItem() != null && !Objects.equals(versionSelector.getSelectionModel().getSelectedItem(), details.getValue().getModsVersion())) {
            popupController.setType(PopupElement.PopupType.WARNING);
            popupController.setContent("mods.manager.popup.change.title", "mods.manager.popup.change.message");
            popupController.clearButtons();
            popupController.addButtons(
                    new PopupElement.PopupButton(PopupElement.ButtonType.NEGATIVE,
                            "mods.manager.popup.change.cancel", "cancelButton",
                            this::onVersionChangeCanceled),
                    new PopupElement.PopupButton(PopupElement.ButtonType.POSITIVE,
                            "mods.manager.popup.change.accept", "acceptButton",
                            this::onVersionChangeAccepted)
            );
            popupController.setVisible(true);
        }
    }

    private void onVersionChangeAccepted(String id) {
        popupController.setVisible(false);
        details.getValue().setModsVersion(versionSelector.getSelectionModel().getSelectedItem());
        try {
            details.getValue().writeToFile(details.getKey().getDirectory() + details.getKey().getDetails());
        } catch (IOException e) {
            LauncherApplication.displayError(e);
        }
        modSearchController.init(details.getValue().getModsVersion(), details.getValue().getModsType(), this::onInstallButtonClicked, this::onSearchBackClicked, details.getValue().getMods());
        onVersionSelected();
        reloadMods();
    }

    private void onVersionChangeCanceled(String id) {
        popupController.setVisible(false);
        versionSelector.getSelectionModel().select(details.getValue().getModsVersion());
        onVersionSelected();
    }

    private void onVersionSelected() {
        reloadButton.setDisable(details.getValue().getModsVersion().equals(versionSelector.getSelectionModel().getSelectedItem()));
    }

    @Override
    public void beforeShow(Stage stage){
        if(details == null || details.getValue().getMods() == null)
            return;
        modSearchController.init(details.getValue().getModsVersion(), details.getValue().getModsType(), this::onInstallButtonClicked, this::onSearchBackClicked, details.getValue().getMods());
        snapshotsCheck.setSelected(false);
        versionSelector.getItems().clear();
        versionSelector.setDisable(true);
        versionSelector.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(this::onVersionSelected);
        });
        popupController.setVisible(false);
        currentModsBox.setVisible(true);
        modSearchController.setVisible(false);
    }

    private void reloadMods() {
        currentModsContainer.getChildren().clear();
        new Thread(() -> {
            elements = new ArrayList<>();
            for(LauncherMod m : details.getValue().getMods()) {
                try {
                    elements.add(ModListElement.from(m, details.getValue().getModsVersion(), this::onInstallButtonClicked, this::onDisableButtonClicked, this::onDeleteButtonClicked));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            elements.sort(Comparator.comparing(e -> e.getKey().getMod().getName()));
            elements.forEach(e -> e.getKey().beforeShow(null));
            Platform.runLater(() -> {
                currentModsContainer.getChildren().addAll(elements.stream().map(Pair::getValue).toList());
                elements.forEach(e -> e.getKey().afterShow(null));
            });
        }).start();

    }

    private void populateVersionChoice() {
        versionSelector.getItems().clear();
        versionSelector.setPromptText(LauncherApplication.stringLocalizer.get("creator.version.prompt.loading"));
        versionSelector.setDisable(true);
        versionSelector.getItems().add(details.getValue().getModsVersion());
        versionSelector.getSelectionModel().select(0);
        new Thread(() -> {
            try {
                List <String> names = (snapshotsCheck.isSelected() ? VersionLoader.getVersions() : VersionLoader.getReleases()).stream()
                        .map(MinecraftVersion::getId)
                        .filter(s -> !s.equals(details.getValue().getModsVersion()))
                        .toList();
                Platform.runLater(() -> {
                    versionSelector.getItems().addAll(names);
                    versionSelector.setPromptText(LauncherApplication.stringLocalizer.get("creator.version.prompt.version"));
                    versionSelector.setDisable(false);
                });
            } catch (FileDownloadException e) {
               LauncherApplication.displayError(e);
            }
        }).start();
    }

    @Override
    public void afterShow(Stage stage) {
        reloadMods();
        populateVersionChoice();
    }

    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }

    public void onInstallButtonClicked(ModVersionData versionData, LauncherMod modData, ModListElement source) {
        downloadMod(versionData, modData, source);
    }

    public boolean onDisableButtonClicked(boolean disabled, LauncherMod modData) {
        modData.setEnabled(!disabled);
        File modFile = new File(details.getKey().getDirectory() + modData.getFileName() + (disabled ? "" : ".disabled"));
        File newFile = new File(details.getKey().getDirectory(), modData.getFileName() + (disabled ? ".disabled" : ""));
        try {
            Files.move(modFile.toPath(), newFile.toPath());
        } catch(IOException e) {
            LauncherApplication.displayError(e);
            return false;
        }
        try {
            details.getValue().writeToFile(details.getKey().getDirectory() + details.getKey().getDetails());
        } catch (IOException e) {
            LauncherApplication.displayError(e);
            return true;
        }
        return true;
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
        try {
            details.getValue().writeToFile(details.getKey().getDirectory() + details.getKey().getDetails());
        } catch (IOException e){
            LauncherApplication.displayError(e);
            return;
        }

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
            ArrayList<LauncherMod> mods = new ArrayList<>(details.getValue().getMods());
            if (modData != null) {
                mods.remove(modData);
            }
            LauncherMod newMod = downloadAndAdd(versionData, mods);
            if (newMod == null) {
                LOGGER.warn("Failed to download mod file");
                Platform.runLater(() -> {
                    source.beforeShow(null);
                    source.afterShow(null);
                });
                return;
            }
            details.getValue().setMods(mods);
            source.setMod(newMod);
            try {
                details.getValue().writeToFile(details.getKey().getDirectory() + details.getKey().getDetails());
            } catch (IOException e) {
                LauncherApplication.displayError(e);
            }
            Platform.runLater(() -> {
                source.beforeShow(null);
                source.afterShow(null);
            });
        }).start();
    }

    private LauncherMod downloadAndAdd(ModVersionData versionData, ArrayList<LauncherMod> mods) {
        LauncherMod newMod = null;
        try {
            newMod = ModFileDownloader.downloadModFile(versionData, new File(details.getKey().getDirectory()), true);
        } catch (FileDownloadException e) {
            LauncherApplication.displayError(e);
        }

        mods.add(newMod);

        try {
            for(ModVersionData d : versionData.getRequiredDependencies(details.getValue().getModsVersion(), details.getValue().getModsType())) {
                if(d != null && d.getParentMod() != null && !modExists(d.getParentMod()) && downloadAndAdd(d, mods) == null) {
                    LauncherApplication.displayError(new FileDownloadException("Failed to download mod dependency file"));
                    return null;
                }
            }
        } catch (FileDownloadException e) {
            LauncherApplication.displayError(e);
        }
        return newMod;
    }

    private boolean modExists(ModData modData) {
        for(LauncherMod m : details.getValue().getMods()) {
            for(LauncherModDownload d : m.getDownloads()) {
                if(modData.getProjectIds().stream().anyMatch(id -> id.equals(d.getId()))) {
                    return true;
                }
            }
        }
        return false;
    }

    public void onSearchBackClicked() {
        currentModsBox.setVisible(true);
        modSearchController.setVisible(false);
        reloadMods();
    }
}
