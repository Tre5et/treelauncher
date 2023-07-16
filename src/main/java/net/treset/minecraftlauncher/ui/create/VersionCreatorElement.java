package net.treset.minecraftlauncher.ui.create;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.treset.mc_version_loader.fabric.FabricVersionDetails;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.mc_version_loader.minecraft.MinecraftVersion;
import net.treset.minecraftlauncher.creation.VersionCreator;
import net.treset.minecraftlauncher.data.LauncherFiles;
import net.treset.minecraftlauncher.ui.base.UiElement;
import net.treset.minecraftlauncher.util.exception.ComponentCreationException;
import net.treset.minecraftlauncher.util.ui.VersionCreationHelper;

import java.util.Map;
import java.util.function.Consumer;

public class VersionCreatorElement extends UiElement {
    @FXML private VBox rootBox;
    @FXML private ComboBox<MinecraftVersion> cbVersion;
    @FXML private ComboBox<String> cbType;
    @FXML private ComboBox<FabricVersionDetails> cbLoader;
    @FXML private CheckBox chSnapshots;
    @FXML private Label lbVersionError;
    @FXML private Label lbTypeError;
    @FXML private Label lbLoaderError;

    private VersionCreationHelper versionCreationHelper;

    private Consumer<Boolean> modsActivateCallback;

    public void setPrerequisites(Map<String, LauncherManifestType> typeConversion, LauncherManifest versionManifest, LauncherFiles launcherFiles, String librariesDir, Consumer<Boolean> modsActivateCallback) {
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

        this.modsActivateCallback = modsActivateCallback;

        cbType.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(this::updateLoaderState));
    }

    public String getGameVersion() {
        return cbVersion.getValue().getId();
    }
    public String getVersionType() {
        if("Fabric".equals(cbType.getValue())) {
            return "fabric";
        }
        return null;
    }


    @Override
    public void beforeShow(Stage stage) {
        versionCreationHelper.beforeShow();
        lbVersionError.setVisible(false);
        lbTypeError.setVisible(false);
        lbLoaderError.setVisible(false);
        cbVersion.getStyleClass().remove("error");
        cbType.getStyleClass().remove("error");
        cbLoader.getStyleClass().remove("error");
    }

    @FXML private void onCheckSnapshots() {
        versionCreationHelper.populateVersionChoice();
    }

    private void updateLoaderState() {
        if("Fabric".equals(cbType.getValue())) {
            modsActivateCallback.accept(true);
        } else {
            modsActivateCallback.accept(false);
            lbLoaderError.setVisible(false);
        }
    }

    @Override
    public void afterShow(Stage stage) {}

    @Override
    public void setRootVisible(boolean visible) {
        rootBox.setVisible(visible);
    }

    public boolean create() throws ComponentCreationException {
        VersionCreator creator = versionCreationHelper.getCreator();
        return creator.getId() != null;
    }

    public void showError(boolean show) {
        lbVersionError.setVisible(false);
        cbVersion.getStyleClass().remove("error");
        lbTypeError.setVisible(false);
        cbType.getStyleClass().remove("error");
        lbLoaderError.setVisible(false);
        cbLoader.getStyleClass().remove("error");
        if(show) {
            if(cbVersion.getSelectionModel().isEmpty()) {
                lbVersionError.setVisible(true);
                cbVersion.getStyleClass().add("error");
            }
            else if(!"Vanilla".equals(cbType.getValue()) && !"Fabric".equals(cbType.getValue())) {
                lbTypeError.setVisible(true);
                cbType.getStyleClass().add("error");
            }
            else if("Fabric".equals(cbType.getValue()) && cbLoader.getSelectionModel().isEmpty()) {
                lbLoaderError.setVisible(true);
                cbLoader.getStyleClass().add("error");
            }
        }
    }

    public VersionCreator getCreator() throws ComponentCreationException {
        return versionCreationHelper.getCreator();
    }

    public boolean checkCreateReady() {
        return !cbVersion.getSelectionModel().isEmpty() && ("Vanilla".equals(cbType.getValue()) || "Fabric".equals(cbType.getValue()) && !cbLoader.getSelectionModel().isEmpty());
    }
}
