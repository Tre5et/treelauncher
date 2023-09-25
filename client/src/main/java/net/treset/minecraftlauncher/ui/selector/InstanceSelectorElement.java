package net.treset.minecraftlauncher.ui.selector;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.treset.mc_version_loader.launcher.LauncherInstanceDetails;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.config.Settings;
import net.treset.minecraftlauncher.creation.VersionCreator;
import net.treset.minecraftlauncher.data.InstanceData;
import net.treset.minecraftlauncher.launching.GameLauncher;
import net.treset.minecraftlauncher.sync.InstanceSynchronizer;
import net.treset.minecraftlauncher.ui.base.UiController;
import net.treset.minecraftlauncher.ui.generic.*;
import net.treset.minecraftlauncher.ui.generic.lists.InstanceSelectorEntryElement;
import net.treset.minecraftlauncher.ui.manager.InstanceManagerElement;
import net.treset.minecraftlauncher.ui.manager.InstanceSettingsElement;
import net.treset.minecraftlauncher.util.CreationStatus;
import net.treset.minecraftlauncher.util.FormatUtil;
import net.treset.minecraftlauncher.util.SyncUtil;
import net.treset.minecraftlauncher.util.UiUtil;
import net.treset.minecraftlauncher.util.exception.ComponentCreationException;
import net.treset.minecraftlauncher.util.exception.FileLoadException;
import net.treset.minecraftlauncher.util.ui.FileSyncExecutor;
import net.treset.minecraftlauncher.util.ui.GameLauncherHelper;
import net.treset.minecraftlauncher.util.ui.sort.FileSyncHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class InstanceSelectorElement extends SelectorElement<InstanceSelectorEntryElement> {
    private static final Logger LOGGER = LogManager.getLogger(InstanceSelectorElement.class);

    @FXML private ButtonBox<Settings.InstanceDataSortType> cbSort;
    @FXML private InstanceManagerElement icDetailsController;
    @FXML private ActionBar abComponent;
    @FXML private ComponentChangerElement ccChanger;
    @FXML private VersionChangerElement vcChanger;
    @FXML private InstanceSettingsElement icInstanceSettingsController;

    private InstanceData currentInstance;

    @Override
    public void init(UiController parent, Function<Boolean, Boolean> lockSetter, Supplier<Boolean> lockGetter) {
        super.init(parent, lockSetter, lockGetter);
        icDetailsController.init(this::onComponentSelected);
        vcChanger.init(files, files.getLauncherDetails().getTypeConversion(), LauncherApplication.config.BASE_DIR + files.getLauncherDetails().getLibrariesDir(), files.getVersionManifest(), this::onVersionChange, this::onVersionChangeFailed);

        cbSort.setItems(Settings.InstanceDataSortType.values());
        cbSort.select(LauncherApplication.settings.getInstanceSortType());

        cbSort.setReverse(LauncherApplication.settings.isInstanceSortReverse());
    }

    @Override
    protected void onCreate() {}

    @Override
    protected List<InstanceSelectorEntryElement> getElements() {
        ArrayList<InstanceSelectorEntryElement> instances = new ArrayList<>();
        for(Pair<LauncherManifest, LauncherInstanceDetails> instance : files.getInstanceComponents()) {
            try {
                instances.add(new InstanceSelectorEntryElement(InstanceData.of(instance, files), this::onSelected));
            } catch (FileLoadException e) {
                LauncherApplication.displaySevereError(e);
            }
        }
        return instances.stream()
            .sorted((e1, e2) -> {
                int result = cbSort.getSelected().getComparator().compare(e1.getInstanceData(), e2.getInstanceData());
                return LauncherApplication.settings.isInstanceSortReverse() ? -result : result;
            })
            .toList();
    }

    @Override
    protected void reloadComponents() {
        super.reloadComponents();
        icDetailsController.setVisible(false);
        ccChanger.setVisible(false);
        vcChanger.setVisible(false);
        icInstanceSettingsController.setVisible(false);
        abComponent.setDisable(true);
    }

    @Override
    protected void onDownload(ActionEvent event) {
        new FileSyncHelper(files.getInstanceManifest(), files.getInstanceComponents().stream().map(Pair::getKey).toList())
                .showDownloadPopup((manifest) -> new FileSyncExecutor(
                        new InstanceSynchronizer(
                                new InstanceData(
                                        null, null,
                                        new Pair<>(manifest, new LauncherInstanceDetails(null, null, null, null, null, null, null, null)),
                                        null, null, null, null, null, null, null, null, null, null, null, null
                                ),
                                files,
                                (s) -> {
                                }
                        )
                ).download(() -> {
                    LauncherApplication.setPopup(null);
                    reloadComponents();
                }));
    }

    @FXML
    private void onSort() {
        cbSort.toggleReverse();
        LauncherApplication.settings.setInstanceSortReverse(cbSort.isReverse());
        reloadComponents();
    }


    private void onSelected(InstanceData instanceData, boolean selected) {
        if(LauncherApplication.settings.getSyncPort() != null && LauncherApplication.settings.getSyncUrl() != null && LauncherApplication.settings.getSyncKey() != null && !SyncUtil.isSyncing(instanceData.getInstance().getKey())) {
            abMain.setShowSync(true);
            abMain.setOnSync((e) -> new FileSyncExecutor(
                    new InstanceSynchronizer(
                            instanceData,
                            files,
                            (s) -> {}
                    )
            ).upload(this::reloadComponents));
        } else {
            abMain.setShowSync(true);
            abMain.setOnSync((e) -> new FileSyncExecutor(
                    new InstanceSynchronizer(
                            instanceData,
                            files,
                            (s) -> {}
                    )
            ).download(this::reloadComponents));
        }

        icDetailsController.clearSelection();
        ccChanger.setVisible(false);
        vcChanger.setVisible(false);
        icInstanceSettingsController.setVisible(false);
        abComponent.setDisable(true);
        abComponent.clearLabel();
        if(selected) {
            for(InstanceSelectorEntryElement instance : elements) {
                if(instance.getInstanceData() != instanceData) {
                    instance.select(false, true, false);
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
            icInstanceSettingsController.save();
            setLock(true);
            abMain.setDisable(true);
            GameLauncher launcher = new GameLauncher(currentInstance, files, LauncherApplication.userAuth.getMinecraftUser());
            GameLauncherHelper gameLauncherHelper = new GameLauncherHelper(launcher, this::onGameExit, getLockSetter());
            gameLauncherHelper.start();
        }
    }

    private void onGameExit(String error) {
        reloadComponents();
    }

    private void onComponentSelected(boolean selected, InstanceManagerElement.SelectedType type) {
        ccChanger.setVisible(false);
        vcChanger.setVisible(false);
        icInstanceSettingsController.setVisible(false);
        abComponent.setDisable(true);
        abComponent.clearLabel();
        if(selected && type != null) {
            List<LauncherManifest> manifests;
            LauncherManifest currentManifest;
            String label;
            switch(type) {
                case VERSION -> {
                    vcChanger.setCurrentVersion(currentInstance.getVersionComponents().get(0).getValue());
                    vcChanger.setVisible(true);
                    return;
                }
                case SETTINGS -> {
                    icInstanceSettingsController.init(currentInstance);
                    icInstanceSettingsController.setVisible(true);
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
            ccChanger.init(manifests, currentManifest, this::onComponentChanged, this::allowChange);
            abComponent.setLabel(label);
            abComponent.setDisable(false);
            ccChanger.setVisible(true);
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
        LauncherApplication.setPopup(
                new PopupElement(
                        PopupElement.PopupType.WARNING,
                        "selector.instance.version.popup.change.title",
                        "selector.instance.version.popup.change.message",
                        List.of(
                                new PopupElement.PopupButton(
                                        PopupElement.ButtonType.NEGATIVE,
                                        "selector.instance.version.popup.change.cancel",
                                        this::onPopupChangeCancel
                                ),
                                new PopupElement.PopupButton(
                                        PopupElement.ButtonType.POSITIVE,
                                        "selector.instance.version.popup.change.confirm",
                                        event -> this.onPopupChangeConfirm(event, creator)
                                )
                        )
                )
        );
    }

    private void onVersionChangeFailed(Exception e) {
        LauncherApplication.displayError(e);
    }

    private void onCreateStatusChanged(CreationStatus status, PopupElement popup) {
        StringBuilder message = new StringBuilder(status.getCurrentStep().getMessage());
        if(status.getDownloadStatus() != null) {
            message.append("\n").append(status.getDownloadStatus().getCurrentFile()).append("\n(").append(status.getDownloadStatus().getCurrentAmount()).append("/").append(status.getDownloadStatus().getTotalAmount()).append(")");
        }
        Platform.runLater(()-> {
            if(popup == null) return;
            popup.setMessage(message.toString());
        });
    }

    private void onPopupChangeConfirm(ActionEvent event, VersionCreator creator) {
        PopupElement popup = new PopupElement(
                PopupElement.PopupType.NONE,
                "selector.instance.version.popup.changing",
                null
        );

        LauncherApplication.setPopup(popup);

        vcChanger.setVisible(false);

        creator.setStatusCallback(status -> onCreateStatusChanged(status, popup));
        new Thread(() -> {
            String versionId;
            try {
                versionId = creator.getId();
            } catch (ComponentCreationException e) {
                LauncherApplication.setPopup(
                        new PopupElement(
                                PopupElement.PopupType.ERROR,
                                "selector.instance.version.popup.failure",
                                null,
                                List.of(
                                        new PopupElement.PopupButton(
                                                PopupElement.ButtonType.POSITIVE,
                                                "selector.instance.version.popup.back",
                                                this::onPopupBackClicked
                                        )
                                )
                        )
                );
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
                vcChanger.setCurrentVersion(currentInstance.getVersionComponents().get(0).getValue());
                vcChanger.setVisible(true);
                LauncherApplication.setPopup(
                        new PopupElement(
                                PopupElement.PopupType.SUCCESS,
                                "selector.instance.version.popup.success",
                                null,
                                List.of(
                                        new PopupElement.PopupButton(
                                                PopupElement.ButtonType.POSITIVE,
                                                "selector.instance.version.popup.back",
                                                this::onPopupBackClicked
                                        )
                                )
                        )
                );
            });
        }).start();
    }

    private void onPopupBackClicked(ActionEvent event) {
        LauncherApplication.setPopup(null);
        setVisible(false);
        setVisible(true);
    }

    private void onPopupChangeCancel(ActionEvent event) {
        LauncherApplication.setPopup(null);
    }

    @FXML
    private void onComponentFolder() {
        switch (icDetailsController.getCurrentSelected()) {
            case SAVES -> UiUtil.openFolder(currentInstance.getSavesComponent().getDirectory());
            case RESOURCEPACKS -> UiUtil.openFolder(currentInstance.getResourcepacksComponent().getDirectory());
            case OPTIONS -> UiUtil.openFolder(currentInstance.getOptionsComponent().getDirectory());
            case MODS -> UiUtil.openFolder(currentInstance.getModsComponent().getKey().getDirectory());
        }
    }

    @FXML
    protected void onFolder() {
        UiUtil.openFolder(currentInstance.getInstance().getKey().getDirectory());
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

    @Override
    public void beforeShow(Stage stage) {
        super.beforeShow(stage);
        icDetailsController.setVisible(false);
        abComponent.clearLabel();
        abComponent.setDisable(true);
        ccChanger.setVisible(false);
        vcChanger.setVisible(false);

        cbSort.setOnSelectionChanged((observable, oldValue, newValue) -> {
            reloadComponents();
            LauncherApplication.settings.setInstanceSortType(newValue);
        });
    }

    @Override
    public void setRootVisible(boolean visible) {
        super.setRootVisible(visible);
        if(!visible) {
            icInstanceSettingsController.save();
        }
    }
}
