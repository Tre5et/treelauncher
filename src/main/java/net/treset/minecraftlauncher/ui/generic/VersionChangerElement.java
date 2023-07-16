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
import net.treset.minecraftlauncher.util.exception.ComponentCreationException;
import net.treset.minecraftlauncher.util.ui.VersionCreationHelper;

import java.util.Map;
import java.util.function.Consumer;

public class VersionChangerElement extends UiElement {

    @FXML private AnchorPane rootPane;
    @FXML private ComboBox<MinecraftVersion> cbVersion;
    @FXML private CheckBox chSnapshots;
    @FXML private ComboBox<String> cbType;
    @FXML private ComboBox<FabricVersionDetails> cbLoader;
    @FXML private Button btChange;

    private VersionCreationHelper versionCreationHelper;

    private Consumer<VersionCreator> changeCallback;
    private Consumer<Exception> changeFailCallback;

    public void init(LauncherFiles launcherFiles, Map<String, LauncherManifestType> typeConversion, String librariesDir, LauncherManifest versionManifest, Consumer<VersionCreator> changeCallback, Consumer<Exception> changeFailCallback) {
        versionCreationHelper = new VersionCreationHelper(
                cbVersion,
                chSnapshots,
                cbType,
                cbLoader,
                typeConversion,
                launcherFiles,
                librariesDir,
                versionManifest,
                this::checkCreateReady
        );

        this.changeCallback = changeCallback;
        this.changeFailCallback = changeFailCallback;
        cbType.getItems().clear();
        cbType.getItems().addAll("Vanilla", "Fabric");
        cbVersion.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(this::updateButtonState));
        cbType.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(this::updateButtonState));
        cbLoader.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(this::updateButtonState));
    }

    @Override
    public void beforeShow(Stage stage) {
        versionCreationHelper.beforeShow();
    }

    @Override
    public void afterShow(Stage stage) {}

    @FXML
    private void onChange() {
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
        if(cbVersion.getValue() == null || cbType.getValue() == null) {
            btChange.setDisable(true);
            return;
        }
        btChange.setDisable(versionCreationHelper.getCurrentVersion() != null
                && versionCreationHelper.getCurrentVersion().getVersionNumber().equals(cbVersion.getValue().getId())
                && versionCreationHelper.getCurrentVersion().getVersionType().equals(cbType.getValue().toLowerCase())
                && (versionCreationHelper.getCurrentVersion().getLoaderVersion() == null || versionCreationHelper.getCurrentVersion().getLoaderVersion().equals(cbLoader.getValue().getLoader().getVersion())));
    }

    @FXML
    private void onCheckSnapshots() {
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
        return !cbVersion.getSelectionModel().isEmpty() && ("Vanilla".equals(cbType.getValue()) || "Fabric".equals(cbType.getValue()) && !cbLoader.getSelectionModel().isEmpty());
    }
}
