package net.treset.minecraftlauncher.util.ui;

import javafx.application.Platform;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import net.treset.mc_version_loader.VersionLoader;
import net.treset.mc_version_loader.exception.FileDownloadException;
import net.treset.mc_version_loader.fabric.FabricProfile;
import net.treset.mc_version_loader.fabric.FabricVersionDetails;
import net.treset.mc_version_loader.files.Sources;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.mc_version_loader.launcher.LauncherVersionDetails;
import net.treset.mc_version_loader.minecraft.MinecraftVersion;
import net.treset.mc_version_loader.minecraft.MinecraftVersionDetails;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.creation.VersionCreator;
import net.treset.minecraftlauncher.data.LauncherFiles;
import net.treset.minecraftlauncher.util.FormatUtil;
import net.treset.minecraftlauncher.util.exception.ComponentCreationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class VersionCreationHelper {
    private static final Logger LOGGER = LogManager.getLogger(VersionCreationHelper.class);

    private final ComboBox<MinecraftVersion> versionChoice;
    private final CheckBox snapshotsCheck;
    private final ComboBox<String> typeChoice;
    private final ComboBox<FabricVersionDetails> loaderChoice;
    private final Map<String, LauncherManifestType> typeConversion;
    private final LauncherFiles launcherFiles;
    private final String librariesDir;
    private final LauncherManifest versionManifest;
    private final Supplier<Boolean> isCreationReady;
    private LauncherVersionDetails currentVersion;


    private List<FabricVersionDetails> fabricVersions;
    private List<MinecraftVersion> vanillaVersions;

    public VersionCreationHelper(ComboBox<MinecraftVersion> versionChoice, CheckBox snapshotsCheck, ComboBox<String> typeChoice, ComboBox<FabricVersionDetails> loaderChoice, Map<String, LauncherManifestType> typeConversion, LauncherFiles launcherFiles, String librariesDir, LauncherManifest versionManifest, Supplier<Boolean> isCreationReady) {
        this.versionChoice = versionChoice;
        this.snapshotsCheck = snapshotsCheck;
        this.typeChoice = typeChoice;
        this.loaderChoice = loaderChoice;
        this.typeConversion = typeConversion;
        this.launcherFiles = launcherFiles;
        this.librariesDir = librariesDir;
        this.versionManifest = versionManifest;
        this.isCreationReady = isCreationReady;

        typeChoice.getItems().clear();
        typeChoice.getItems().addAll("Vanilla", "Fabric");

        versionChoice.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(this::onVersionChanged);
        });
        typeChoice.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(this::onTypeChanged);
        });
    }

    public void beforeShow() {
        versionChoice.getItems().clear();
        snapshotsCheck.setSelected(false);
        loaderChoice.getItems().clear();
        if(currentVersion != null) {
            switch (currentVersion.getVersionType()) {
                case "vanilla" -> typeChoice.getSelectionModel().select("Vanilla");
                case "fabric" -> typeChoice.getSelectionModel().select("Fabric");
            }
        }
        populateVersionChoice();
    }

    public void setCurrentVersion(LauncherVersionDetails currentVersion) {
        this.currentVersion = currentVersion;
    }

    public LauncherVersionDetails getCurrentVersion() {
        return currentVersion;
    }

    private void onVersionChanged() {
        updateLoaderChoice();
    }

    private void onTypeChanged() {
        updateLoaderChoice();
    }

    public void populateVersionChoice() {
        versionChoice.getItems().clear();
        versionChoice.setPromptText(LauncherApplication.stringLocalizer.get("creator.version.prompt.loading"));
        versionChoice.setDisable(true);
        updateLoaderChoice();
        new Thread(() -> {
            try {
                vanillaVersions = snapshotsCheck.isSelected() ? VersionLoader.getVersions() : VersionLoader.getReleases();
            } catch (FileDownloadException e) {
                LOGGER.error("Failed to load versions", e);
                return;
            }
            Platform.runLater(() -> {
                versionChoice.getItems().addAll(vanillaVersions);
                if(currentVersion != null) {
                    versionChoice.getSelectionModel().select(FormatUtil.indexInList(vanillaVersions.stream().map(MinecraftVersion::getId).toList(), currentVersion.getVersionNumber()));
                }
                versionChoice.setPromptText(LauncherApplication.stringLocalizer.get("creator.version.prompt.version"));
                versionChoice.setDisable(false);
            });
        }).start();
    }

    public void updateLoaderChoice() {
        loaderChoice.setVisible(false);
        if("Fabric".equals(typeChoice.getSelectionModel().getSelectedItem())) {
            loaderChoice.setVisible(true);
            loaderChoice.getItems().clear();
            loaderChoice.setPromptText(LauncherApplication.stringLocalizer.get("creator.version.prompt.loading"));
            loaderChoice.setDisable(true);
            loaderChoice.setVisible(true);
            new Thread(() -> {
                try {
                    fabricVersions = FabricVersionDetails.fromJsonArray(Sources.getFabricForMinecraftVersion(versionChoice.getValue().getId()));
                } catch (FileDownloadException e) {
                    LOGGER.error("Failed to load fabric versions", e);
                    return;
                }
                Platform.runLater(() -> {
                    loaderChoice.getItems().addAll(fabricVersions);
                    if(currentVersion != null) {
                        loaderChoice.getSelectionModel().select(FormatUtil.indexInList(fabricVersions.stream().map((v) -> v.getLoader().getVersion()).toList(), currentVersion.getLoaderVersion()));
                    }
                    loaderChoice.setPromptText(LauncherApplication.stringLocalizer.get("creator.version.prompt.loaderversion"));
                    loaderChoice.setDisable(false);
                });
            }).start();
        }
    }

    public VersionCreator getCreator() throws ComponentCreationException {
        if(!isCreationReady.get()) {
            throw new ComponentCreationException("Not ready to create version");
        }
        if("Vanilla".equals(typeChoice.getValue())) {
            MinecraftVersion version = versionChoice.getValue();
            if(version == null) {
                throw new ComponentCreationException("Could not get Minecraft version");
            }
            MinecraftVersionDetails details;
            try {
                details = MinecraftVersionDetails.fromJson(Sources.getFileFromUrl(version.getUrl()));
            } catch (FileDownloadException e) {
                throw new ComponentCreationException("Could not get Minecraft version details", e);
            }
            return new VersionCreator(typeConversion, versionManifest, details, launcherFiles, librariesDir);
        } else if("Fabric".equals(typeChoice.getValue())) {
            FabricVersionDetails details = loaderChoice.getValue();
            if(details == null) {
                throw new ComponentCreationException("Could not get Fabric version");
            }
            FabricProfile profile;
            try {
                profile = FabricProfile.fromJson(Sources.getFileFromHttpGet("https://meta.fabricmc.net/v2/versions/loader/" + versionChoice.getValue() + "/" + details.getLoader().getVersion() + "/profile/json", List.of(), List.of()));
            } catch (FileDownloadException e) {
                throw new ComponentCreationException("Could not get Fabric profile", e);
            }
            return new VersionCreator(typeConversion, versionManifest, details, profile, launcherFiles, librariesDir);
        }
        throw new ComponentCreationException("Invalid version type");
    }
}
