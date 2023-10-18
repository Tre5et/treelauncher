package net.treset.minecraftlauncher.ui.create;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.creation.InstanceCreator;
import net.treset.minecraftlauncher.data.LauncherFiles;
import net.treset.minecraftlauncher.ui.base.UiElement;
import net.treset.minecraftlauncher.ui.generic.ErrorWrapper;
import net.treset.minecraftlauncher.ui.generic.popup.PopupElement;
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
    @FXML private TextField tfName;
    @FXML private ErrorWrapper ewName;
    @FXML private VersionCreatorElement crVersion;
    @FXML private SavesCreatorElement crSaves;
    @FXML private ResourcepacksCreatorElement crResourcepacks;
    @FXML private OptionsCreatorElement crOptions;
    @FXML private ModsCreatorElement crMods;
    @FXML private VBox modsContainer;
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
        crMods.setGameVersion(crVersion.getGameVersion());
        crMods.setModsType(crVersion.getVersionType());
        if(checkCreateReady() && setLock(true)) {
            InstanceCreator creator;
            try {
                creator = new InstanceCreator(
                        tfName.getText(),
                        launcherFiles.getLauncherDetails().getTypeConversion(),
                        launcherFiles.getInstanceManifest(),
                        List.of(),
                        List.of(),
                        List.of(),
                        modsActive ? crMods.getCreator() : null,
                        crOptions.getCreator(),
                        crResourcepacks.getCreator(),
                        crSaves.getCreator(),
                        crVersion.getCreator()
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
            crMods.setGameVersion(null);
            crMods.setModsType(null);
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
        ewName.showError(false);
        tfName.getStyleClass().remove("error");
        tfName.setText("");
        crVersion.init(launcherFiles.getLauncherDetails().getTypeConversion(), launcherFiles.getVersionManifest(), launcherFiles, LauncherApplication.config.BASE_DIR + launcherFiles.getLauncherDetails().getLibrariesDir(), this::onModsChange);
        crSaves.init(launcherFiles.getSavesComponents(), launcherFiles.getLauncherDetails().getTypeConversion(), launcherFiles.getSavesManifest(), launcherFiles.getGameDetailsManifest());
        crResourcepacks.init(launcherFiles.getResourcepackComponents(), launcherFiles.getLauncherDetails().getTypeConversion(), launcherFiles.getResourcepackManifest());
        crOptions.init(launcherFiles.getOptionsComponents(), launcherFiles.getLauncherDetails().getTypeConversion(), launcherFiles.getOptionsManifest());
        crMods.init(launcherFiles.getModsComponents(), launcherFiles.getLauncherDetails().getTypeConversion(), launcherFiles.getModsManifest(), launcherFiles.getGameDetailsManifest());
        onModsChange(false);
    }

    @Override
    public void afterShow(Stage stage) {}

    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }

    public void showError(boolean show) {
        ewName.showError(false);
        tfName.getStyleClass().remove("error");
        if(show && tfName.getText().isEmpty()) {
            ewName.showError(true);
        }
        crVersion.showError(show);
        crSaves.showError(show);
        crOptions.showError(show);
        crResourcepacks.showError(show);
        if(modsActive)
            crMods.showError(show);
    }

    public boolean checkCreateReady() {
        return !tfName.getText().isEmpty() && crVersion.isCreateReady() && crSaves.isCreateReady() && crResourcepacks.isCreateReady() && crOptions.isCreateReady() && (!modsActive || crMods.isCreateReady());
    }

    public void onModsChange(boolean active) {
        if(active == modsActive) {
            return;
        }
        modsActive = active;
        modsContainer.setDisable(!active);
    }
}
