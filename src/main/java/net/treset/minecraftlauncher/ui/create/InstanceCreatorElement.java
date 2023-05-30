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
import net.treset.minecraftlauncher.ui.generic.PopupElement;
import net.treset.minecraftlauncher.util.exception.ComponentCreationException;
import net.treset.minecraftlauncher.util.exception.FileLoadException;
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
    @FXML private PopupElement popupController;
    private LauncherFiles launcherFiles;
    private boolean modsActive = true;

    @FXML
    private void onCreateButtonClicked() {
        modsCreatorController.setGameVersion(versionCreatorController.getGameVersion());
        modsCreatorController.setModsType(versionCreatorController.getVersionType());
        if(checkCreateReady() && setLock(true)) {
            InstanceCreator creator;
            try {
                creator = new InstanceCreator(
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
            } catch (ComponentCreationException e) {
                displayError(e);
                return;
            }
            scrollContainer.getStyleClass().add("popup-background");
            popupController.setType(PopupElement.PopupType.NONE);
            popupController.setContent("creator.instance.popup.label.creating", "");
            popupController.setVisible(true);
            new Thread(() -> {
                try {
                    creator.getId();
                    onInstanceCreationSuccess();
                } catch (Exception e) {
                    onInstanceCreationFailure(e);
                }
            }).start();

        } else {
            showError(true);
            modsCreatorController.setGameVersion(null);
            modsCreatorController.setModsType(null);
        }
    }

    private void onInstanceCreationSuccess() {
        LOGGER.info("Created instance");
        Platform.runLater(() -> {
            popupController.setType(PopupElement.PopupType.SUCCESS);
            popupController.setContent("creator.instance.popup.label.success", "");
            popupController.setControlsDisabled(false);
            setLock(false);
        });
    }

    private void onInstanceCreationFailure(Exception e) {
        LOGGER.error("Failed to create instance", e);
        Platform.runLater(() -> {
            popupController.setType(PopupElement.PopupType.ERROR);
            popupController.setTitle("creator.instance.popup.label.failure");
            popupController.setMessage("error.message", e.getMessage());
            popupController.setControlsDisabled(false);
            setLock(false);
        });
    }

    private void onBackButtonClicked(String id) {
        triggerHomeAction();
    }

    @Override
    public void beforeShow(Stage stage) {
        popupController.setContent("creator.instance.popup.label.undefined", "");
        popupController.clearButtons();
        popupController.addButtons(new PopupElement.PopupButton(
                PopupElement.ButtonType.POSITIVE,
                "creator.instance.popup.button.back",
                "backButton",
                this::onBackButtonClicked
        ));
        popupController.setControlsDisabled(true);
        popupController.setVisible(false);
        scrollContainer.getStyleClass().remove("popup-background");
        scrollContainer.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollContainer.setVvalue(0);
        try {
            launcherFiles = new LauncherFiles();
            launcherFiles.reloadAll();
        } catch (FileLoadException e) {
            handleSevereException(e);
        }
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

    private void displayError(Exception e) {
        LOGGER.error("An error occurred", e);
        popupController.setType(PopupElement.PopupType.ERROR);
        popupController.setTitle("error.title");
        popupController.setMessage("error.message", e.getMessage());
        popupController.setControlsDisabled(false);
        popupController.clearButtons();
        popupController.addButtons(
                new PopupElement.PopupButton(
                        PopupElement.ButtonType.POSITIVE,
                        "error.close",
                        "close",
                        id -> popupController.setVisible(false)
                )
        );
    }
}
