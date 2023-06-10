package net.treset.minecraftlauncher.ui.generic;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import net.treset.mc_version_loader.fabric.FabricVersionDetails;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.mc_version_loader.launcher.LauncherVersionDetails;
import net.treset.mc_version_loader.minecraft.MinecraftVersion;
import net.treset.minecraftlauncher.creation.VersionCreator;
import net.treset.minecraftlauncher.data.LauncherFiles;
import net.treset.minecraftlauncher.ui.base.UiElement;
import net.treset.minecraftlauncher.util.ui.VersionCreationHelper;
import net.treset.minecraftlauncher.ui.create.VersionCreatorElement;
import net.treset.minecraftlauncher.util.exception.ComponentCreationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.function.Consumer;

public class VersionChangerElement extends UiElement {
    private static final Logger LOGGER = LogManager.getLogger(VersionCreatorElement.class);

    @FXML private AnchorPane rootPane;
    @FXML private ComboBox<MinecraftVersion> versionChoice;
    @FXML private CheckBox snapshotsCheck;
    @FXML private ComboBox<String> typeChoice;
    @FXML private ComboBox<FabricVersionDetails> loaderChoice;
    @FXML private Button changeButton;

    private VersionCreationHelper versionCreationHelper;

    private Consumer<VersionCreator> changeCallback;
    private Consumer<Exception> changeFailCallback;

    public void init(LauncherFiles launcherFiles, Map<String, LauncherManifestType> typeConversion, String librariesDir, LauncherManifest versionManifest, Consumer<VersionCreator> changeCallback, Consumer<Exception> changeFailCallback) {
        versionCreationHelper = new VersionCreationHelper(
                versionChoice,
                snapshotsCheck,
                typeChoice,
                loaderChoice,
                typeConversion,
                launcherFiles,
                librariesDir,
                versionManifest,
                this::checkCreateReady
        );

        this.changeCallback = changeCallback;
        this.changeFailCallback = changeFailCallback;
        typeChoice.getItems().clear();
        typeChoice.getItems().addAll("Vanilla", "Fabric");
        versionChoice.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(this::updateButtonState);
        });
        typeChoice.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(this::updateButtonState);
        });
        loaderChoice.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(this::updateButtonState);
        });
    }

    @Override
    public void beforeShow(Stage stage) {
        versionCreationHelper.beforeShow();
    }

    @Override
    public void afterShow(Stage stage) {}

    @FXML
    private void onChangeButtonClicked() {
        if(checkCreateReady()) {
            try {
                VersionCreator creator = versionCreationHelper.getCreator();
                changeCallback.accept(creator);
            } catch (ComponentCreationException e) {
                changeFailCallback.accept(e);
            }
        }
    }

    private void updateButtonState() {
        changeButton.setDisable(versionCreationHelper.getCurrentVersion() != null
                && versionCreationHelper.getCurrentVersion().getVersionNumber().equals(versionChoice.getValue().getId())
                && versionCreationHelper.getCurrentVersion().getVersionType().equals(typeChoice.getValue().toLowerCase())
                && (versionCreationHelper.getCurrentVersion().getLoaderVersion() == null || versionCreationHelper.getCurrentVersion().getLoaderVersion().equals(loaderChoice.getValue())));
    }

    @FXML
    private void onSnapshotsChecked() {
        versionCreationHelper.populateVersionChoice();
    }

    public void setCurrentVersion(LauncherVersionDetails currentVersion) {
        versionCreationHelper.setCurrentVersion(currentVersion);
    }



    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }

    public boolean checkCreateReady() {
        return !versionChoice.getSelectionModel().isEmpty() && ("Vanilla".equals(typeChoice.getValue()) || "Fabric".equals(typeChoice.getValue()) && !loaderChoice.getSelectionModel().isEmpty());
    }
}
