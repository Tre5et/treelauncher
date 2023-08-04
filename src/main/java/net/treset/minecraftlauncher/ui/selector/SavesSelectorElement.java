package net.treset.minecraftlauncher.ui.selector;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import net.treset.mc_version_loader.launcher.LauncherInstanceDetails;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.ui.base.UiController;
import net.treset.minecraftlauncher.ui.create.SavesCreatorElement;
import net.treset.minecraftlauncher.util.FileUtil;
import net.treset.minecraftlauncher.util.exception.ComponentCreationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class SavesSelectorElement extends ManifestSelectorElement {
    private static final Logger LOGGER = LogManager.getLogger(SavesSelectorElement.class);

    @FXML private SavesCreatorElement icCreatorController;

    @Override
    public void init(UiController parent, Function<Boolean, Boolean> lockSetter, Supplier<Boolean> lockGetter) {
        super.init(parent, lockSetter, lockGetter);
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
