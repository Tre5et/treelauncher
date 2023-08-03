package net.treset.minecraftlauncher.ui.selector;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.treset.mc_version_loader.launcher.LauncherInstanceDetails;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.config.Settings;
import net.treset.minecraftlauncher.creation.VersionCreator;
import net.treset.minecraftlauncher.data.InstanceData;
import net.treset.minecraftlauncher.launching.GameLauncher;
import net.treset.minecraftlauncher.ui.base.UiController;
import net.treset.minecraftlauncher.ui.generic.*;
import net.treset.minecraftlauncher.ui.manager.InstanceManagerElement;
import net.treset.minecraftlauncher.util.FormatUtil;
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
import java.util.function.Function;
import java.util.function.Supplier;

public class InstanceSelectorElement extends SelectorElement<InstanceSelectorEntryElement> {
    private static final Logger LOGGER = LogManager.getLogger(InstanceSelectorElement.class);

    @FXML private IconButton btSort;
    @FXML private ComboBox<Settings.InstanceDataSortType> cbSort;
    @FXML private InstanceManagerElement icDetailsController;
    @FXML private ActionBar abComponent;
    @FXML private ComponentChangerElement icComponentChangerController;
    @FXML private VersionChangerElement icVersionChangerController;

    private InstanceData currentInstance;

    @Override
    public void init(UiController parent, Function<Boolean, Boolean> lockSetter, Supplier<Boolean> lockGetter) {
        super.init(parent, lockSetter, lockGetter);
        icDetailsController.init(this::onComponentSelected);
        icVersionChangerController.init(files, files.getLauncherDetails().getTypeConversion(), LauncherApplication.config.BASE_DIR + files.getLauncherDetails().getLibrariesDir(), files.getVersionManifest(), this::onVersionChange, this::onVersionChangeFailed);

        cbSort.getItems().clear();
        cbSort.getItems().addAll(Settings.InstanceDataSortType.values());
        cbSort.getSelectionModel().select(LauncherApplication.settings.getInstanceSortType());

        if(LauncherApplication.settings.isInstanceSortReverse()) {
            btSort.getStyleClass().add("reverse");
        }
    }

    @Override
    protected void onCreate() {}

    @Override
    protected List<Pair<InstanceSelectorEntryElement, AnchorPane>> getElements() {
        ArrayList<Pair<InstanceSelectorEntryElement, AnchorPane>> instances = new ArrayList<>();
        for(Pair<LauncherManifest, LauncherInstanceDetails> instance : files.getInstanceComponents()) {
            try {
                instances.add(InstanceSelectorEntryElement.from(InstanceData.of(instance, files)));
            } catch (IOException | FileLoadException e) {
                LauncherApplication.displaySevereError(e);
            }
        }
        return instances.stream().peek(instance -> {
                instance.getKey().setSelectionInstanceAcceptor(this::allowSelection);
                instance.getKey().setSelectionInstanceListeners(List.of(this::onSelected));
            })
            .sorted((e1, e2) -> {
                int result = cbSort.getSelectionModel().getSelectedItem().getComparator().compare(e1.getKey().getInstanceData(), e2.getKey().getInstanceData());
                return LauncherApplication.settings.isInstanceSortReverse() ? -result : result;
            })
            .toList();
    }

    @Override
    protected void reloadComponents() {
        super.reloadComponents();
        icDetailsController.setVisible(false);
        icComponentChangerController.setVisible(false);
    }

    @FXML
    private void onSort() {
        if(LauncherApplication.settings.isInstanceSortReverse()) {
            LauncherApplication.settings.setInstanceSortReverse(false);
            btSort.getStyleClass().remove("reverse");
        } else {
            LauncherApplication.settings.setInstanceSortReverse(true);
            btSort.getStyleClass().add("reverse");
        }
        reloadComponents();
     }

    private void onSelected(InstanceData instanceData, boolean selected) {
        icDetailsController.clearSelection();
        icComponentChangerController.setVisible(false);
        icVersionChangerController.setVisible(false);
        abComponent.setDisable(true);
        abComponent.clearLabel();
        if(selected) {
            for(Pair<InstanceSelectorEntryElement, AnchorPane> instance : elements) {
                if(instance.getKey().getInstanceData() != instanceData) {
                    instance.getKey().select(false, true, false);
                }
            }
            abMain.setDisable(false);
            abMain.setLabel(instanceData.getInstance().getKey().getName());
            currentInstance = instanceData;
            icDetailsController.populate(instanceData);
            icDetailsController.setVisible(true);
        } else {
            abMain.setDisable(true);
            abMain.clearLabel();
            currentInstance = null;
            icDetailsController.setVisible(false);
        }
    }

    private boolean allowSelection(InstanceData instanceData, boolean selected) {
        return !getLock();
    }

