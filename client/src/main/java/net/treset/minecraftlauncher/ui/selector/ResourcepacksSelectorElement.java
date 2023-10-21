package net.treset.minecraftlauncher.ui.selector;

import net.treset.mc_version_loader.launcher.LauncherInstanceDetails;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.util.file.LauncherFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

public class ResourcepacksSelectorElement extends ManifestSelectorElement {
    private static final Logger LOGGER = LogManager.getLogger(ResourcepacksSelectorElement.class);

    @Override
    protected void reloadComponents() {
        super.reloadComponents();
        crCreator.init(files.getResourcepackComponents(), files.getLauncherDetails().getTypeConversion(), files.getResourcepackManifest());
    }

    @Override
    protected void deleteCurrent() {
        if(currentProvider != null) {
            if(!files.getResourcepackManifest().getComponents().remove(getManifest().getId())) {
                LOGGER.warn("Unable to remove resourcepacks from manifest");
                return;
            }
            try {
                LauncherFile.of(files.getResourcepackManifest().getDirectory(), LauncherApplication.config.MANIFEST_FILE_NAME).write(files.getResourcepackManifest());
            } catch (IOException e) {
                LauncherApplication.displayError(e);
                return;
            }
            try {
                LauncherFile.of(getManifest().getDirectory()).remove();
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


    @Override
    protected LauncherManifest getBaseManifest() {
        return files.getResourcepackManifest();
    }
}
