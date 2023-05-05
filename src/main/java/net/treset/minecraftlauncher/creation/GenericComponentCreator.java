package net.treset.minecraftlauncher.creation;

import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.minecraftlauncher.config.Config;
import net.treset.minecraftlauncher.util.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class GenericComponentCreator implements ComponentCreator {
    private static final Logger LOGGER = LogManager.getLogger(GenericComponentCreator.class);

    private LauncherManifest componentsManifest;

    public GenericComponentCreator(LauncherManifest componentsManifest) {
        this.componentsManifest = componentsManifest;
    }

    public boolean copyFiles(LauncherManifest oldManifest, LauncherManifest newManifest) {
        if(!isValid()) {
            LOGGER.warn("Unable to copy files: invalid parameters");
            return false;
        }
        if(oldManifest == null || newManifest == null || oldManifest.getDirectory() == null || newManifest.getDirectory() == null) {
            LOGGER.warn("Unable to copy files: invalid parameters");
            return false;
        }
        if(!FileUtil.copyContents(oldManifest.getDirectory(), newManifest.getDirectory(), (filename) -> !filename.equals(Config.MANIFEST_FILE_NAME) || !filename.equals(oldManifest.getDetails()), StandardCopyOption.REPLACE_EXISTING)) {
            LOGGER.warn("Unable to copy files: unable to copy files");
            return false;
        }
        return true;
    }

    public boolean writeManifest(LauncherManifest manifest) {
        if(!isValid()) {
            LOGGER.warn("Unable to write manifest: invalid parameters");
            return false;
        }
        manifest.setDirectory(componentsManifest.getDirectory() + componentsManifest.getPrefix() + "_" + manifest.getId() + "/");
        if(!manifest.writeToFile(manifest.getDirectory() + Config.MANIFEST_FILE_NAME)) {
            LOGGER.warn("Unable to write manifest: unable to write manifest to file");
            return false;
        }
        if(manifest.getIncludedFiles() != null) {
            if(!FileUtil.createDir(manifest.getDirectory() + Config.INCLUDED_FILES_DIR)) {
                LOGGER.warn("Unable to write manifest: unable to create included files directory");
                return false;
            }
        }
        return true;
    }

    public String getManifestType(LauncherManifestType type, Map<String, LauncherManifestType> typeConversion) {
        for(Map.Entry<String, LauncherManifestType> e : typeConversion.entrySet()) {
            if(e.getValue() == type) {
                return e.getKey();
            }
        }
        LOGGER.warn("Unable to get manifest type: no type found");
        return null;
    }

    private boolean isValid() {
        return componentsManifest != null && isComponentManifest() && componentsManifest.getDirectory() != null && componentsManifest.getPrefix() != null;
    }

    private boolean isComponentManifest() {
        return componentsManifest.getType() == LauncherManifestType.INSTANCES || componentsManifest.getType() == LauncherManifestType.OPTIONS || componentsManifest.getType() == LauncherManifestType.VERSIONS || componentsManifest.getType() == LauncherManifestType.RESOURCEPACKS || componentsManifest.getType() == LauncherManifestType.SAVES || componentsManifest.getType() == LauncherManifestType.MODS;
    }
}