    public void onPlay() {
        if(currentInstance != null) {
            setLock(true);
            abMain.setDisable(true);
            GameLauncher launcher = new GameLauncher(currentInstance, files, LauncherApplication.userAuth.getMinecraftUser(), List.of(this::onGameExit));
            displayGamePreparing();
            try {
                launcher.launch(false, this::onGameLaunchDone);
            } catch (GameLaunchException e) {
                onGameExit(null);
                displayGameLaunchFailed(e);
            }
        }
    }

    private void onGameLaunchDone(Exception e) {
        if(e == null) {
            displayGameRunning();
        } else {
            onGameExit(null);
            displayGameLaunchFailed(e);
        }
    }

    private void displayGamePreparing() {
        Platform.runLater(() -> {
            icPopupController.setContent("selector.instance.launch.preparing.title", "selector.instance.launch.preparing.message");
            icPopupController.clearControls();
            icPopupController.setVisible(true);
        });
    }

    private void onGameExit(String error) {
        if(error != null) {
            Platform.runLater(() -> displayGameCrash(error));
        }
        reloadComponents();
        setLock(false);
        Platform.runLater(() -> {
            icPopupController.setVisible(false);
        });
        Platform.runLater(() -> abMain.setDisable(false));
    }

    private void onComponentSelected(boolean selected, InstanceManagerElement.SelectedType type) {
        icComponentChangerController.setVisible(false);
        icVersionChangerController.setVisible(false);
        abComponent.setDisable(true);
        abComponent.clearLabel();
        if(selected && type != null) {
            List<LauncherManifest> manifests;
            LauncherManifest currentManifest;
            String label;
            switch(type) {
                case VERSION -> {
                    icVersionChangerController.setCurrentVersion(currentInstance.getVersionComponents().get(0).getValue());
                    icVersionChangerController.setVisible(true);
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
            icComponentChangerController.init(manifests, currentManifest, this::onComponentChanged, this::allowChange);
            abComponent.setLabel(label);
            abComponent.setDisable(false);
            icComponentChangerController.setVisible(true);
        }
    }

    private boolean allowChange() {
        return true;
    }

    private void onComponentChanged(LauncherManifest manifest) {
        switch(icDetailsController.getCurrentSelected()) {
            case SAVES -> currentInstance.getInstance().getValue().setSavesComponent(manifest.getId());
            case RESOURCEPACKS -> currentInstance.getInstance().getValue().setResourcepacksComponent(manifest.getId());
            case OPTIONS -> currentInstance.getInstance().getValue().setOptionsComponent(manifest.getId());
            case MODS -> currentInstance.getInstance().getValue().setModsComponent(manifest.getId());
        }
        try {
            currentInstance.getInstance().getValue().writeToFile(currentInstance.getInstance().getKey().getDirectory() + currentInstance.getInstance().getKey().getDetails());
        } catch (IOException e) {
            LauncherApplication.displayError(e);
        }
        try {
            files.reloadAll();
            currentInstance = InstanceData.of(currentInstance.getInstance(), files);
        } catch (FileLoadException e) {
            LauncherApplication.displaySevereError(e);
        }
        if(currentInstance != null) {
            icDetailsController.populate(currentInstance);
        }
    }

    private void onVersionChange(VersionCreator creator) {
        icPopupController.clearControls();
        icPopupController.setType(PopupElement.PopupType.WARNING);
        icPopupController.setContent("selector.instance.version.popup.change.title", "selector.instance.version.popup.change.message");
        icPopupController.addButtons(
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
        icPopupController.setVisible(true);
    }

    private void onVersionChangeFailed(Exception e) {
        LauncherApplication.displayError(e);
    }

    private void onPopupChangeConfirm(String id, VersionCreator creator) {
        icPopupController.setVisible(false);
        icPopupController.setContent("selector.instance.version.popup.changing", "");
        icPopupController.setType(PopupElement.PopupType.NONE);
        icPopupController.clearControls();
        icPopupController.addButtons(
                new PopupElement.PopupButton(
                        PopupElement.ButtonType.POSITIVE,
                        "selector.instance.version.popup.back",
                        "back",
                        this::onPopupBackClicked
                )
        );
        icPopupController.setControlsDisabled(true);
        icPopupController.setVisible(true);
        icVersionChangerController.setVisible(false);
        new Thread(() -> {
            String versionId;
            try {
                versionId = creator.getId();
            } catch (ComponentCreationException e) {
                Platform.runLater(() -> {
                    icPopupController.setType(PopupElement.PopupType.ERROR);
                    icPopupController.setContent("selector.instance.version.popup.failure", "");
                    icPopupController.setControlsDisabled(false);
                });
                return;
            }
            currentInstance.getInstance().getValue().setVersionComponent(versionId);
            try {
                currentInstance.getInstance().getValue().writeToFile(currentInstance.getInstance().getKey().getDirectory() + currentInstance.getInstance().getKey().getDetails());
            } catch (IOException e) {
                LauncherApplication.displayError(e);
            }
            try {
                files.reloadAll();
                currentInstance = InstanceData.of(currentInstance.getInstance(), files);
            } catch (FileLoadException e) {
                LauncherApplication.displaySevereError(e);
            }
            Platform.runLater(() -> {
                icDetailsController.populate(currentInstance);
                icVersionChangerController.setCurrentVersion(currentInstance.getVersionComponents().get(0).getValue());
                icVersionChangerController.setVisible(true);
                icPopupController.setType(PopupElement.PopupType.SUCCESS);
                icPopupController.setContent("selector.instance.version.popup.success", "");
                icPopupController.setControlsDisabled(false);
            });
        }).start();
    }

    private void onPopupBackClicked(String id) {
        icPopupController.setVisible(false);
        setVisible(false);
        setVisible(true);
    }

    private void onPopupChangeCancel(String id) {
        icPopupController.setVisible(false);
    }

    @FXML
    private void onComponentFolder() {
        switch (icDetailsController.getCurrentSelected()) {
            case SAVES -> openFolder(currentInstance.getSavesComponent().getDirectory());
            case RESOURCEPACKS -> openFolder(currentInstance.getResourcepacksComponent().getDirectory());
            case OPTIONS -> openFolder(currentInstance.getOptionsComponent().getDirectory());
            case MODS -> openFolder(currentInstance.getModsComponent().getKey().getDirectory());
        }
    }

    @FXML
    protected void onFolder() {
        openFolder(currentInstance.getInstance().getKey().getDirectory());
    }

    @Override
    protected boolean editValid(String newName) {
        return newName != null && !newName.isEmpty() && !newName.equals(currentInstance.getInstance().getKey().getName());
    }

    @Override
    protected void editCurrent(String newName) {
        if(currentInstance == null) {
            LOGGER.warn("Current instance is null");
            return;
        }
        currentInstance.getInstance().getKey().setName(newName);
        try {
            currentInstance.getInstance().getKey().writeToFile(FormatUtil.absoluteFilePath(currentInstance.getInstance().getKey().getDirectory(), LauncherApplication.config.MANIFEST_FILE_NAME));
        } catch (IOException e) {
            LauncherApplication.displayError(e);
        }
        setVisible(false);
        setVisible(true);
    }

    @Override
    protected void deleteCurrent() {
        try {
            currentInstance.delete(files);
        } catch (IOException e) {
            LauncherApplication.displayError(e);
        }
        setVisible(false);
        reloadComponents();
        setVisible(true);
    }

    @Override
    protected String getCurrentUsedBy() {
        return null;
    }

    private void openFolder(String path) {
        try {
            Desktop.getDesktop().open(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayGameLaunchFailed(Exception e) {
        LOGGER.error("Failed to launch game", e);
        Platform.runLater(() -> {
            icPopupController.setType(PopupElement.PopupType.ERROR);
            icPopupController.setTitle("selector.instance.error.launch.title");
            icPopupController.setMessage("selector.instance.error.launch.message", e.getMessage());
            icPopupController.setControlsDisabled(false);
            icPopupController.clearControls();
            icPopupController.addButtons(
                    new PopupElement.PopupButton(
                            PopupElement.ButtonType.POSITIVE,
                            "error.close",
                            "close",
                            id -> icPopupController.setVisible(false)
                    )
            );
            icPopupController.setVisible(true);
        });
    }

    private void displayGameRunning() {
        Platform.runLater(() -> {
            icPopupController.setType(PopupElement.PopupType.NONE);
            icPopupController.setContent("selector.instance.game.running.title", "selector.instance.game.running.message");
            icPopupController.clearControls();
            icPopupController.setVisible(true);
        });
    }

    private void displayGameCrash(String error) {
        icPopupController.setType(PopupElement.PopupType.WARNING);
        icPopupController.setTitle("selector.instance.game.crash.title");
        icPopupController.setMessage("selector.instance.game.crash.message", error.isBlank() ? "unknown error" : error);
        icPopupController.setControlsDisabled(false);
        icPopupController.clearControls();
        icPopupController.addButtons(
                new PopupElement.PopupButton(
                        PopupElement.ButtonType.POSITIVE,
                        "selector.instance.game.crash.close",
                        "close",
                        id -> icPopupController.setVisible(false)
                ),
                new PopupElement.PopupButton(
                        PopupElement.ButtonType.POSITIVE,
                        "selector.instance.game.crash.reports",
                        "reports",
                        id -> openFolder(FormatUtil.absoluteDirPath(currentInstance.getInstance().getKey().getDirectory(), LauncherApplication.config.INCLUDED_FILES_DIR, "crash-reports"))
                )
        );
        icPopupController.setVisible(false);
        icPopupController.setVisible(true);
    }

    @Override
    public void beforeShow(Stage stage) {
        super.beforeShow(stage);
        icDetailsController.setVisible(false);
        abComponent.clearLabel();
        abComponent.setDisable(true);
        icComponentChangerController.setVisible(false);
        icVersionChangerController.setVisible(false);

        cbSort.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            reloadComponents();
            LauncherApplication.settings.setInstanceSortType(newValue);
        });
    }
}
