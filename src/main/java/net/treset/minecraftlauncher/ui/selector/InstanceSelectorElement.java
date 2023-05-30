package net.treset.minecraftlauncher.ui.selector;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.treset.mc_version_loader.launcher.LauncherInstanceDetails;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.creation.VersionCreator;
import net.treset.minecraftlauncher.data.InstanceData;
import net.treset.minecraftlauncher.data.LauncherFiles;
import net.treset.minecraftlauncher.launching.GameLauncher;
import net.treset.minecraftlauncher.ui.base.UiController;
import net.treset.minecraftlauncher.ui.base.UiElement;
import net.treset.minecraftlauncher.ui.generic.ComponentChangerElement;
import net.treset.minecraftlauncher.ui.generic.PopupElement;
import net.treset.minecraftlauncher.ui.generic.SelectorEntryElement;
import net.treset.minecraftlauncher.ui.generic.VersionChangerElement;
import net.treset.minecraftlauncher.ui.manager.InstanceManagerElement;
import net.treset.minecraftlauncher.util.exception.ComponentCreationException;
import net.treset.minecraftlauncher.util.exception.FileLoadException;
import net.treset.minecraftlauncher.util.exception.GameLaunchException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class InstanceSelectorElement extends UiElement {
    private static final Logger LOGGER = LogManager.getLogger(InstanceSelectorElement.class);

    @FXML private AnchorPane rootPane;
    @FXML private VBox instanceContainer;
    @FXML private Button playButton;
    @FXML private Label instanceDetailsTitle;
    @FXML private Button folderButton;
    @FXML private Button deleteButton;
    @FXML private InstanceManagerElement instanceDetailsController;
    @FXML private Button componentFolderButton;
    @FXML private Label componentTitleLabel;
    @FXML private ComponentChangerElement componentChangerController;
    @FXML private VersionChangerElement versionChangerController;
    @FXML private PopupElement popupController;

    private LauncherFiles files;
    private List<Pair<SelectorEntryElement, AnchorPane>> instances = new ArrayList<>();
    private InstanceData currentInstance;

    @Override
    public void init(UiController parent, Function<Boolean, Boolean> lockSetter, Supplier<Boolean> lockGetter, Consumer<Exception> severeExceptionHandler) {
        super.init(parent, lockSetter, lockGetter, severeExceptionHandler);
        instanceDetailsController.init(this::onComponentSelected);
        try {
            files = new LauncherFiles();
        } catch (FileLoadException e) {
            handleSevereException(e);
        }
        versionChangerController.init(files, files.getLauncherDetails().getTypeConversion(), LauncherApplication.config.BASE_DIR + files.getLauncherDetails().getLibrariesDir(), files.getVersionManifest(), this::onVersionChange, this::onVersionChangeFailed);
    }

    public void reloadComponents() {
        try {
            files.reloadAll();
        } catch (FileLoadException e) {
            handleSevereException(e);
        }
        instances = new ArrayList<>();
        for(Pair<LauncherManifest, LauncherInstanceDetails> instance : files.getInstanceComponents()) {
            try {
                instances.add(SelectorEntryElement.from(InstanceData.of(instance, files)));
            } catch (IOException | FileLoadException e) {
                handleSevereException(e);
            }
        }
        popupController.setVisible(false);
        instanceContainer.getChildren().clear();
        for(Pair<SelectorEntryElement, AnchorPane> instance : instances) {
            instanceContainer.getChildren().add(instance.getValue());
            instance.getKey().setSelectionInstanceAcceptor(this::allowSelection);
            instance.getKey().setSelectionInstanceListeners(List.of(this::onSelected));
        }
    }

    @Override
    public void beforeShow(Stage stage) {
        reloadComponents();
        for(Pair<SelectorEntryElement, AnchorPane> instance : instances) {
            instance.getKey().beforeShow(stage);
        }
    }
    @Override
    public void afterShow(Stage stage) {
        for(Pair<SelectorEntryElement, AnchorPane> instance : instances) {
            instance.getKey().afterShow(stage);
        }
    }

    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }

    private void onSelected(InstanceData instanceData, boolean selected) {
        instanceDetailsController.clearSelection();
        componentChangerController.setVisible(false);
        versionChangerController.setVisible(false);
        componentTitleLabel.setDisable(true);
        componentTitleLabel.setText(LauncherApplication.stringLocalizer.get("selector.instance.label.component.title"));
        componentFolderButton.setDisable(true);
        if(selected) {
            for(Pair<SelectorEntryElement, AnchorPane> instance : instances) {
                if(instance.getKey().getInstanceData() != instanceData) {
                    instance.getKey().select(false, true, false);
                }
            }
            playButton.setDisable(false);
            currentInstance = instanceData;
            instanceDetailsTitle.setText(instanceData.getInstance().getKey().getName());
            instanceDetailsTitle.setDisable(false);
            folderButton.setDisable(false);
            deleteButton.setDisable(false);
            instanceDetailsController.populate(instanceData);
            instanceDetailsController.setVisible(true);
        } else {
            playButton.setDisable(true);
            currentInstance = null;
            instanceDetailsTitle.setText(LauncherApplication.stringLocalizer.get("instances.label.details.title"));
            instanceDetailsTitle.setDisable(true);
            folderButton.setDisable(true);
            deleteButton.setDisable(true);
            instanceDetailsController.setVisible(false);
        }
    }

    private boolean allowSelection(InstanceData instanceData, boolean selected) {
        return !getLock();
    }

    public void onPlayButtonClicked() {
        if(currentInstance != null) {
            setLock(true);
            playButton.setDisable(true);
            GameLauncher launcher = new GameLauncher(currentInstance, files, LauncherApplication.userAuth.getMinecraftUser(), List.of(this::onGameExit));
            try {
                launcher.launch(false);
            } catch (GameLaunchException e) {
                displayGameLaunchFailed(e);
                onGameExit(null);
                return;
            }
            displayGameRunning();
        }
    }

    private void onGameExit(String error) {
        setLock(false);
        Platform.runLater(() -> popupController.setVisible(false));
        if(error != null) {
            Platform.runLater(() -> displayGameCrash(error));
        }
        Platform.runLater(() -> playButton.setDisable(false));
    }

    private void onComponentSelected(boolean selected, InstanceManagerElement.SelectedType type) {
        componentChangerController.setVisible(false);
        versionChangerController.setVisible(false);
        componentTitleLabel.setDisable(true);
        componentTitleLabel.setText(LauncherApplication.stringLocalizer.get("selector.instance.label.component.title"));
        componentFolderButton.setDisable(true);
        if(selected && type != null) {
            List<LauncherManifest> manifests;
            LauncherManifest currentManifest;
            String label;
            switch(type) {
                case VERSION -> {
                    versionChangerController.setCurrentVersion(currentInstance.getVersionComponents().get(0).getValue());
                    versionChangerController.setVisible(true);
                    return;
                }
                case SAVES -> {
                    manifests = files.getSavesComponents();
                    currentManifest = currentInstance.getSavesComponent();
                    label = LauncherApplication.stringLocalizer.get("selector.instance.change.saves");
                }
                case RESOURCEPACKS -> {
                    manifests = files.getResourcepackComponents();
                    currentManifest = currentInstance.getResourcepacksComponent();
                    label = LauncherApplication.stringLocalizer.get("selector.instance.change.resourcepacks");
                }
                case OPTIONS -> {
                    manifests = files.getOptionsComponents();
                    currentManifest = currentInstance.getOptionsComponent();
                    label = LauncherApplication.stringLocalizer.get("selector.instance.change.options");
                }
                case MODS -> {
                    manifests = files.getModsComponents().stream().map(Pair::getKey).toList();
                    currentManifest = currentInstance.getModsComponent().getKey();
                    label = LauncherApplication.stringLocalizer.get("selector.instance.change.mods");
                }
                default -> throw new IllegalStateException("Unexpected value: " + type);
            }
            componentChangerController.init(manifests, currentManifest, this::onComponentChanged, this::allowChange);
            componentTitleLabel.setText(label);
            componentTitleLabel.setDisable(false);
            componentFolderButton.setDisable(false);
            componentChangerController.setVisible(true);
        }
    }

    private boolean allowChange() {
        return true;
    }

    private void onComponentChanged(LauncherManifest manifest) {
        switch(instanceDetailsController.getCurrentSelected()) {
            case SAVES -> currentInstance.getInstance().getValue().setSavesComponent(manifest.getId());
            case RESOURCEPACKS -> currentInstance.getInstance().getValue().setResourcepacksComponent(manifest.getId());
            case OPTIONS -> currentInstance.getInstance().getValue().setOptionsComponent(manifest.getId());
            case MODS -> currentInstance.getInstance().getValue().setModsComponent(manifest.getId());
        }
        try {
            currentInstance.getInstance().getValue().writeToFile(currentInstance.getInstance().getKey().getDirectory() + currentInstance.getInstance().getKey().getDetails());
        } catch (IOException e) {
            displayError(e);
        }
        try {
            files.reloadAll();
            currentInstance = InstanceData.of(currentInstance.getInstance(), files);
        } catch (FileLoadException e) {
            handleSevereException(e);
        }
        if(currentInstance != null) {
            instanceDetailsController.populate(currentInstance);
        }
    }

    private void onVersionChange(VersionCreator creator) {
        popupController.clearButtons();
        popupController.setType(PopupElement.PopupType.WARNING);
        popupController.setContent("selector.instance.version.popup.change.title", "selector.instance.version.popup.change.message");
        popupController.addButtons(
                new PopupElement.PopupButton(
                        PopupElement.ButtonType.NEGATIVE,
                        "selector.instance.version.popup.change.cancel",
                        "cancel",
                        this::onPopupChangeCancel
                ),
                new PopupElement.PopupButton(
                        PopupElement.ButtonType.POSITIVE,
                        "selector.instance.version.popup.change.confirm",
                        "confirm",
                        id -> this.onPopupChangeConfirm(id, creator)
                )
        );
        popupController.setVisible(true);
    }

    private void onVersionChangeFailed(Exception e) {
        displayError(e);
    }

    private void onPopupChangeConfirm(String id, VersionCreator creator) {
        popupController.setVisible(false);
        popupController.setContent("selector.instance.version.popup.changing", "");
        popupController.setType(PopupElement.PopupType.NONE);
        popupController.clearButtons();
        popupController.addButtons(
                new PopupElement.PopupButton(
                        PopupElement.ButtonType.POSITIVE,
                        "selector.instance.version.popup.back",
                        "back",
                        this::onPopupBackClicked
                )
        );
        popupController.setControlsDisabled(true);
        popupController.setVisible(true);
        versionChangerController.setVisible(false);
        new Thread(() -> {
            String versionId = null;
            try {
                versionId = creator.getId();
            } catch (ComponentCreationException e) {
                Platform.runLater(() -> {
                    popupController.setType(PopupElement.PopupType.ERROR);
                    popupController.setContent("selector.instance.version.popup.failure", "");
                    popupController.setControlsDisabled(false);
                });
                return;
            }
            currentInstance.getInstance().getValue().setVersionComponent(versionId);
            try {
                currentInstance.getInstance().getValue().writeToFile(currentInstance.getInstance().getKey().getDirectory() + currentInstance.getInstance().getKey().getDetails());
            } catch (IOException e) {
                displayError(e);
            }
            try {
                files.reloadAll();
                currentInstance = InstanceData.of(currentInstance.getInstance(), files);
            } catch (FileLoadException e) {
                handleSevereException(e);
            }
            Platform.runLater(() -> {
                instanceDetailsController.populate(currentInstance);
                versionChangerController.setCurrentVersion(currentInstance.getVersionComponents().get(0).getValue());
                versionChangerController.setVisible(true);
                popupController.setType(PopupElement.PopupType.SUCCESS);
                popupController.setContent("selector.instance.version.popup.success", "");
                popupController.setControlsDisabled(false);
            });
        }).start();
    }

    private void onPopupBackClicked(String id) {
        popupController.setVisible(false);
        setVisible(false);
        setVisible(true);
    }

    private void onPopupChangeCancel(String id) {
        popupController.setVisible(false);
    }

    @FXML
    private void onComponentFolderButtonClicked() {
        switch (instanceDetailsController.getCurrentSelected()) {
            case SAVES -> openFolder(currentInstance.getSavesComponent().getDirectory());
            case RESOURCEPACKS -> openFolder(currentInstance.getResourcepacksComponent().getDirectory());
            case OPTIONS -> openFolder(currentInstance.getOptionsComponent().getDirectory());
            case MODS -> openFolder(currentInstance.getModsComponent().getKey().getDirectory());
        }
    }

    @FXML
    private void onFolderButtonClicked() {
        openFolder(currentInstance.getInstance().getKey().getDirectory());
    }

    @FXML
    private void onDeleteButtonClicked() {
        popupController.setType(PopupElement.PopupType.WARNING);
        popupController.setContent("selector.instance.delete.title", "selector.instance.delete.message");
        popupController.clearButtons();
        popupController.addButtons(
                new PopupElement.PopupButton(
                        PopupElement.ButtonType.NEGATIVE,
                        "selector.instance.delete.cancel",
                        "cancel",
                        this::onDeleteCancel
                ),
                new PopupElement.PopupButton(
                        PopupElement.ButtonType.POSITIVE,
                        "selector.instance.delete.confirm",
                        "confirm",
                        this::onDeleteConfirm
                )
        );
        popupController.setVisible(true);
    }

    private void onDeleteCancel(String id) {
        popupController.setVisible(false);
    }

    private void onDeleteConfirm(String id) {
        popupController.setVisible(false);
        try {
            currentInstance.delete(files);
        } catch (IOException e) {
            displayError(e);
        }
        setVisible(false);
        reloadComponents();
        setVisible(true);
    }

    private void openFolder(String path) {
        try {
            Desktop.getDesktop().open(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void displayGameLaunchFailed(Exception e) {
        LOGGER.error("Failed to launch game", e);
        popupController.setType(PopupElement.PopupType.ERROR);
        popupController.setTitle("selector.instance.error.launch.title");
        popupController.setMessage("selector.instance.error.launch.message", e.getMessage());
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
        popupController.setVisible(true);
    }

    private void displayGameRunning() {
        popupController.setType(PopupElement.PopupType.NONE);
        popupController.setContent("selector.instance.game.running.title", "selector.instance.game.running.message");
        popupController.clearButtons();
        popupController.setVisible(true);
    }

    private void displayGameCrash(String error) {
        popupController.setType(PopupElement.PopupType.WARNING);
        popupController.setTitle("selector.instance.game.crash.title");
        popupController.setMessage("selector.instance.game.crash.message", error.isBlank() ? "unknown error" : error);
        popupController.setControlsDisabled(false);
        popupController.clearButtons();
        popupController.addButtons(
                new PopupElement.PopupButton(
                        PopupElement.ButtonType.POSITIVE,
                        "selector.instance.game.crash.close",
                        "close",
                        id -> popupController.setVisible(false)
                ),
                new PopupElement.PopupButton(
                        PopupElement.ButtonType.POSITIVE,
                        "selector.instance.game.crash.reports",
                        "reports",
                        id -> openFolder(currentInstance.getInstance().getKey().getDirectory() + LauncherApplication.config.INCLUDED_FILES_DIR + "/crash-reports")
                )
        );
        popupController.setVisible(false);
        popupController.setVisible(true);
    }
}
