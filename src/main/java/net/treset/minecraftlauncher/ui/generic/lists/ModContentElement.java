package net.treset.minecraftlauncher.ui.generic.lists;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Pair;
import net.treset.mc_version_loader.exception.FileDownloadException;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherMod;
import net.treset.mc_version_loader.launcher.LauncherModDownload;
import net.treset.mc_version_loader.launcher.LauncherModsDetails;
import net.treset.mc_version_loader.mods.*;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.ui.generic.IconButton;
import net.treset.minecraftlauncher.util.UiUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ModContentElement extends ContentElement {
    public enum ProviderStatus {
        NONE,
        AVAILABLE,
        CURRENT
    }

    private static final Logger LOGGER = LogManager.getLogger(ModContentElement.class);


    private final HBox downloadContainer = new HBox();
    private final ImageView ivDownloading = new ImageView("img/downloading.gif");
    private final IconButton btInstall = new IconButton();
    private final ComboBox<ModVersionData> cbVersion = new ComboBox<>();
    private final HBox controlsContainer = new HBox();
    private final IconButton btEnable = new IconButton();
    private final IconButton btDelete = new IconButton();
    private final HBox providerContainer = new HBox();
    private final IconButton btOpen = new IconButton();
    private final ImageView ivModrinth = new ImageView();
    private final ImageView ivCurseForge = new ImageView();

    private final ChangeEvent<LauncherMod, ModContentElement> changeEvent;
    private final String gameVersion;

    private LauncherMod launcherMod;
    private LauncherManifest componentManifest;
    private LauncherModsDetails componentDetails;
    private ModData modData;
    private ModVersionData currentVersion;
    private List<ModVersionData> versions;
    private boolean enabled = true;
    
    public ModContentElement(LauncherMod launcherMod, String gameVersion, ChangeEvent<LauncherMod, ModContentElement> changeEvent, Pair<LauncherManifest, LauncherModsDetails> details, boolean controls) {
        this(launcherMod.getName(), launcherMod.getDescription(), launcherMod, null, gameVersion, controls, changeEvent, details);
    }
    
    public ModContentElement(ModData modData, String gameVersion, ChangeEvent<LauncherMod, ModContentElement> changeEvent, Pair<LauncherManifest, LauncherModsDetails> details) {
        this(modData.getName(), modData.getDescription(), null, modData, gameVersion, false, changeEvent, details);
    }

    public ModContentElement(String title, String description, LauncherMod launcherMod, ModData modData, String gameVersion, boolean controls, ChangeEvent<LauncherMod, ModContentElement> changeEvent, Pair<LauncherManifest, LauncherModsDetails> details) {
        super(null, title, description);
        long time = System.currentTimeMillis();

        this.gameVersion = gameVersion;
        this.launcherMod = launcherMod;
        this.modData = modData;

        this.changeEvent = changeEvent;
        this.componentManifest = details.getKey();
        this.componentDetails = details.getValue();

        this.getStylesheets().add("css/manager/ModsListElement.css");

        this.ivDownloading.setFitHeight(32);
        this.ivDownloading.setFitWidth(32);
        this.ivDownloading.setPreserveRatio(true);
        this.ivDownloading.setVisible(false);
        this.btInstall.getStyleClass().addAll("download", "highlight");
        this.btInstall.setIconSize(32);
        this.btInstall.setTooltipText(LauncherApplication.stringLocalizer.get("content.mods.tooltip.install"));
        this.btInstall.setOnAction(this::onInstall);
        this.btInstall.setDisable(true);
        this.cbVersion.setPromptText(LauncherApplication.stringLocalizer.get("manager.mods.prompt.version"));
        HBox.setHgrow(cbVersion, Priority.ALWAYS);
        this.downloadContainer.setAlignment(Pos.CENTER_RIGHT);
        this.downloadContainer.getChildren().addAll(this.ivDownloading, this.btInstall, this.cbVersion);
        this.add(downloadContainer, 2, 0);

        this.btOpen.getStyleClass().add("open");
        this.btOpen.setIconSize(20);
        this.btOpen.setIconSize(20);
        this.btOpen.setTooltipText(LauncherApplication.stringLocalizer.get("content.mods.tooltip.open"));
        this.btOpen.setOnAction(this::onOpen);
        this.ivModrinth.getStyleClass().add("modrinth");
        this.ivModrinth.setFitHeight(32);
        this.ivModrinth.setFitWidth(32);
        this.ivModrinth.setPreserveRatio(true);
        this.ivCurseForge.getStyleClass().add("curseforge");
        this.ivCurseForge.setFitHeight(32);
        this.ivCurseForge.setFitWidth(32);
        this.ivCurseForge.setPreserveRatio(true);
        this.providerContainer.setPadding(new Insets(0, 8, 8, 8));
        this.providerContainer.setSpacing(10);
        this.providerContainer.setAlignment(Pos.BOTTOM_RIGHT);
        this.providerContainer.getChildren().addAll(this.btOpen, this.ivModrinth, this.ivCurseForge);
        this.add(providerContainer, 2, 2);
        
        this.btEnable.getStyleClass().add("visibility");
        this.btEnable.setIconSize(32);
        this.btEnable.setTooltipText(LauncherApplication.stringLocalizer.get("content.mods.tooltip.disable"));
        this.btEnable.setOnAction(this::onEnable);
        this.btDelete.getStyleClass().addAll("delete", "negative");
        this.btDelete.setIconSize(32);
        this.btDelete.setTooltipText(LauncherApplication.stringLocalizer.get("content.mods.tooltip.delete"));
        this.btDelete.setOnAction(this::onDelete);
        this.controlsContainer.setAlignment(Pos.CENTER_RIGHT);
        this.controlsContainer.getChildren().addAll(btEnable, btDelete);
        this.controlsContainer.setVisible(controls);
        this.add(controlsContainer, 2, 1);

        new Thread(this::populateVersions).start();
        new Thread(this::loadImage).start();
        
        if(launcherMod != null) {
            setEnabled(launcherMod.isEnabled());
            if(launcherMod.getCurrentProvider() != null) {
                setModrinthStatus(launcherMod.getCurrentProvider().equals("modrinth") ? ProviderStatus.CURRENT : launcherMod.getDownloads().stream().map(LauncherModDownload::getProvider).anyMatch("modrinth"::equals) ? ProviderStatus.AVAILABLE : ProviderStatus.NONE);
                setCurseforgeStatus(launcherMod.getCurrentProvider().equals("curseforge") ? ProviderStatus.CURRENT : launcherMod.getDownloads().stream().map(LauncherModDownload::getProvider).anyMatch("curseforge"::equals) ? ProviderStatus.AVAILABLE : ProviderStatus.NONE);
            }
        } else if(modData != null) {
            setModrinthStatus(modData.getModProviders().stream().anyMatch(ModProvider.MODRINTH::equals) ? ProviderStatus.AVAILABLE : ProviderStatus.NONE);
            setCurseforgeStatus(modData.getModProviders().stream().anyMatch(ModProvider.CURSEFORGE::equals) ? ProviderStatus.AVAILABLE : ProviderStatus.NONE);
        }

        LOGGER.debug("Created mod element: name={}, time={}", title, System.currentTimeMillis() - time);
    }
    
    private void populateVersions() {
        if(modData == null && launcherMod != null && launcherMod.getDownloads() != null && !launcherMod.getDownloads().isEmpty()) {
            try {
                modData = launcherMod.getModData();
            } catch (FileDownloadException e) {
                LauncherApplication.displayError(e);
            }
        }
        if(modData == null) {
            updateCurrentVersion();
            Platform.runLater(() -> {
                if(currentVersion == null) return;
                cbVersion.getItems().clear();
                cbVersion.getItems().add(currentVersion);
                cbVersion.getSelectionModel().select(currentVersion);
            });
            return;
        }

        if(versions == null) {
            try {
                versions = modData.getVersions(gameVersion, "fabric");
            } catch (FileDownloadException e) {
                LauncherApplication.displayError(e);
                return;
            }
        }

        updateCurrentVersion();
        List<ModVersionData> tempVersions = new ArrayList<>(versions);
        if(currentVersion != null && !versions.contains(currentVersion)) {
            tempVersions.add(currentVersion);
        }
        Platform.runLater(() -> {
            cbVersion.getItems().clear();
            cbVersion.getItems().addAll(tempVersions);
            if(currentVersion != null) {
                cbVersion.getSelectionModel().select(currentVersion);
            }
            cbVersion.getSelectionModel().selectedItemProperty().addListener(this::onVersionChange);
        });
    }

    private void updateCurrentVersion() {
        if(launcherMod != null) {
            currentVersion = null;
            if(versions != null) {
                for (ModVersionData v : versions) {
                    if (v.getVersionNumber().equals(launcherMod.getVersion())) {
                        currentVersion = v;
                        break;
                    }
                }
            }
            if(currentVersion == null) {
                currentVersion = new GenericModVersion() {
                    @Override public LocalDateTime getDatePublished() {return null;}
                    @Override public int getDownloads() {return 0;}
                    @Override public String getName() {return null;}
                    @Override public String getVersionNumber() {return launcherMod.getVersion();}
                    @Override public String getDownloadUrl() {return null;}
                    @Override public List<String> getModLoaders() {return null;}
                    @Override public List<String> getGameVersions() {return null;}
                    @Override public List<ModVersionData> getRequiredDependencies(String s, String s1) {return null;}
                    @Override public ModData getParentMod() {return null;}
                    @Override public void setParentMod(ModData modData) {}
                    @Override public List<ModProvider> getModProviders() {return null;}
                    @Override public ModVersionType getModVersionType() {return null;}
                };
            }
        }
    }
    
    private void loadImage() {
        if(launcherMod != null && launcherMod.getIconUrl() != null && !launcherMod.getIconUrl().isBlank()) {
            Image logo = new Image(launcherMod.getIconUrl());
            Platform.runLater(() -> setIcon(logo));
        } else if(modData != null && modData.getIconUrl() != null && !modData.getIconUrl().isBlank()) {
            Image logo = new Image(modData.getIconUrl());
            Platform.runLater(() -> setIcon(logo));
        }
    }

    public void update(boolean autoUpdate, boolean enable, boolean disable){
        if(cbVersion.getSelectionModel().getSelectedIndex() != 0) {
            cbVersion.getSelectionModel().select(0);
        }
        if(autoUpdate) {
            update(enable);
        }
        if(disable && cbVersion.getSelectionModel().getSelectedItem().getModProviders() == null) {
            enable(false);
        }
    }

    public void enable(boolean enable) {
        if(enable == enabled) {
            return;
        }
        File modFile = new File(componentManifest.getDirectory() + launcherMod.getFileName() + (enable ? ".disabled" : ""));
        File newFile = new File(componentManifest.getDirectory(), launcherMod.getFileName() + (enable ? "" : ".disabled"));
        try {
            Files.move(modFile.toPath(), newFile.toPath());
        } catch(IOException e) {
            LauncherApplication.displayError(e);
        }
        launcherMod.setEnabled(enable);
        setEnabled(enable);
        changeEvent.update();
    }

    public void delete() {
        File oldFile = new File(componentManifest.getDirectory() + launcherMod.getFileName() + (isEnabled() ? "" : ".disabled"));
        try {
            Files.delete(oldFile.toPath());
        } catch (IOException e) {
            LauncherApplication.displayError(e);
            return;
        }
        changeEvent.remove(this);
    }

    private void update(boolean enable) {
        if(!cbVersion.getSelectionModel().getSelectedItem().equals(currentVersion)) {
            downloadMod(cbVersion.getSelectionModel().getSelectedItem(), enable);
        }
    }

    private void downloadMod(ModVersionData versionData, boolean enable) {
        LOGGER.debug("Downloading mod: name={}, version={}", versionData.getName(), versionData.getVersionNumber());
        setInstalling(true);
        new Thread(() -> {
            if (launcherMod != null) {
                File oldFile = new File(componentManifest.getDirectory() + launcherMod.getFileName() + (launcherMod.isEnabled() ? "" : ".disabled"));
                LOGGER.debug("Deleting old mod file: name={}", oldFile.getName());
                if (oldFile.exists()) {
                    if(!oldFile.delete()) {
                        LOGGER.warn("Failed to delete old mod file: name={}", oldFile.getName());
                        Platform.runLater(() -> {
                            setInstalling(false);
                            setCurrentSelected(currentVersion);
                        });
                        LauncherApplication.displayError(new IOException("Failed to delete old mod file: name=" + oldFile.getName()));
                        return;
                    }
                } else {
                    LOGGER.warn("Unable to locate old mod file: name={}", oldFile.getName());
                }
                LOGGER.debug("Deleted old mod file: name={}", oldFile.getName());
            }
            List<LauncherMod> newMods = downloadRequired(versionData, enabled || enable);
            if (newMods == null || newMods.isEmpty()) {
                LOGGER.warn("Failed to download mod file, name={}", versionData.getName());
                changeEvent.remove(this);
                LauncherApplication.displayError(new IOException("Failed to download mod file, name=" + versionData.getName()));
                return;
            }
            if(launcherMod != null) {
                changeEvent.change(launcherMod, newMods.get(0));
                setLauncherMod(newMods.get(0));
            }
            for(int i = launcherMod == null ? 0 : 1; i < newMods.size(); i++) {
                changeEvent.add(newMods.get(i));
            }
            setInstalling(false);
        }).start();
    }

    private List<LauncherMod> downloadRequired(ModVersionData versionData, boolean enabled) {
        LOGGER.debug("Downloading mod file: name={}", versionData.getName());
        LauncherMod newMod;
        try {
            newMod = ModUtil.downloadModFile(versionData, new File(componentManifest.getDirectory()), enabled);
        } catch (FileDownloadException e) {
            LauncherApplication.displayError(e);
            return List.of();
        }

        ArrayList<LauncherMod> mods = new ArrayList<>();
        mods.add(newMod);

        try {
            for(ModVersionData d : versionData.getRequiredDependencies(componentDetails.getModsVersion(), componentDetails.getModsType())) {
                if(d == null) {
                    continue;
                }
                if(d.getParentMod() != null && !modExists(d.getParentMod())) {
                    LOGGER.debug("Downloading mod dependency file: name={}", d.getName());
                    mods.addAll(downloadRequired(d, true));
                } else {
                    LOGGER.debug("Skipping mod dependency file: name={}", d.getName());
                }
            }
        } catch (FileDownloadException e) {
            LauncherApplication.displayError(e);
        }
        LOGGER.debug("Downloaded mod file: name={}", versionData.getName());
        return mods;
    }

    private boolean modExists(ModData modData) {
        for(LauncherMod m : componentDetails.getMods()) {
            if(modData.getName().equals(m.getName()) || modData.getProjectIds().stream().anyMatch(id -> m.getDownloads().stream().anyMatch(d -> d.getId().equals(id)))) {
                return true;
            }
        }
        return false;
    }


    public boolean hasValidVersion() {
        return currentVersion.getModProviders() != null;
    }

    public void setModrinthStatus(ProviderStatus status) {
        updateImageStatus(status, this.ivModrinth);
    }

    public void setCurseforgeStatus(ProviderStatus status) {
        updateImageStatus(status, this.ivCurseForge);
    }

    private void updateImageStatus(ProviderStatus status, ImageView imageView) {
        switch (status) {
            case NONE -> imageView.getStyleClass().removeAll("current", "available");
            case AVAILABLE -> {
                imageView.getStyleClass().remove("current");
                imageView.getStyleClass().add("available");
            }
            case CURRENT -> {
                imageView.getStyleClass().remove("available");
                imageView.getStyleClass().add("current");
            }
        }
    }
    
    public ModVersionData getCurrentSelected() {
        return this.cbVersion.getValue();
    }
    
    public void setCurrentSelected(ModVersionData version) {
        this.cbVersion.getSelectionModel().select(version);
    }
    
    public void setCurrentSelected(int index) {
        this.cbVersion.getSelectionModel().select(index);
    }

    public void setInstallAvailable(boolean available) {
        this.btInstall.setDisable(available);
    }

    public void setEnabled(boolean enabled) {
        if(this.enabled == enabled) return;
        this.enabled = enabled;
        iconContainer.setOpacity(enabled ? 1 : 0.5);
        contentContainer.setDisable(!enabled);
        btInstall.setOpacity(enabled ? 1 : 0.5);
        cbVersion.setDisable(!enabled);
        controlsContainer.setOpacity(enabled ? 1 : 0.5);
        providerContainer.setOpacity(enabled ? 1 : 0.5);
        btEnable.setTooltipText(LauncherApplication.stringLocalizer.get(enabled ? "content.mods.tooltip.disable" : "content.mods.tooltip.enable"));
    }

    public void setInstalling(boolean installing) {
        this.ivDownloading.setVisible(installing);
        this.btInstall.setDisable(true);
    }

    public void setLauncherMod(LauncherMod launcherMod) {
        if(launcherMod == null || this.launcherMod == launcherMod) return;
        this.launcherMod = launcherMod;
        setInstalling(false);
        setEnabled(launcherMod.isEnabled());
        setModrinthStatus(launcherMod.getCurrentProvider().equals("modrinth") ? ProviderStatus.CURRENT : launcherMod.getDownloads().stream().map(LauncherModDownload::getProvider).anyMatch("modrinth"::equals) ? ProviderStatus.AVAILABLE : ProviderStatus.NONE);
        setCurseforgeStatus(launcherMod.getCurrentProvider().equals("curseforge") ? ProviderStatus.CURRENT : launcherMod.getDownloads().stream().map(LauncherModDownload::getProvider).anyMatch("curseforge"::equals) ? ProviderStatus.AVAILABLE : ProviderStatus.NONE);
        populateVersions();
    }

    private void onInstall(ActionEvent event) {
        update(true);
    }

    private void onEnable(ActionEvent actionEvent) {
        enable(!enabled);
    }

    private void onDelete(ActionEvent actionEvent) {
        delete();
    }

    private void onOpen(ActionEvent actionEvent) {
        String url = getUrl();
        if(url != null) {
            try {
                UiUtil.openBrowser(url);
            } catch (Exception e) {
                LauncherApplication.displayError(e);
            }
        }
    }

    private void onVersionChange(ObservableValue<? extends ModVersionData> observable, ModVersionData oldValue, ModVersionData newValue) {
        btInstall.setDisable(newValue == null || launcherMod != null && newValue.equals(currentVersion));
    }

    public String getUrl() {
        if(launcherMod != null && launcherMod.getUrl() != null) {
            return launcherMod.getUrl();
        } else if(modData != null && modData.getUrl() != null) {
            return modData.getUrl();
        }
        return null;
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public LauncherMod getLauncherMod() {
        return launcherMod;
    }

    public ModData getModData() {
        return modData;
    }

    public ModVersionData getCurrentVersion() {
        return currentVersion;
    }

    public boolean isEnabled() {
        return enabled;
    }
}


