package net.treset.minecraftlauncher.ui.create;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.treset.minecraftlauncher.config.Config;
import net.treset.minecraftlauncher.creation.InstanceCreator;
import net.treset.minecraftlauncher.data.LauncherFiles;
import net.treset.minecraftlauncher.ui.base.UiElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class InstanceCreatorElement extends UiElement {
    private static final Logger LOGGER = LogManager.getLogger(InstanceCreatorElement.class);

    @FXML private HBox rootPane;
    @FXML private ScrollPane scrollContainer;
    @FXML private TextField nameInput;
    @FXML private Label nameError;
    @FXML private VersionCreatorElement versionCreatorController;
    @FXML private SavesCreatorElement savesCreatorController;
    @FXML private ResourcepacksCreatorElement resourcepacksCreatorController;
    @FXML private OptionsCreatorElement optionsCreatorController;
    @FXML private ModsCreatorElement modsCreatorController;
    @FXML private VBox modsContainer;
    @FXML private Button createButton;

    private LauncherFiles launcherFiles;
    private boolean modsActive = true;

    @FXML
    private void onCreateButtonClicked() {
        modsCreatorController.setGameVersion(versionCreatorController.getGameVersion());
        modsCreatorController.setModsType(versionCreatorController.getVersionType());
        if(checkCreateReady()) {
            InstanceCreator creator = new InstanceCreator(
                    nameInput.getText(),
                    launcherFiles.getLauncherDetails().getTypeConversion(),
                    launcherFiles.getInstanceManifest(),
                    List.of(),
                    List.of(),
                    List.of(),
                    modsActive ? modsCreatorController.getCreator() : null,
                    optionsCreatorController.getCreator(),
                    resourcepacksCreatorController.getCreator(),
                    savesCreatorController.getCreator(),
                    versionCreatorController.getCreator()
            );
            if(creator.getId() == null) {
                LOGGER.warn("Failed to create instance");
            }

        } else {
            showError(true);
            modsCreatorController.setGameVersion(null);
            modsCreatorController.setModsType(null);
        }
    }

    @Override
    public void beforeShow(Stage stage) {
        scrollContainer.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollContainer.setVvalue(0);
        launcherFiles = new LauncherFiles();
        launcherFiles.reloadAll();
        versionCreatorController.setPrerequisites(launcherFiles.getLauncherDetails().getTypeConversion(), launcherFiles.getVersionManifest(), launcherFiles, Config.BASE_DIR + launcherFiles.getLauncherDetails().getLibrariesDir(), this::onModsChange);
        savesCreatorController.setPrerequisites(launcherFiles.getSavesComponents(), launcherFiles.getLauncherDetails().getTypeConversion(), launcherFiles.getSavesManifest(), launcherFiles.getGameDetailsManifest());
        resourcepacksCreatorController.setPrerequisites(launcherFiles.getResourcepackComponents(), launcherFiles.getLauncherDetails().getTypeConversion(), launcherFiles.getResourcepackManifest());
        optionsCreatorController.setPrerequisites(launcherFiles.getOptionsComponents(), launcherFiles.getLauncherDetails().getTypeConversion(), launcherFiles.getOptionsManifest());
        modsCreatorController.setPrerequisites(launcherFiles.getModsComponents(), launcherFiles.getLauncherDetails().getTypeConversion(), launcherFiles.getModsManifest(), launcherFiles.getGameDetailsManifest());
        versionCreatorController.beforeShow(stage);
        savesCreatorController.beforeShow(stage);
        resourcepacksCreatorController.beforeShow(stage);
        optionsCreatorController.beforeShow(stage);
        modsCreatorController.beforeShow(stage);
        onModsChange(false);
    }

    @Override
    public void afterShow(Stage stage) {}

    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }

    public void showError(boolean show) {
        nameError.setVisible(false);
        nameInput.getStyleClass().remove("error");
        if(show && nameInput.getText().isEmpty()) {
            nameError.setVisible(true);
            nameInput.getStyleClass().add("error");
        }
        versionCreatorController.showError(show);
        savesCreatorController.showError(show);
        resourcepacksCreatorController.showError(show);
        optionsCreatorController.showError(show);
        modsCreatorController.showError(show);
    }

    public boolean checkCreateReady() {
        return !nameInput.getText().isEmpty() && versionCreatorController.checkCreateReady() && savesCreatorController.checkCreateReady() && resourcepacksCreatorController.checkCreateReady() && optionsCreatorController.checkCreateReady() && (!modsActive || modsCreatorController.checkCreateReady());
    }

    public void onModsChange(boolean active) {
        if(active == modsActive) {
            return;
        }
        modsActive = active;
        modsContainer.setDisable(!active);
    }
}
