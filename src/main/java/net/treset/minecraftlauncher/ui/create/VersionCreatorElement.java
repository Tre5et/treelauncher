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
import net.treset.minecraftlauncher.util.ui.VersionCreationHelper;
import net.treset.minecraftlauncher.util.exception.ComponentCreationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class VersionCreatorElement extends UiElement {
    @FXML private VBox rootBox;
    @FXML private ComboBox<MinecraftVersion> versionChoice;
    @FXML private ComboBox<String> typeChoice;
    @FXML private ComboBox<FabricVersionDetails> loaderChoice;
    @FXML private CheckBox snapshotsCheck;
    @FXML private Label errorVersion;
    @FXML private Label errorType;
    @FXML private Label errorLoader;

    private VersionCreationHelper versionCreationHelper;

    private Consumer<Boolean> modsActivateCallback;

    public void setPrerequisites(Map<String, LauncherManifestType> typeConversion, LauncherManifest versionManifest, LauncherFiles launcherFiles, String librariesDir, Consumer<Boolean> modsActivateCallback) {
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

        this.modsActivateCallback = modsActivateCallback;

        typeChoice.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(this::updateLoaderState);
        });
    }

    public String getGameVersion() {
        return versionChoice.getValue().getId();
    }
    public String getVersionType() {
        if("Fabric".equals(typeChoice.getValue())) {
            return "fabric";
        }
        return null;
    }


    @Override
    public void beforeShow(Stage stage) {
        versionCreationHelper.beforeShow();
        errorVersion.setVisible(false);
        errorType.setVisible(false);
        errorLoader.setVisible(false);
        versionChoice.getStyleClass().remove("error");
        typeChoice.getStyleClass().remove("error");
        loaderChoice.getStyleClass().remove("error");
    }

    @FXML private void onSnapshotsCheck() {
        versionCreationHelper.populateVersionChoice();
    }

    private void updateLoaderState() {
        if("Fabric".equals(typeChoice.getValue())) {
            modsActivateCallback.accept(true);
        } else {
            modsActivateCallback.accept(false);
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
        VersionCreator creator = versionCreationHelper.getCreator();
        return creator.getId() != null;
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

    public VersionCreator getCreator() throws ComponentCreationException {
        return versionCreationHelper.getCreator();
    }

    public boolean checkCreateReady() {
        boolean result = !versionChoice.getSelectionModel().isEmpty() && ("Vanilla".equals(typeChoice.getValue()) || "Fabric".equals(typeChoice.getValue()) && !loaderChoice.getSelectionModel().isEmpty());
        return result;
    }
}
