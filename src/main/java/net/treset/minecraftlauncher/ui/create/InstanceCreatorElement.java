package net.treset.minecraftlauncher.ui.create;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.creation.InstanceCreator;
import net.treset.minecraftlauncher.data.LauncherFiles;
import net.treset.minecraftlauncher.ui.base.UiElement;
import net.treset.minecraftlauncher.ui.generic.PopupElement;
import net.treset.minecraftlauncher.util.CreationStatus;
import net.treset.minecraftlauncher.util.exception.ComponentCreationException;
import net.treset.minecraftlauncher.util.exception.FileLoadException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class InstanceCreatorElement extends UiElement {
    private static final Logger LOGGER = LogManager.getLogger(InstanceCreatorElement.class);

    @FXML private HBox rootPane;
    @FXML private ScrollPane spContainer;
    @FXML private TextField nameInput;
    @FXML private Label lbNameError;
    @FXML private VersionCreatorElement vcCreator;
    @FXML private SavesCreatorElement icSavesCreatorController;
    @FXML private ResourcepacksCreatorElement icResourcepacksCreatorController;
    @FXML private OptionsCreatorElement icOptionsCreatorController;
    @FXML private ModsCreatorElement icModsCreatorController;
    @FXML private VBox modsContainer;
    @FXML private Button btCreate;
    private LauncherFiles launcherFiles;
    private boolean modsActive = true;

    private PopupElement popup;

    private void onCreateStatusChanged(CreationStatus status) {
        StringBuilder message = new StringBuilder(status.getCurrentStep().getMessage());
        if(status.getDownloadStatus() != null) {
            message.append("\n").append(status.getDownloadStatus().getCurrentFile()).append("\n(").append(status.getDownloadStatus().getCurrentAmount()).append("/").append(status.getDownloadStatus().getTotalAmount()).append(")");
        }
        Platform.runLater(()-> {
            if(popup == null) return;
            popup.setMessage(message.toString());
        });
    }

    @FXML
    private void onCreate() {
        icModsCreatorController.setGameVersion(vcCreator.getGameVersion());
        icModsCreatorController.setModsType(vcCreator.getVersionType());
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
                        modsActive ? icModsCreatorController.getCreator() : null,
                        icOptionsCreatorController.getCreator(),
                        icResourcepacksCreatorController.getCreator(),
                        icSavesCreatorController.getCreator(),
                        vcCreator.getCreator()
                );
                creator.setStatusCallback(this::onCreateStatusChanged);
            } catch (ComponentCreationException e) {
                LauncherApplication.displayError(e);
                return;
            }
            popup = new PopupElement(
                    "creator.instance.popup.label.creating",
                    null
            );
            LauncherApplication.setPopup(popup);
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
            icModsCreatorController.setGameVersion(null);
            icModsCreatorController.setModsType(null);
        }
    }

    private void onInstanceCreationSuccess() {
        LOGGER.info("Created instance");
        popup = new PopupElement(
                PopupElement.PopupType.SUCCESS,
                "creator.instance.popup.label.success",
                null,
                List.of(
                        new PopupElement.PopupButton(
                                PopupElement.ButtonType.POSITIVE,
                                "creator.instance.popup.button.back",
                                this::onBackButtonClicked
                        )
                )
        );
        LauncherApplication.setPopup(popup);
        setLock(false);
    }

    private void onInstanceCreationFailure(Exception e) {
        LOGGER.error("Failed to create instance", e);
        popup = new PopupElement(
                PopupElement.PopupType.ERROR,
                "creator.instance.popup.label.failure",
                LauncherApplication.stringLocalizer.getFormatted("error.message", e.getMessage()),
                List.of(
                        new PopupElement.PopupButton(
                                PopupElement.ButtonType.POSITIVE,
                                "creator.instance.popup.button.back",
                                this::onBackButtonClicked
                        )
                )
        );
        LauncherApplication.setPopup(popup);
        setLock(false);
    }

    private void onBackButtonClicked(ActionEvent event) {
        LauncherApplication.setPopup(null);
        triggerHomeAction();
    }

    @Override
    public void beforeShow(Stage stage) {
        spContainer.getStyleClass().remove("popup-background");
        spContainer.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spContainer.setVvalue(0);
        try {
            launcherFiles = new LauncherFiles();
            launcherFiles.reloadAll();
        } catch (FileLoadException e) {
            LauncherApplication.displaySevereError(e);
        }
        lbNameError.setVisible(false);
        nameInput.getStyleClass().remove("error");
        nameInput.setText("");
        vcCreator.init(launcherFiles.getLauncherDetails().getTypeConversion(), launcherFiles.getVersionManifest(), launcherFiles, LauncherApplication.config.BASE_DIR + launcherFiles.getLauncherDetails().getLibrariesDir(), this::onModsChange);
        icSavesCreatorController.setPrerequisites(launcherFiles.getSavesComponents(), launcherFiles.getLauncherDetails().getTypeConversion(), launcherFiles.getSavesManifest(), launcherFiles.getGameDetailsManifest());
        icResourcepacksCreatorController.setPrerequisites(launcherFiles.getResourcepackComponents(), launcherFiles.getLauncherDetails().getTypeConversion(), launcherFiles.getResourcepackManifest());
        icOptionsCreatorController.setPrerequisites(launcherFiles.getOptionsComponents(), launcherFiles.getLauncherDetails().getTypeConversion(), launcherFiles.getOptionsManifest());
        icModsCreatorController.setPrerequisites(launcherFiles.getModsComponents(), launcherFiles.getLauncherDetails().getTypeConversion(), launcherFiles.getModsManifest(), launcherFiles.getGameDetailsManifest());
        icSavesCreatorController.beforeShow(stage);
        icResourcepacksCreatorController.beforeShow(stage);
        icOptionsCreatorController.beforeShow(stage);
        icModsCreatorController.beforeShow(stage);
        onModsChange(false);
    }

    @Override
    public void afterShow(Stage stage) {}

    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }

    public void showError(boolean show) {
        lbNameError.setVisible(false);
        nameInput.getStyleClass().remove("error");
        if(show && nameInput.getText().isEmpty()) {
            lbNameError.setVisible(true);
            nameInput.getStyleClass().add("error");
        }
        vcCreator.showError(show);
        icSavesCreatorController.showError(show);
        icResourcepacksCreatorController.showError(show);
        icOptionsCreatorController.showError(show);
        if(modsActive)
            icModsCreatorController.showError(show);
    }

    public boolean checkCreateReady() {
        return !nameInput.getText().isEmpty() && vcCreator.isCreateReady() && icSavesCreatorController.checkCreateReady() && icResourcepacksCreatorController.checkCreateReady() && icOptionsCreatorController.checkCreateReady() && (!modsActive || icModsCreatorController.checkCreateReady());
    }

    public void onModsChange(boolean active) {
        if(active == modsActive) {
            return;
        }
        modsActive = active;
        modsContainer.setDisable(!active);
    }
}
