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
import net.treset.mc_version_loader.exception.FileDownloadException;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherMod;
import net.treset.mc_version_loader.launcher.LauncherModDownload;
import net.treset.mc_version_loader.launcher.LauncherModsDetails;
import net.treset.mc_version_loader.minecraft.MinecraftUtil;
import net.treset.mc_version_loader.minecraft.MinecraftVersion;
import net.treset.mc_version_loader.mods.ModData;
import net.treset.mc_version_loader.mods.ModUtil;
import net.treset.mc_version_loader.mods.ModVersionData;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.ui.base.UiElement;
import net.treset.minecraftlauncher.ui.generic.PopupElement;
import net.treset.minecraftlauncher.ui.generic.lists.ModContentElement;
import net.treset.minecraftlauncher.util.FileUtil;
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
    @FXML private VBox vbCurrentMods;
    @FXML private CheckBox chUpdate;

    @FXML private CheckBox chDisable;
    @FXML private VBox currentModsContainer;
    @FXML private ComboBox<String> cbVersion;
    @FXML private CheckBox chSnapshots;
    @FXML private Button btReload;
    @FXML private ModsSearchElement icModSearchController;
    @FXML private PopupElement icPopupController;

    private Pair<LauncherManifest, LauncherModsDetails> details;
    private List<ModContentElement> elements;

    public void setLauncherMods(Pair<LauncherManifest, LauncherModsDetails> details) {
        this.details = details;
    }

    @FXML
    private void onAdd() {
        vbCurrentMods.setVisible(false);
        icModSearchController.setCurrentMods(details.getValue().getMods());
        icModSearchController.setVisible(true);
    }

    @FXML
    private void onUpdate(){
        for(ModContentElement e : elements) {
            e.checkUpdate();
            if(chUpdate.isSelected()) {
                e.changeVersion();
            }
            if(chDisable.isSelected() && !e.hasValidVersion()) {
                //TODO: This is still very broken
                Platform.runLater(() -> enableMod(false, e));
            }
        }
    }

    @FXML
    private void onCheckSnapshots() {
        populateVersionChoice();
    }

    @FXML
    private void onReload() {
        if(cbVersion.getSelectionModel().getSelectedItem() != null && !Objects.equals(cbVersion.getSelectionModel().getSelectedItem(), details.getValue().getModsVersion())) {
            icPopupController.setType(PopupElement.PopupType.WARNING);
            icPopupController.setContent("mods.manager.popup.change.title", "mods.manager.popup.change.message");
            icPopupController.clearControls();
            icPopupController.addButtons(
                    new PopupElement.PopupButton(PopupElement.ButtonType.NEGATIVE,
                            "mods.manager.popup.change.cancel", "cancelButton",
                            this::onVersionChangeCanceled),
                    new PopupElement.PopupButton(PopupElement.ButtonType.POSITIVE,
                            "mods.manager.popup.change.accept", "acceptButton",
                            this::onVersionChangeAccepted)
            );
            icPopupController.setVisible(true);
        }
    }

    private void onVersionChangeAccepted(String id) {
        icPopupController.setVisible(false);
        details.getValue().setModsVersion(cbVersion.getSelectionModel().getSelectedItem());
        try {
            details.getValue().writeToFile(details.getKey().getDirectory() + details.getKey().getDetails());
        } catch (IOException e) {
            LauncherApplication.displayError(e);
        }
        //TODO: icModSearchController.init(details.getValue().getModsVersion(), details.getValue().getModsType(), this::onInstallButtonClicked, this::onSearchBackClicked, details.getValue().getMods(), this::addMod);
        onVersionSelected();
        reloadMods();
    }

    private void onVersionChangeCanceled(String id) {
        icPopupController.setVisible(false);
        cbVersion.getSelectionModel().select(details.getValue().getModsVersion());
        onVersionSelected();
    }

    private void onVersionSelected() {
        btReload.setDisable(details.getValue().getModsVersion().equals(cbVersion.getSelectionModel().getSelectedItem()));
    }

    @Override
    public void beforeShow(Stage stage){
        if(details == null || details.getValue().getMods() == null)
            return;
        //TODO: icModSearchController.init(details.getValue().getModsVersion(), details.getValue().getModsType(), this::onInstallButtonClicked, this::onSearchBackClicked, details.getValue().getMods(), this::addMod);
        chSnapshots.setSelected(false);
        cbVersion.getItems().clear();
        cbVersion.setDisable(true);
        cbVersion.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(this::onVersionSelected));
        icPopupController.setVisible(false);
        vbCurrentMods.setVisible(true);
        icModSearchController.setVisible(false);
    }

    private void reloadMods() {
        currentModsContainer.getChildren().clear();
        new Thread(() -> {
            elements = new ArrayList<>();
            for(LauncherMod m : details.getValue().getMods()) {
                elements.add(new ModContentElement(m, details.getValue().getModsVersion(), this::installMod, this::enableMod, this::deleteMod));
            }
            elements.sort(Comparator.comparing(e -> e.getLauncherMod().getName()));
            Platform.runLater(() -> {
                currentModsContainer.getChildren().addAll(elements);
            });
        }).start();

    }

    private void populateVersionChoice() {
        cbVersion.getItems().clear();
        cbVersion.setPromptText(LauncherApplication.stringLocalizer.get("creator.version.prompt.loading"));
        cbVersion.setDisable(true);
        cbVersion.getItems().add(details.getValue().getModsVersion());
        cbVersion.getSelectionModel().select(0);
        new Thread(() -> {
            try {
                List <String> names = (chSnapshots.isSelected() ? MinecraftUtil.getVersions() : MinecraftUtil.getReleases()).stream()
                        .map(MinecraftVersion::getId)
                        .filter(s -> !s.equals(details.getValue().getModsVersion()))
                        .toList();
                Platform.runLater(() -> {
                    cbVersion.getItems().addAll(names);
                    cbVersion.setPromptText(LauncherApplication.stringLocalizer.get("creator.version.prompt.version"));
                    cbVersion.setDisable(false);
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

    public void installMod(ModContentElement source) {
        downloadMod(source.getCurrentSelected(), source.getLauncherMod(), source);
    }

    public void enableMod(boolean enable, ModContentElement source) {
        File modFile = new File(details.getKey().getDirectory() + source.getLauncherMod().getFileName() + (enable ? ".disabled" : ""));
        File newFile = new File(details.getKey().getDirectory(), source.getLauncherMod().getFileName() + (enable ? "" : ".disabled"));
        try {
            Files.move(modFile.toPath(), newFile.toPath());
        } catch(IOException e) {
            LauncherApplication.displayError(e);
        }
        source.getLauncherMod().setEnabled(enable);
        try {
            details.getValue().writeToFile(details.getKey().getDirectory() + details.getKey().getDetails());
        } catch (IOException e) {
            LauncherApplication.displayError(e);
        }
        source.setEnabled(enable);
    }

    public void deleteMod(ModContentElement source) {
        ArrayList<LauncherMod> mods = new ArrayList<>(details.getValue().getMods());
        mods.remove(source.getLauncherMod());

        File modFile = new File(details.getKey().getDirectory(), source.getLauncherMod().getFileName() + (source.getLauncherMod().isEnabled() ? "" : ".disabled"));
        try {
            Files.delete(modFile.toPath());
        } catch (IOException e) {
            LauncherApplication.displayError(e);
            return;
        }

        details.getValue().setMods(mods);
        try {
            details.getValue().writeToFile(details.getKey().getDirectory() + details.getKey().getDetails());
        } catch (IOException e){
            LauncherApplication.displayError(e);
            return;
        }

        ModContentElement element = null;
        for(ModContentElement e : elements) {
            if(e.equals(source)) {
                element = e;
                break;
            }
        }
        if(element != null) {
            currentModsContainer.getChildren().remove(element);
            elements.remove(element);
        } else {
            LauncherApplication.displayError(new IllegalStateException("Unable to locate mod element to remove"));
        }
    }

    private void downloadMod(ModVersionData versionData, LauncherMod modData, ModContentElement source) {
        LOGGER.debug("Downloading mod: name={}, version={}", versionData.getName(), versionData.getVersionNumber());
        source.setInstalling(true);
        new Thread(() -> {
            if (modData != null) {
                File oldFile = new File(details.getKey().getDirectory() + modData.getFileName());
                LOGGER.debug("Deleting old mod file: name={}", oldFile.getName());
                if (oldFile.exists()) {
                    if(!oldFile.delete()) {
                        LOGGER.warn("Failed to delete old mod file: name={}", oldFile.getName());
                        Platform.runLater(() -> {
                            source.setInstalling(false);
                            source.setCurrentSelected(versionData);
                        });
                        LauncherApplication.displayError(new IOException("Failed to delete old mod file: name=" + oldFile.getName()));
                        return;
                    }
                } else {
                    LOGGER.warn("Unable to locate old mod file: name={}", oldFile.getName());
                }
                LOGGER.debug("Deleted old mod file: name={}", oldFile.getName());
            }
            ArrayList<LauncherMod> mods = new ArrayList<>(details.getValue().getMods());
            if (modData != null) {
                mods.remove(modData);
            }
            LauncherMod newMod = downloadAndAdd(versionData, mods);
            if (newMod == null) {
                LOGGER.warn("Failed to download mod file, name={}", versionData.getName());
                Platform.runLater(() -> {
                    source.setInstalling(false);
                    source.setCurrentSelected(versionData);
                });
                LauncherApplication.displayError(new IOException("Failed to download mod file, name=" + versionData.getName()));
                return;
            }
            details.getValue().setMods(mods);
            source.setLauncherMod(newMod);
            try {
                details.getValue().writeToFile(details.getKey().getDirectory() + details.getKey().getDetails());
            } catch (IOException e) {
                LauncherApplication.displayError(e);
            }
        }).start();
    }

    private void addMod(LauncherMod mod, File source) {
        try {
            FileUtil.copyFile(source.getAbsolutePath(), details.getKey().getDirectory() + mod.getFileName());
        } catch (IOException e) {
            LauncherApplication.displayError(e);
        }

        ArrayList<LauncherMod> mods = new ArrayList<>(details.getValue().getMods());
        mods.add(mod);
        details.getValue().setMods(mods);
        try {
            details.getValue().writeToFile(details.getKey().getDirectory() + details.getKey().getDetails());
        } catch (IOException e) {
            LauncherApplication.displayError(e);
        }
        reloadMods();
    }

    private LauncherMod downloadAndAdd(ModVersionData versionData, ArrayList<LauncherMod> mods) {
        LOGGER.debug("Downloading mod file: name={}", versionData.getName());
        LauncherMod newMod;
        try {
            newMod = ModUtil.downloadModFile(versionData, new File(details.getKey().getDirectory()), true);
        } catch (FileDownloadException e) {
            LauncherApplication.displayError(e);
            return null;
        }

        mods.add(newMod);

        try {
            for(ModVersionData d : versionData.getRequiredDependencies(details.getValue().getModsVersion(), details.getValue().getModsType())) {
                if(d == null) {
                    continue;
                }
                LOGGER.debug("Downloading or skipping mod dependency file: name={}", d.getName());
                if(d.getParentMod() != null && !modExists(d.getParentMod()) && downloadAndAdd(d, mods) == null) {
                    LauncherApplication.displayError(new FileDownloadException("Failed to download mod dependency file"));
                    return null;
                }
            }
        } catch (FileDownloadException e) {
            LauncherApplication.displayError(e);
        }
        LOGGER.debug("Downloaded mod file: name={}", versionData.getName());
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
        vbCurrentMods.setVisible(true);
        icModSearchController.setVisible(false);
        reloadMods();
    }
}
