package net.treset.minecraftlauncher.ui.selector;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.util.Pair;
import net.treset.mc_version_loader.launcher.LauncherInstanceDetails;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.data.InstanceData;
import net.treset.minecraftlauncher.launching.GameLauncher;
import net.treset.minecraftlauncher.ui.base.UiController;
import net.treset.minecraftlauncher.ui.create.SavesCreatorElement;
import net.treset.minecraftlauncher.ui.generic.PopupElement;
import net.treset.minecraftlauncher.ui.generic.lists.ContentElement;
import net.treset.minecraftlauncher.ui.generic.lists.SavesContentContainer;
import net.treset.minecraftlauncher.util.FileUtil;
import net.treset.minecraftlauncher.util.FormatUtil;
import net.treset.minecraftlauncher.util.QuickPlayData;
import net.treset.minecraftlauncher.util.exception.FileLoadException;
import net.treset.minecraftlauncher.util.ui.GameLauncherHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class SavesSelectorElement extends ManifestSelectorElement {
    private static final Logger LOGGER = LogManager.getLogger(SavesSelectorElement.class);

    @FXML
    private void onSelect(MouseEvent event) {
        abMain.setShowPlay(((ContentElement)event.getSource()).isSelected());
    }

    @Override
    public void init(UiController parent, Function<Boolean, Boolean> lockSetter, Supplier<Boolean> lockGetter) {
        super.init(parent, lockSetter, lockGetter);
        abMain.setShowPlay(false);
    }

    @Override
    protected void onSelected(ManifestContentProvider contentProvider, boolean selected) {
        super.onSelected(contentProvider, selected);
        abMain.setShowPlay(false);
        File serversDatFile = new File(FormatUtil.absoluteFilePath(contentProvider.getManifest().getDirectory(), ".included_files", "servers.dat"));
        if(serversDatFile.exists()) {
            ((SavesContentContainer)ccDetails).setServersFile(serversDatFile);
        } else {
            ((SavesContentContainer)ccDetails).setServersFile(null);
        }
    }

    @Override
    protected void reloadComponents() {
        super.reloadComponents();
        ((SavesCreatorElement) crCreator).init(files.getSavesComponents(), files.getLauncherDetails().getTypeConversion(), files.getSavesManifest(), files.getGameDetailsManifest());
        Platform.runLater(() -> abMain.setShowPlay(false));
    }

    @FXML
    private void onPlay() {
        //TODO: Show realms

        QuickPlayData quickPlayData = ((SavesContentContainer)ccDetails).getQuickPlayData();
        if(quickPlayData == null) {
            return;
        }

        List<Pair<LauncherManifest, LauncherInstanceDetails>> instances = getInstances();
        if(instances.isEmpty()) {
            showPlayNoInstance();
            return;
        }
        if(instances.size() > 1) {
            showPlayMultipleInstances(instances, quickPlayData);
            return;
        }

        initLaunch(instances.get(0), quickPlayData);
    }

    private static class InstancePair {
        private final Pair<LauncherManifest, LauncherInstanceDetails> instance;

        public InstancePair(Pair<LauncherManifest, LauncherInstanceDetails> instance) {
            this.instance = instance;
        }

        public Pair<LauncherManifest, LauncherInstanceDetails> getInstance() {
            return instance;
        }

        @Override
        public String toString() {
            return instance.getKey().getName();
        }
    }

    private void initLaunch(Pair<LauncherManifest, LauncherInstanceDetails> instance, QuickPlayData quickPlayData) {
        InstanceData data;
        try {
            data = InstanceData.of(instance, files);
        } catch (FileLoadException e) {
            LauncherApplication.displayError(e);
            return;
        }

        setLock(true);
        abMain.setDisable(true);
        GameLauncher launcher = new GameLauncher(data, files, LauncherApplication.userAuth.getMinecraftUser(), quickPlayData);
        GameLauncherHelper gameLauncherHelper = new GameLauncherHelper(launcher, this::onGameExit, getLockSetter());
        gameLauncherHelper.start();
    }

    private void showPlayMultipleInstances(List<Pair<LauncherManifest, LauncherInstanceDetails>> instances, QuickPlayData quickPlayData) {
        PopupElement.PopupComboBox<InstancePair> comboBox = new PopupElement.PopupComboBox<>(instances.stream().map(InstancePair::new).toList(), 0);
        LauncherApplication.setPopup(
                new PopupElement(
                        PopupElement.PopupType.NONE,
                        "selector.saves.play.multipleinstances.title",
                        "selector.saves.play.multipleinstances.message",
                        List.of(
                                comboBox
                        ),
                        List.of(
                                new PopupElement.PopupButton(
                                        PopupElement.ButtonType.NEUTRAL,
                                        "selector.saves.play.multipleinstances.cancel",
                                        (e) -> LauncherApplication.setPopup(null)
                                ),
                                new PopupElement.PopupButton(
                                        PopupElement.ButtonType.POSITIVE,
                                        "selector.saves.play.multipleinstances.play",
                                        (e) -> initLaunch(comboBox.getSelected().getInstance(), quickPlayData)
                                )
                        )
                )
        );
    }

    private void showPlayNoInstance() {
        LauncherApplication.setPopup(
                new PopupElement(
                        PopupElement.PopupType.ERROR,
                        "selector.saves.play.noinstance.title",
                        "selector.saves.play.noinstance.message",
                        List.of(
                                new PopupElement.PopupButton(
                                        PopupElement.ButtonType.POSITIVE,
                                        "selector.saves.play.noinstance.close",
                                        (e) -> LauncherApplication.setPopup(null)
                                )
                        )

                )
        );
    }

    private void onGameExit(String error) {
        reloadComponents();
    }

    @Override
    protected void deleteCurrent() {
        if(currentProvider != null) {
            if(!files.getSavesManifest().getComponents().remove(getManifest().getId())) {
                LOGGER.warn("Unable to remove save from manifest");
                return;
            }
            try {
                files.getSavesManifest().writeToFile(files.getSavesManifest().getDirectory() + files.getGameDetailsManifest().getComponents().get(1));
            } catch (IOException e) {
                LauncherApplication.displayError(e);
                return;
            }
            try {
                FileUtil.deleteDir(new File(getManifest().getDirectory()));
            } catch (IOException e) {
                LauncherApplication.displayError(e);
            }
            LOGGER.debug("Save deleted");
            setVisible(false);
            setVisible(true);
        }
    }

    @Override
    protected LauncherManifestType getBaseManifestType() {
        return LauncherManifestType.SAVES_COMPONENT;
    }

    @Override
    protected LauncherManifest getBaseManifest() {
        return files.getSavesManifest();
    }

    @Override
    protected String getManifestId(LauncherInstanceDetails instanceDetails) {
        return instanceDetails.getSavesComponent();
    }

    @Override
    protected List<LauncherManifest> getComponents() {
        return files.getSavesComponents();
    }
}
