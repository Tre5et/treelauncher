package net.treset.minecraftlauncher.ui.selector;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.treset.mc_version_loader.launcher.LauncherInstanceDetails;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.data.InstanceData;
import net.treset.minecraftlauncher.launching.GameLauncher;
import net.treset.minecraftlauncher.ui.base.UiController;
import net.treset.minecraftlauncher.ui.create.SavesCreatorElement;
import net.treset.minecraftlauncher.ui.generic.PopupElement;
import net.treset.minecraftlauncher.ui.generic.lists.ContentElement;
import net.treset.minecraftlauncher.util.FileUtil;
import net.treset.minecraftlauncher.util.FormatUtil;
import net.treset.minecraftlauncher.util.QuickPlayData;
import net.treset.minecraftlauncher.util.exception.ComponentCreationException;
import net.treset.minecraftlauncher.util.exception.FileLoadException;
import net.treset.minecraftlauncher.util.exception.GameLaunchException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class SavesSelectorElement extends ManifestSelectorElement {
    private static final Logger LOGGER = LogManager.getLogger(SavesSelectorElement.class);

    @FXML private SavesCreatorElement icCreatorController;

    @FXML
    private void onSelectWorld(MouseEvent event) {
        abMain.setShowPlay(((ContentElement)event.getSource()).isSelected());
    }

    @Override
    public void init(UiController parent, Function<Boolean, Boolean> lockSetter, Supplier<Boolean> lockGetter) {
        super.init(parent, lockSetter, lockGetter);
        abMain.setShowPlay(false);
        icCreatorController.enableUse(false);
        icCreatorController.init(this, lockSetter, lockGetter);
        icCreatorController.setPrerequisites(files.getSavesComponents(), files.getLauncherDetails().getTypeConversion(), files.getSavesManifest(), files.getGameDetailsManifest());
    }


    @Override
    public void beforeShow(Stage stage) {
        super.beforeShow(stage);
        icCreatorController.beforeShow(stage);
    }

    @Override
    public void afterShow(Stage stage) {
        super.afterShow(stage);
        icCreatorController.afterShow(stage);
    }

    @Override
    protected void onCreate() {
        if(icCreatorController.checkCreateReady()) {
            try {
                icCreatorController.getCreator().getId();
            } catch (ComponentCreationException e) {
                LauncherApplication.displayError(e);
            }
            reloadComponents();
        } else {
            icCreatorController.showError(true);
        }
    }

    @FXML
    private void onPlay() {
        //TODO: Show servers and realms

        List<Pair<LauncherManifest, LauncherInstanceDetails>> instances = getInstances();
        ContentElement selected = ccDetails.getSelected();
        if(selected == null || instances.isEmpty()) {
            return;
        }

        //TODO: Handle multiple instances

        InstanceData data;
        try {
            data = InstanceData.of(instances.get(0), files);
        } catch (FileLoadException e) {
            LauncherApplication.displayError(e);
            return;
        }

        setLock(true);
        GameLauncher launcher = new GameLauncher(data, files, LauncherApplication.userAuth.getMinecraftUser(), new QuickPlayData(QuickPlayData.Type.WORLD, selected.getDetails()), List.of(this::onGameExit));
        displayGamePreparing();
        try {
            launcher.launch(false, this::onGameLaunchDone);
        } catch (GameLaunchException e) {
            onGameExit(null);
            displayGameLaunchFailed(e);
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
        LauncherApplication.setPopup(
                new PopupElement("selector.instance.launch.preparing.title", "selector.instance.launch.preparing.message")
        );
    }

    private void onGameExit(String error) {
        if(error != null) {
            Platform.runLater(() -> displayGameCrash(error));
        }
        reloadComponents();
        setLock(false);
        LauncherApplication.setPopup(null);
        Platform.runLater(() -> abMain.setDisable(false));
    }

    private void displayGameLaunchFailed(Exception e) {
        LOGGER.error("Failed to launch game", e);
        LauncherApplication.setPopup(
                new PopupElement(
                        PopupElement.PopupType.ERROR,
                        "selector.instance.error.launch.title",
                        "selector.instance.error.launch.message",
                        List.of(
                                new PopupElement.PopupButton(
                                        PopupElement.ButtonType.POSITIVE,
                                        "error.close",
                                        event -> LauncherApplication.setPopup(null)
                                )
                        )
                )
        );
    }

    private void displayGameRunning() {
        LauncherApplication.setPopup(
                new PopupElement(
                        "selector.instance.game.running.title",
                        "selector.instance.game.running.message"
                )
        );
    }

    private void displayGameCrash(String error) {
        LauncherApplication.setPopup(
                new PopupElement(
                        PopupElement.PopupType.WARNING,
                        "selector.instance.game.crash.title",
                        LauncherApplication.stringLocalizer.getFormatted("selector.instance.game.crash.message", error.isBlank() ? "unknown error" : error),
                        List.of(
                                new PopupElement.PopupButton(
                                        PopupElement.ButtonType.POSITIVE,
                                        "selector.instance.game.crash.close",
                                        event -> LauncherApplication.setPopup(null)
                                ),
                                new PopupElement.PopupButton(
                                        PopupElement.ButtonType.POSITIVE,
                                        "selector.instance.game.crash.reports",
                                        event -> openFolder(FormatUtil.absoluteDirPath(getInstances().get(0).getKey().getDirectory(), LauncherApplication.config.INCLUDED_FILES_DIR, "crash-reports"))
                                )
                        )
                )
        );
    }

    private void openFolder(String path) {
        try {
            Desktop.getDesktop().open(new File(path));
        } catch (IOException e) {
            LauncherApplication.displayError(e);
        }
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
            icCreatorController.setPrerequisites(files.getSavesComponents(), files.getLauncherDetails().getTypeConversion(), files.getSavesManifest(), files.getGameDetailsManifest());
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
    protected String getManifestId(LauncherInstanceDetails instanceDetails) {
        return instanceDetails.getSavesComponent();
    }

    @Override
    protected List<LauncherManifest> getComponents() {
        return files.getSavesComponents();
    }
}
