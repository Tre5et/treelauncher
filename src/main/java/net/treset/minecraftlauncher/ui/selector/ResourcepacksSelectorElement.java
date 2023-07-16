package net.treset.minecraftlauncher.ui.selector;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.treset.mc_version_loader.launcher.LauncherInstanceDetails;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.ui.base.UiController;
import net.treset.minecraftlauncher.ui.create.ResourcepacksCreatorElement;
import net.treset.minecraftlauncher.ui.generic.SelectorEntryElement;
import net.treset.minecraftlauncher.util.FileUtil;
import net.treset.minecraftlauncher.util.exception.ComponentCreationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ResourcepacksSelectorElement extends ManifestSelectorElement {
    private static final Logger LOGGER = LogManager.getLogger(ResourcepacksSelectorElement.class);
    @FXML private ResourcepacksCreatorElement icCreatorController;

    @Override
    public void init(UiController parent, Function<Boolean, Boolean> lockSetter, Supplier<Boolean> lockGetter) {
        super.init(parent, lockSetter, lockGetter);
        icCreatorController.enableUse(false);
        icCreatorController.init(this, lockSetter, lockGetter);
        icCreatorController.setPrerequisites(files.getResourcepackComponents(), files.getLauncherDetails().getTypeConversion(), files.getResourcepackManifest());
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

    @FXML @Override
    protected void onCreate() {
        if(icCreatorController.checkCreateReady()) {
            try {
                icCreatorController.getCreator().getId();
            } catch (ComponentCreationException e) {
                LauncherApplication.displayError(e);
            }
            reloadComponents();
            for(Pair<SelectorEntryElement, AnchorPane> resourcepack: elements) {
                resourcepack.getKey().beforeShow(null);
            }
        } else {
            icCreatorController.showError(true);
        }
    }

    @Override
    protected void deleteCurrent() {
        if(currentManifest != null) {
            if(!files.getResourcepackManifest().getComponents().remove(currentManifest.getId())) {
                LOGGER.warn("Unable to remove resourcepacks from manifest");
                return;
            }
            try {
                files.getResourcepackManifest().writeToFile(files.getResourcepackManifest().getDirectory() + LauncherApplication.config.MANIFEST_FILE_NAME);
            } catch (IOException e) {
                LauncherApplication.displayError(e);
                return;
            }
            icCreatorController.setPrerequisites(files.getResourcepackComponents(), files.getLauncherDetails().getTypeConversion(), files.getResourcepackManifest());
            try {
                FileUtil.deleteDir(new File(currentManifest.getDirectory()));
            } catch (IOException e) {
                LauncherApplication.displayError(e);
                return;
            }
            LOGGER.debug("Resourcepacks deleted");
            setVisible(false);
            setVisible(true);
        }
    }

    @Override
    protected String getManifestId(LauncherInstanceDetails instanceDetails) {
        return instanceDetails.getResourcepacksComponent();
    }

    @Override
    protected List<LauncherManifest> getComponents() {
        return files.getResourcepackComponents();
    }
}
