package net.treset.minecraftlauncher.ui.selector;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.treset.mc_version_loader.launcher.LauncherInstanceDetails;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.ui.base.UiController;
import net.treset.minecraftlauncher.ui.create.OptionsCreatorElement;
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

public class OptionsSelectorElement extends ManifestSelectorElement {
    private static final Logger LOGGER = LogManager.getLogger(OptionsSelectorElement.class);

    @FXML private OptionsCreatorElement optionsCreatorController;

    @Override
    public void init(UiController parent, Function<Boolean, Boolean> lockSetter, Supplier<Boolean> lockGetter) {
        super.init(parent, lockSetter, lockGetter);
        optionsCreatorController.enableUse(false);
        optionsCreatorController.init(this, lockSetter, lockGetter);
        optionsCreatorController.setPrerequisites(files.getOptionsComponents(), files.getLauncherDetails().getTypeConversion(), files.getOptionsManifest());
    }

    @Override
    public void beforeShow(Stage stage) {
        super.beforeShow(stage);
        optionsCreatorController.beforeShow(stage);
    }

    @Override
    public void afterShow(Stage stage) {
        super.afterShow(stage);
        optionsCreatorController.afterShow(stage);
    }

    @Override
    protected void onCreateClicked() {
        if(optionsCreatorController.checkCreateReady()) {
            try {
                optionsCreatorController.getCreator().getId();
            } catch (ComponentCreationException e) {
                LauncherApplication.displayError(e);
            }
            reloadComponents();
            for(Pair<SelectorEntryElement, AnchorPane> option: elements) {
                option.getKey().beforeShow(null);
            }
        } else {
            optionsCreatorController.showError(true);
        }
    }

    @Override
    protected void deleteCurrent() {
        if(currentManifest != null) {
            if(!files.getOptionsManifest().getComponents().remove(currentManifest.getId())) {
                LOGGER.warn("Unable to remove options from manifest");
                return;
            }
            try {
                files.getOptionsManifest().writeToFile(files.getOptionsManifest().getDirectory() + LauncherApplication.config.MANIFEST_FILE_NAME);
            } catch (IOException e) {
                LauncherApplication.displayError(e);
                return;
            }
            optionsCreatorController.setPrerequisites(files.getOptionsComponents(), files.getLauncherDetails().getTypeConversion(), files.getOptionsManifest());
            try {
                FileUtil.deleteDir(new File(currentManifest.getDirectory()));
            } catch (IOException e) {
                LauncherApplication.displayError(e);
                return;
            }
            LOGGER.debug("Options deleted");
            setVisible(false);
            setVisible(true);
        }
    }

    @Override
    protected String getManifestId(LauncherInstanceDetails instanceDetails) {
        return instanceDetails.getOptionsComponent();
    }

    @Override
    protected List<LauncherManifest> getComponents() {
        return files.getOptionsComponents();
    }
}
