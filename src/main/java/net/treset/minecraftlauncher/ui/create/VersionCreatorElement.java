package net.treset.minecraftlauncher.ui.create;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.treset.mc_version_loader.VersionLoader;
import net.treset.mc_version_loader.exception.FileDownloadException;
import net.treset.mc_version_loader.fabric.FabricProfile;
import net.treset.mc_version_loader.fabric.FabricVersionDetails;
import net.treset.mc_version_loader.files.Sources;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.mc_version_loader.minecraft.MinecraftVersion;
import net.treset.mc_version_loader.minecraft.MinecraftVersionDetails;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.creation.VersionCreator;
import net.treset.minecraftlauncher.data.LauncherFiles;
import net.treset.minecraftlauncher.ui.base.UiElement;
import net.treset.minecraftlauncher.util.exception.ComponentCreationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class VersionCreatorElement extends UiElement {
    private static final Logger LOGGER = LogManager.getLogger(VersionCreatorElement.class);

    @FXML private VBox rootBox;
    @FXML private ComboBox<String> versionChoice;
    @FXML private ComboBox<String> typeChoice;
    @FXML private ComboBox<String> loaderChoice;
    @FXML private CheckBox snapshotsCheck;
    @FXML private Label errorVersion;
    @FXML private Label errorType;
    @FXML private Label errorLoader;

    private LauncherManifest versionManifest;
    private Map<String, LauncherManifestType> typeConversion;
    private List<FabricVersionDetails> fabricVersions;
    private List<MinecraftVersion> minecraftVersions;
    private LauncherFiles launcherFiles;
    private String librariesDir;
    private Consumer<Boolean> modsActivateCallback;

    public void setPrerequisites(Map<String, LauncherManifestType> typeConversion, LauncherManifest versionManifest, LauncherFiles launcherFiles, String librariesDir, Consumer<Boolean> modsActivateCallback) {
        this.typeConversion = typeConversion;
        this.versionManifest = versionManifest;
        this.launcherFiles = launcherFiles;
        this.librariesDir = librariesDir;
        this.modsActivateCallback = modsActivateCallback;
    }

    public String getGameVersion() {
        return versionChoice.getValue();
    }
    public String getVersionType() {
        if("Fabric".equals(typeChoice.getValue())) {
            return "fabric";
        }
        return null;
    }


    @Override
    public void beforeShow(Stage stage) {
        typeChoice.setVisible(false);
        loaderChoice.setVisible(false);
        errorVersion.setVisible(false);
        errorType.setVisible(false);
        errorLoader.setVisible(false);
        versionChoice.getStyleClass().remove("error");
        typeChoice.getStyleClass().remove("error");
        loaderChoice.getStyleClass().remove("error");
        populateVersionChoice();
        versionChoice.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(this::updateTypeState);
        });
        typeChoice.getItems().clear();
        typeChoice.getItems().addAll("Vanilla", "Fabric");
        typeChoice.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(this::updateLoaderState);
        });
    }

    @FXML private void onSnapshotsCheck() {
        populateVersionChoice();
    }

    private void populateVersionChoice() {
        versionChoice.getItems().clear();
        versionChoice.setPromptText(LauncherApplication.stringLocalizer.get("creator.version.prompt.loading"));
        versionChoice.setDisable(true);
        updateTypeState();
        new Thread(() -> {
            try {
                minecraftVersions = snapshotsCheck.isSelected() ? VersionLoader.getVersions() : VersionLoader.getReleases();
            } catch (FileDownloadException e) {
                LOGGER.error("Failed to download versions", e);
                return;
            }
            List<String> names = new ArrayList<>();
            for (MinecraftVersion version : minecraftVersions) {
                names.add(version.getId());
            }
            Platform.runLater(() -> {
                versionChoice.getItems().addAll(names);
                versionChoice.setPromptText(LauncherApplication.stringLocalizer.get("creator.version.prompt.version"));
                versionChoice.setDisable(false);
            });
        }).start();
    }

    private void updateTypeState() {
        if(versionChoice.getValue() == null || versionChoice.getValue().isEmpty() || versionChoice.getValue().equals(LauncherApplication.stringLocalizer.get("creator.version.prompt.loading"))) {
            typeChoice.setVisible(false);
            typeChoice.getSelectionModel().clearSelection();
        } else {
            typeChoice.setVisible(true);
            errorType.setVisible(false);
        }
        updateLoaderState();
    }

    private void updateLoaderState() {
        if("Fabric".equals(typeChoice.getValue())) {
            modsActivateCallback.accept(true);
            loaderChoice.getItems().clear();
            loaderChoice.setPromptText(LauncherApplication.stringLocalizer.get("creator.version.prompt.loading"));
            loaderChoice.setDisable(true);
            loaderChoice.setVisible(true);
            new Thread(() -> {
                try {
                    fabricVersions = FabricVersionDetails.fromJsonArray(Sources.getFabricForMinecraftVersion(versionChoice.getValue()));
                } catch (FileDownloadException e) {
                    LOGGER.warn("Failed to download fabric versions", e);
                    return;
                }
                List<String> names = new ArrayList<>();
                for (FabricVersionDetails version : fabricVersions) {
                    names.add(version.getLoader().getVersion());
                }
                Platform.runLater(() -> {
                    loaderChoice.getItems().addAll(names);
                    loaderChoice.setPromptText(LauncherApplication.stringLocalizer.get("creator.version.prompt.loaderversion"));
                    loaderChoice.setDisable(false);
                });
            }).start();
        } else {
            modsActivateCallback.accept(false);
            loaderChoice.setVisible(false);
            loaderChoice.getItems().clear();
            loaderChoice.getSelectionModel().clearSelection();
            errorLoader.setVisible(false);
        }
    }

    @Override
    public void afterShow(Stage stage) {}

    @Override
    public void setRootVisible(boolean visible) {
        rootBox.setVisible(visible);
    }

    public boolean create() throws ComponentCreationException {
        VersionCreator creator = getCreator();
        return creator.getId() != null;
    }

    public VersionCreator getCreator() throws ComponentCreationException {
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
                throw new ComponentCreationException("Could not get Fabric version details");
            }
            FabricProfile profile = null;
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
        for (MinecraftVersion version : minecraftVersions) {
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

    public void showError(boolean show) {
        errorVersion.setVisible(false);
        versionChoice.getStyleClass().remove("error");
        errorType.setVisible(false);
        typeChoice.getStyleClass().remove("error");
        errorLoader.setVisible(false);
        loaderChoice.getStyleClass().remove("error");
        if(show) {
            if(versionChoice.getSelectionModel().isEmpty()) {
                errorVersion.setVisible(true);
                versionChoice.getStyleClass().add("error");
            }
            else if(!"Vanilla".equals(typeChoice.getValue()) && !"Fabric".equals(typeChoice.getValue())) {
                errorType.setVisible(true);
                typeChoice.getStyleClass().add("error");
            }
            else if("Fabric".equals(typeChoice.getValue()) && loaderChoice.getSelectionModel().isEmpty()) {
                errorLoader.setVisible(true);
                loaderChoice.getStyleClass().add("error");
            }
        }
    }

    public boolean checkCreateReady() {
        boolean result = !versionChoice.getSelectionModel().isEmpty() && ("Vanilla".equals(typeChoice.getValue()) || "Fabric".equals(typeChoice.getValue()) && !loaderChoice.getSelectionModel().isEmpty());
        return result;
    }
}
