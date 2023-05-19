package net.treset.minecraftlauncher.ui.create;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.treset.minecraftlauncher.LauncherApplication;
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
    @FXML private GridPane creationPopup;
    @FXML private Label popupLabel;
    @FXML private Button popupBackButton;

    private LauncherFiles launcherFiles;
    private boolean modsActive = true;

    @FXML
    private void onCreateButtonClicked() {
        modsCreatorController.setGameVersion(versionCreatorController.getGameVersion());
        modsCreatorController.setModsType(versionCreatorController.getVersionType());
        if(checkCreateReady() && setLock(true)) {
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
            popupBackButton.setDisable(true);
            popupLabel.setText(LauncherApplication.stringLocalizer.get("creator.instance.popup.label.creating"));
            scrollContainer.getStyleClass().add("popup-background");
            creationPopup.setVisible(true);
            new Thread(() -> onInstanceCreationDone(creator.getId() != null)).start();

        } else {
            showError(true);
            modsCreatorController.setGameVersion(null);
            modsCreatorController.setModsType(null);
        }
    }

    private void onInstanceCreationDone(boolean success) {
        Platform.runLater(() -> {
            if(success) {
                popupLabel.setText(LauncherApplication.stringLocalizer.get("creator.instance.popup.label.success"));
            } else {
                popupLabel.setText(LauncherApplication.stringLocalizer.get("creator.instance.popup.label.failure"));
            }
            popupBackButton.setDisable(false);
            setLock(false);
        });
    }

    @FXML
    private void onBackButtonClicked() {
        triggerHomeAction();
    }

    @Override
    public void beforeShow(Stage stage) {
        creationPopup.setVisible(false);
        scrollContainer.getStyleClass().remove("popup-background");
        popupBackButton.setDisable(true);
        scrollContainer.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollContainer.setVvalue(0);
        launcherFiles = new LauncherFiles();
        launcherFiles.reloadAll();
        nameError.setVisible(false);
        nameInput.getStyleClass().remove("error");
        nameInput.setText("");
        versionCreatorController.setPrerequisites(launcherFiles.getLauncherDetails().getTypeConversion(), launcherFiles.getVersionManifest(), launcherFiles, LauncherApplication.config.BASE_DIR + launcherFiles.getLauncherDetails().getLibrariesDir(), this::onModsChange);
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
        if(modsActive)
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
