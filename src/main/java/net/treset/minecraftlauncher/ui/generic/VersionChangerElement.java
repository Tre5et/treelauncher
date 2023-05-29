package net.treset.minecraftlauncher.ui.generic;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
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
import net.treset.minecraftlauncher.ui.base.UiElement;
import net.treset.minecraftlauncher.ui.create.VersionCreatorElement;
import net.treset.minecraftlauncher.util.exception.ComponentCreationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class VersionChangerElement extends UiElement {
    private static final Logger LOGGER = LogManager.getLogger(VersionCreatorElement.class);

    @FXML private AnchorPane rootPane;
    @FXML private ComboBox<String> versionChoice;
    @FXML private CheckBox snapshotsCheck;
    @FXML private ComboBox<String> typeChoice;
    @FXML private ComboBox<String> loaderChoice;
    @FXML private Button changeButton;

    private LauncherVersionDetails currentVersion;
    private List<FabricVersionDetails> fabricVersions;
    private List<MinecraftVersion> vanillaVersions;
    private Map<String, LauncherManifestType> typeConversion;
    private LauncherFiles launcherFiles;
    private String librariesDir;
    private LauncherManifest versionManifest;
    private Consumer<VersionCreator> changeCallback;
    private Consumer<Exception> changeFailCallback;

    public void init(LauncherFiles launcherFiles, Map<String, LauncherManifestType> typeConversion, String librariesDir, LauncherManifest versionManifest, Consumer<VersionCreator> changeCallback, Consumer<Exception> changeFailCallback) {
        this.typeConversion = typeConversion;
        this.launcherFiles = launcherFiles;
        this.librariesDir = librariesDir;
        this.versionManifest = versionManifest;
        this.changeCallback = changeCallback;
        this.changeFailCallback = changeFailCallback;
        typeChoice.getItems().clear();
        typeChoice.getItems().addAll("Vanilla", "Fabric");
        versionChoice.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(this::onVersionChanged);
        });
        typeChoice.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(this::onTypeChanged);
        });
        loaderChoice.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(this::onLoaderVersionChanged);
        });
    }

    @Override
    public void beforeShow(Stage stage) {
        if(currentVersion != null) {
            versionChoice.getItems().clear();
            snapshotsCheck.setSelected(false);
            loaderChoice.getItems().clear();
            switch (currentVersion.getVersionType()) {
                case "vanilla" -> typeChoice.getSelectionModel().select("Vanilla");
                case "fabric" -> typeChoice.getSelectionModel().select("Fabric");
            }
            populateVersionChoice();
        }
    }

    @Override
    public void afterShow(Stage stage) {}

    @FXML
    private void onChangeButtonClicked() {
        if(checkCreateReady()) {
            try {
                VersionCreator creator = getCreator();
                changeCallback.accept(creator);
            } catch (ComponentCreationException e) {
                changeFailCallback.accept(e);
            }
        }
    }

    private void onVersionChanged() {
        updateButtonState();
        updateLoaderChoice();
    }

    private void onTypeChanged() {
        updateButtonState();
        updateLoaderChoice();
    }

    private void onLoaderVersionChanged() {
        updateButtonState();
    }

    private void updateButtonState() {
        changeButton.setDisable(currentVersion.getVersionNumber().equals(versionChoice.getValue()) && currentVersion.getVersionType().equals(typeChoice.getValue().toLowerCase()) && (currentVersion.getLoaderVersion() == null || currentVersion.getLoaderVersion().equals(loaderChoice.getValue())));
    }

    @FXML
    private void onSnapshotsChecked() {
        populateVersionChoice();
    }

    public void setCurrentVersion(LauncherVersionDetails currentVersion) {
        this.currentVersion = currentVersion;
    }

    private void populateVersionChoice() {
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
                versionChoice.getItems().addAll(vanillaVersions.stream().map(MinecraftVersion::getId).toList());
                versionChoice.getSelectionModel().select(currentVersion.getVersionNumber());
                versionChoice.setPromptText(LauncherApplication.stringLocalizer.get("creator.version.prompt.version"));
                versionChoice.setDisable(false);
            });
        }).start();
    }

    private void updateLoaderChoice() {
        loaderChoice.setVisible(false);
        if("Fabric".equals(typeChoice.getSelectionModel().getSelectedItem())) {
            loaderChoice.setVisible(true);
            loaderChoice.getItems().clear();
            loaderChoice.setPromptText(LauncherApplication.stringLocalizer.get("creator.version.prompt.loading"));
            loaderChoice.setDisable(true);
            loaderChoice.setVisible(true);
            new Thread(() -> {
                try {
                    fabricVersions = FabricVersionDetails.fromJsonArray(Sources.getFabricForMinecraftVersion(versionChoice.getValue()));
                } catch (FileDownloadException e) {
                    LOGGER.error("Failed to load fabric versions", e);
                    return;
                }
                Platform.runLater(() -> {
                    loaderChoice.getItems().addAll(fabricVersions.stream().map(v -> v.getLoader().getVersion()).toList());
                    loaderChoice.getSelectionModel().select(currentVersion.getLoaderVersion());
                    loaderChoice.setPromptText(LauncherApplication.stringLocalizer.get("creator.version.prompt.loaderversion"));
                    loaderChoice.setDisable(false);
                });
            }).start();
        }
    }

    private VersionCreator getCreator() throws ComponentCreationException {
        if(!checkCreateReady()) {
            throw new ComponentCreationException("Not ready to create version");
        }
        if("Vanilla".equals(typeChoice.getValue())) {
            MinecraftVersion version = getMinecraftFromString(versionChoice.getValue());
            if(version == null) {
                throw new ComponentCreationException("Could not get Minecraft version");
            }
            MinecraftVersionDetails details = null;
            try {
                details = MinecraftVersionDetails.fromJson(Sources.getFileFromUrl(version.getUrl()));
            } catch (FileDownloadException e) {
                throw new ComponentCreationException("Could not get Minecraft version details", e);
            }
            return new VersionCreator(typeConversion, versionManifest, details, launcherFiles, librariesDir);
        } else if("Fabric".equals(typeChoice.getValue())) {
            FabricVersionDetails details = getFabricFromString(loaderChoice.getValue());
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

    private MinecraftVersion getMinecraftFromString(String name) {
        for (MinecraftVersion version : vanillaVersions) {
            if(name.equals(version.getId())) {
                return version;
            }
        }
        LOGGER.warn("Could not find Minecraft version from string: " + versionChoice.getValue());
        return null;
    }

    private FabricVersionDetails getFabricFromString(String name) {
        for (FabricVersionDetails version : fabricVersions) {
            if(name.equals(version.getLoader().getVersion())) {
                return version;
            }
        }
        LOGGER.warn("Could not find Fabric version from string: " + loaderChoice.getValue());
        return null;
    }


    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }

    public boolean checkCreateReady() {
        return !versionChoice.getSelectionModel().isEmpty() && ("Vanilla".equals(typeChoice.getValue()) || "Fabric".equals(typeChoice.getValue()) && !loaderChoice.getSelectionModel().isEmpty());
    }
}
