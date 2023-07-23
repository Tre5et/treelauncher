package net.treset.minecraftlauncher.ui.generic.lists;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import net.treset.mc_version_loader.exception.FileDownloadException;
import net.treset.mc_version_loader.launcher.LauncherMod;
import net.treset.mc_version_loader.launcher.LauncherModDownload;
import net.treset.mc_version_loader.mods.*;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.ui.generic.IconButton;
import org.apache.logging.log4j.util.BiConsumer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ModContentElement extends ContentElement {
    public enum ProviderStatus {
        NONE,
        AVAILABLE,
        CURRENT
    }

    private final HBox downloadContainer = new HBox();
    private final ImageView ivDownloading = new ImageView("img/downloading.gif");
    private final IconButton btInstall = new IconButton();
    private final ComboBox<ModVersionData> cbVersion = new ComboBox<>();
    private final HBox controlsContainer = new HBox();
    private final IconButton btEnable = new IconButton();
    private final IconButton btDelete = new IconButton();
    private final HBox providerContainer = new HBox();
    private final ImageView ivModrinth = new ImageView();
    private final ImageView ivCurseForge = new ImageView();

    private final Consumer<ModContentElement> installCallback;
    private final BiConsumer<Boolean, ModContentElement> enableCallback;
    private final Consumer<ModContentElement> deleteCallback;
    
    private final String gameVersion;
    private LauncherMod launcherMod;
    private ModData modData;
    private ModVersionData currentVersion;
    private List<ModVersionData> versions;
    private boolean enabled = true;
    
    public ModContentElement(LauncherMod launcherMod, String gameVersion, Consumer<ModContentElement> installCallback) {
        this(launcherMod.getName(), launcherMod.getDescription(), launcherMod, null, gameVersion, false, installCallback, (a,b) -> {}, (a) -> {});
    }
    
    public ModContentElement(LauncherMod launcherMod, String gameVersion, Consumer<ModContentElement> installCallback, BiConsumer<Boolean, ModContentElement> enableCallback, Consumer<ModContentElement> deleteCallback) {
        this(launcherMod.getName(), launcherMod.getDescription(), launcherMod, null, gameVersion, true, installCallback, enableCallback, deleteCallback);
    }
    
    public ModContentElement(ModData modData, String gameVersion, Consumer<ModContentElement> installCallback) {
        this(modData.getName(), modData.getDescription(), null, modData, gameVersion, false, installCallback, (a,b) -> {}, (a) -> {});
    }

    public ModContentElement(String title, String details, LauncherMod launcherMod, ModData modData, String gameVersion, boolean controls, Consumer<ModContentElement> installCallback, BiConsumer<Boolean, ModContentElement> enableCallback, Consumer<ModContentElement> deleteCallback) {
        super(null, title, details);

        this.gameVersion = gameVersion;
        this.launcherMod = launcherMod;
        this.modData = modData;
        
        this.installCallback = installCallback;
        this.enableCallback = enableCallback;
        this.deleteCallback = deleteCallback;

        this.getStylesheets().add("css/manager/ModsListElement.css");
        ColumnConstraints constraints0 = new ColumnConstraints();
        constraints0.setHgrow(Priority.NEVER);
        this.getColumnConstraints().add(constraints0);
        ColumnConstraints constraints1 = new ColumnConstraints();
        constraints1.setHgrow(Priority.ALWAYS);
        this.getColumnConstraints().add(constraints1);

        this.ivDownloading.setFitHeight(32);
        this.ivDownloading.setFitWidth(32);
        this.ivDownloading.setPreserveRatio(true);
        this.ivDownloading.setVisible(false);
        this.btInstall.getStyleClass().addAll("download", "highlight");
        this.btInstall.setIconSize(32);
        this.btInstall.setOnAction(this::onInstall);
        this.btInstall.setDisable(true);
        this.cbVersion.setPromptText(LauncherApplication.stringLocalizer.get("manager.mods.prompt.version"));
        HBox.setHgrow(cbVersion, Priority.ALWAYS);
        this.downloadContainer.setAlignment(Pos.CENTER_RIGHT);
        this.downloadContainer.getChildren().addAll(this.ivDownloading, this.btInstall, this.cbVersion);
        this.add(downloadContainer, 2, 0);


        this.ivModrinth.getStyleClass().add("modrinth");
        this.ivModrinth.setFitHeight(32);
        this.ivModrinth.setFitWidth(32);
        this.ivModrinth.setPreserveRatio(true);
        this.ivCurseForge.getStyleClass().add("curseforge");
        this.ivCurseForge.setFitHeight(32);
        this.ivCurseForge.setFitWidth(32);
        this.ivCurseForge.setPreserveRatio(true);
        this.providerContainer.setPadding(new Insets(8, 8, 8, 8));
        this.providerContainer.setSpacing(10);
        this.providerContainer.setAlignment(Pos.BOTTOM_RIGHT);
        this.providerContainer.getChildren().addAll(this.ivModrinth, this.ivCurseForge);
        this.add(providerContainer, 2, 2);
        
        this.btEnable.getStyleClass().add("visibility");
        this.btEnable.setIconSize(32);
        this.btEnable.setOnAction(this::onEnable);
        this.btDelete.getStyleClass().addAll("delete", "neutral");
        this.btDelete.setIconSize(32);
        this.btDelete.setOnAction(this::onDelete);
        this.controlsContainer.setAlignment(Pos.CENTER_RIGHT);
        this.controlsContainer.getChildren().addAll(btEnable, btDelete);
        this.controlsContainer.setVisible(controls);
        this.add(controlsContainer, 2, 1);

        new Thread(this::populateVersions).start();
        new Thread(this::loadImage).start();
        
        if(launcherMod != null) {
            setEnabled(launcherMod.isEnabled());
            setModrinthStatus(launcherMod.getCurrentProvider().equals("modrinth") ? ProviderStatus.CURRENT : launcherMod.getDownloads().stream().map(LauncherModDownload::getProvider).anyMatch("modrinth"::equals) ? ProviderStatus.AVAILABLE : ProviderStatus.NONE);
            setCurseforgeStatus(launcherMod.getCurrentProvider().equals("curseforge") ? ProviderStatus.CURRENT : launcherMod.getDownloads().stream().map(LauncherModDownload::getProvider).anyMatch("curseforge"::equals) ? ProviderStatus.AVAILABLE : ProviderStatus.NONE);
        } else if(modData != null) {
            setModrinthStatus(modData.getModProviders().stream().anyMatch(ModProvider.MODRINTH::equals) ? ProviderStatus.AVAILABLE : ProviderStatus.NONE);
            setCurseforgeStatus(modData.getModProviders().stream().anyMatch(ModProvider.CURSEFORGE::equals) ? ProviderStatus.AVAILABLE : ProviderStatus.NONE);
        }
    }
    
    private void populateVersions() {
        if(modData == null && launcherMod != null && launcherMod.getDownloads() != null && !launcherMod.getDownloads().isEmpty()) {
            try {
                modData = launcherMod.getModData();
            } catch (FileDownloadException e) {
                LauncherApplication.displayError(e);
            }
        }
        if (modData != null) {
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
                tempVersions.add(0, currentVersion);
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
    }

    private void updateCurrentVersion() {
        if(launcherMod != null) {
            currentVersion = null;
            for (ModVersionData v : versions) {
                if(v.getVersionNumber().equals(launcherMod.getVersion())) {
                    currentVersion = v;
                    break;
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

    public void checkUpdate() {
        if(cbVersion.getItems().size() > 0 && !cbVersion.getItems().get(0).equals(currentVersion)) {
            cbVersion.getSelectionModel().select(0);
        }
    }

    public void changeVersion() {
        if(launcherMod != null && cbVersion.getSelectionModel().getSelectedItem() != null && !cbVersion.getSelectionModel().getSelectedItem().equals(currentVersion)) {
            Platform.runLater(() -> {
                onInstall(null);
            });
        }
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
    }

    public void setInstalling(boolean installing) {
        this.ivDownloading.setVisible(installing);
        this.btInstall.setDisable(true);
    }

    public void setLauncherMod(LauncherMod launcherMod) {
        this.launcherMod = launcherMod;
        setInstalling(false);
        setEnabled(launcherMod.isEnabled());
        setModrinthStatus(launcherMod.getCurrentProvider().equals("modrinth") ? ProviderStatus.CURRENT : launcherMod.getDownloads().stream().map(LauncherModDownload::getProvider).anyMatch("modrinth"::equals) ? ProviderStatus.AVAILABLE : ProviderStatus.NONE);
        setCurseforgeStatus(launcherMod.getCurrentProvider().equals("curseforge") ? ProviderStatus.CURRENT : launcherMod.getDownloads().stream().map(LauncherModDownload::getProvider).anyMatch("curseforge"::equals) ? ProviderStatus.AVAILABLE : ProviderStatus.NONE);
        populateVersions();
    }

    private void onInstall(ActionEvent event) {
        if(!enabled) {
            onEnable(null);
        }
        installCallback.accept(this);
    }

    private void onEnable(ActionEvent actionEvent) {
        enableCallback.accept(!enabled, this);
    }

    private void onDelete(ActionEvent actionEvent) {
        deleteCallback.accept(this);
    }

    private void onVersionChange(ObservableValue<? extends ModVersionData> observable, ModVersionData oldValue, ModVersionData newValue) {
        btInstall.setDisable(newValue == null || launcherMod != null && newValue.equals(currentVersion));
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


