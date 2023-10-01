package net.treset.minecraftlauncher.ui.selector;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.treset.mc_version_loader.launcher.LauncherInstanceDetails;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.sync.ManifestSynchronizer;
import net.treset.minecraftlauncher.ui.generic.ButtonElement;
import net.treset.minecraftlauncher.ui.generic.lists.FolderContentContainer;
import net.treset.minecraftlauncher.ui.generic.lists.SelectorEntryElement;
import net.treset.minecraftlauncher.ui.manager.ComponentManagerElement;
import net.treset.minecraftlauncher.util.FormatUtil;
import net.treset.minecraftlauncher.util.SyncUtil;
import net.treset.minecraftlauncher.util.UiUtil;
import net.treset.minecraftlauncher.util.ui.FileSyncExecutor;
import net.treset.minecraftlauncher.util.ui.FileSyncHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class ManifestSelectorElement extends SelectorElement<SelectorEntryElement<ManifestSelectorElement.ManifestContentProvider>> {
    public static class ManifestContentProvider implements SelectorEntryElement.ContentProvider {
        private LauncherManifest manifest;
        public ManifestContentProvider(LauncherManifest manifest) {
            this.manifest = manifest;
        }

        public LauncherManifest getManifest() {
            return manifest;
        }

        public void setManifest(LauncherManifest manifest) {
            this.manifest = manifest;
        }

        @Override
        public String getTitle() {
            return manifest.getName();
        }

        @Override
        public String getDetails() {
            return manifest.getId();
        }
    }

    private static final Logger LOGGER = LogManager.getLogger(ManifestSelectorElement.class);

    @FXML protected FolderContentContainer ccDetails;
    @FXML protected ComponentManagerElement maComponent;
    @FXML protected ButtonElement btSettings;
    @FXML protected ComponentManagerElement maComponentSecondary;


    protected ManifestContentProvider currentProvider = null;

    @FXML
    private void onSettings() {
        if(maComponentSecondary != null) {
            maComponentSecondary.setVisible(true);
        }
    }

    @FXML
    private void onSettingsBack() {
        if(maComponentSecondary != null) {
            maComponentSecondary.setVisible(false);
        }
    }

    @Override
    protected void onDownload(ActionEvent event) {
        new FileSyncHelper(getBaseManifest(), getComponents())
                .showDownloadPopup((manifest) -> new FileSyncExecutor(
                        new ManifestSynchronizer(
                                manifest,
                                files,
                                (s) -> {
                                }
                        )
                ).download(() -> {
                    LauncherApplication.setPopup(null);
                    reloadComponents();
                }));
    }

    protected abstract LauncherManifest getBaseManifest();

    @Override
    protected String getCurrentUsedBy() {
        List<Pair<LauncherManifest, LauncherInstanceDetails>> usedBy = getInstances();
        return usedBy.isEmpty() ? null : usedBy.get(0).getKey().getName();
    }

    protected List<Pair<LauncherManifest, LauncherInstanceDetails>> getInstances() {
        List<Pair<LauncherManifest, LauncherInstanceDetails>> usedBy = new ArrayList<>();
        for(Pair<LauncherManifest, LauncherInstanceDetails> i : files.getInstanceComponents()) {
            if (getManifest().getId().equals(getManifestId(i.getValue()))) {
                usedBy.add(i);
            }
        }
        return usedBy;
    }

    protected abstract String getManifestId(LauncherInstanceDetails instanceDetails);

    @Override
    protected List<SelectorEntryElement<ManifestSelectorElement.ManifestContentProvider>> getElements() {
        ArrayList<SelectorEntryElement<ManifestSelectorElement.ManifestContentProvider>> elements = new ArrayList<>();
        for(LauncherManifest manifest: getComponents()) {
            elements.add(new SelectorEntryElement<>(new ManifestContentProvider(manifest), this::onSelected, () -> SyncUtil.isSyncing(manifest)));
        }
        return elements.stream()
            .sorted(Comparator.comparing(a -> a.getContentProvider().getTitle()))
            .toList();
    }

    protected abstract List<LauncherManifest> getComponents();

    protected void onSelected(ManifestContentProvider contentProvider, boolean selected) {
        if(LauncherApplication.settings.getSyncPort() != null && LauncherApplication.settings.getSyncUrl() != null && LauncherApplication.settings.getSyncKey() != null && !SyncUtil.isSyncing(contentProvider.getManifest())) {
            abMain.setShowSync(true);
            abMain.setOnSync((e) -> new FileSyncExecutor(new ManifestSynchronizer(
                    contentProvider.getManifest(),
                    files,
                    (s) -> {}
            )).upload(this::reloadComponents));
        } else {
            // TODO: change
            abMain.setShowSync(true);
            abMain.setOnSync((e) -> new FileSyncExecutor(new ManifestSynchronizer(
                    contentProvider.getManifest(),
                    files,
                    (s) -> {}
            )).upload(this::reloadComponents));
        }

        if(ccDetails != null) {
            ccDetails.setVisible(selected);
        }
        if(maComponent != null) {
            maComponent.setVisible(selected);
        }
        if(btSettings != null) {
            btSettings.setVisible(selected);
        }
        if(maComponentSecondary != null) {
            maComponentSecondary.setVisible(false);
        }
        if(selected) {
            deselectAll();
            currentProvider = contentProvider;
            createSelected = false;
            csCreate.getStyleClass().remove("selected");
            vbCreate.setVisible(false);
            abMain.setDisable(false);
            abMain.setLabel(contentProvider.getManifest().getName());
            if(ccDetails != null) {
                ccDetails.setFolder(new File(contentProvider.getManifest().getDirectory()));
            }
            if(maComponent != null) {
                maComponent.init(contentProvider.getManifest());
                maComponent.setVisible(true);
            }
            if(btSettings != null) {
                btSettings.setVisible(true);
            }
            if(maComponentSecondary != null) {
                maComponentSecondary.init(contentProvider.getManifest());
            }
        } else {
            currentProvider = null;
            abMain.setDisable(true);
            abMain.clearLabel();
        }
    }

    public LauncherManifest getManifest() {
        return currentProvider.getManifest();
    }

    @Override
    protected void onSelectCreate() {
        super.onSelectCreate();
        if(ccDetails != null) {
            ccDetails.setVisible(false);
        }
        if(maComponent != null) {
            maComponent.setVisible(false);
        }
    }

    @Override
    public void beforeShow(Stage stage) {
        super.beforeShow(stage);
        if(ccDetails != null) {
            ccDetails.setVisible(false);
        }
    }

    @Override
    protected void onFolder() {
        if(currentProvider == null) {
            LOGGER.warn("No element selected");
        }
        UiUtil.openFolder(currentProvider.getManifest().getDirectory());
    }

    @Override
    protected boolean editValid(String newName) {
        return newName != null && !newName.isBlank() && !newName.equals(currentProvider.getManifest().getName());
    }

    @Override
    protected void editCurrent(String newName) {
        if(currentProvider == null) {
            LOGGER.warn("No element selected");
            return;
        }
        getManifest().setName(newName);
        try {
            getManifest().writeToFile(FormatUtil.absoluteFilePath(currentProvider.getManifest().getDirectory(), LauncherApplication.config.MANIFEST_FILE_NAME));
        } catch (IOException e) {
            LauncherApplication.displayError(e);
        }
        setVisible(false);
        setVisible(true);
    }

    @Override
    protected void reloadComponents() {
        super.reloadComponents();
        if(ccDetails != null) {
            ccDetails.setVisible(false);
        }
    }
}
