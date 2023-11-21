package net.treset.minecraftlauncher.ui.selector;

import net.treset.mc_version_loader.launcher.LauncherInstanceDetails;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.util.file.LauncherFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

public class OptionsSelectorElement extends ManifestSelectorElement {
    private static final Logger LOGGER = LogManager.getLogger(OptionsSelectorElement.class);

    @Override
    protected void reloadComponents() {
        super.reloadComponents();
        crCreator.init(files.getResourcepackComponents(), files.getLauncherDetails().getTypeConversion(), files.getResourcepackManifest());
    }

    @Override
    protected void deleteCurrent() {
        if(currentProvider != null) {
            if(!files.getOptionsManifest().getComponents().remove(currentProvider.getManifest().getId())) {
                LOGGER.warn("Unable to remove options from manifest");
                return;
            }
            try {
                LauncherFile.of(files.getOptionsManifest().getDirectory(), LauncherApplication.config.MANIFEST_FILE_NAME).write(files.getOptionsManifest());
            } catch (IOException e) {
                LauncherApplication.displayError(e);
                return;
            }
            try {
                LauncherFile.of(currentProvider.getManifest().getDirectory()).remove();
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

    @Override
    protected LauncherManifest getBaseManifest() {
        return files.getOptionsManifest();
    }
}
