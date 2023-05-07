package net.treset.minecraftlauncher.creation;

import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.minecraftlauncher.config.Config;
import net.treset.minecraftlauncher.util.FileUtil;
import net.treset.minecraftlauncher.util.FormatUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class GenericComponentCreator implements ComponentCreator {
    private static final Logger LOGGER = LogManager.getLogger(GenericComponentCreator.class);

    private final LauncherManifestType type;
    private final LauncherManifest uses;
    private final LauncherManifest inheritsFrom;
    private final String name;
    private final Map<String, LauncherManifestType> typeConversion;
    private final List<String> includedFiles;
    private final String details;
    private final LauncherManifest componentsManifest;
    private LauncherManifest newManifest;

    public GenericComponentCreator(LauncherManifestType type, LauncherManifest uses, LauncherManifest inheritsFrom, String name, Map<String, LauncherManifestType> typeConversion, List<String> includedFiles, String details, LauncherManifest componentsManifest) {
        this.type = type;
        this.uses = uses;
        this.inheritsFrom = inheritsFrom;
        this.name = name;
        this.typeConversion = typeConversion;
        this.includedFiles = includedFiles;
        this.details = details;
        this.componentsManifest = componentsManifest;
    }

    @Override
    public String getId() {
        if(uses != null) {
            return useComponent();
        }

        if(name == null) {
            LOGGER.warn("Unable to create {} component: invalid name", type.toString().toLowerCase());
            return null;
        }

        if(inheritsFrom != null) {
            return inheritComponent();
        }

        return createComponent();
    }


    public String createComponent() {
        if(typeConversion == null) {
            LOGGER.warn("Unable to create {} component: invalid parameters", type.toString().toLowerCase());
            return null;
        }

        String manifestType = getManifestType(type, typeConversion);
        if(manifestType == null) {
            LOGGER.warn("Unable to create {} component: unable to get manifest type", type.toString().toLowerCase());
            return null;
        }
        newManifest = new LauncherManifest(manifestType, typeConversion, null, details, null, name, includedFiles, null);
        newManifest.setId(FormatUtil.hash(newManifest));
        if(!writeNewManifest()) {
            LOGGER.warn("Unable to create {} component: unable to write manifest", type.toString().toLowerCase());
            return null;
        }
        return newManifest.getId();
    }

    public String useComponent() {
        if(uses.getType() != type || uses.getId() == null) {
            LOGGER.warn("Unable to use {} component: invalid component specified", type.toString().toLowerCase());
            return null;
        }
        return uses.getId();
    }

    public String inheritComponent() {
        if(inheritsFrom.getType() != type) {
            LOGGER.warn("Unable to inherit {} component: invalid component specified", type.toString().toLowerCase());
            return null;
        }
        String manifestType = getManifestType(type, inheritsFrom.getTypeConversion());
        if(manifestType == null) {
            LOGGER.warn("Unable to inherit {} component: unable to get manifest type", type.toString().toLowerCase());
            return null;
        }
        newManifest = new LauncherManifest(manifestType, inheritsFrom.getTypeConversion(), null, inheritsFrom.getDetails(), inheritsFrom.getPrefix(), name, inheritsFrom.getIncludedFiles(), inheritsFrom.getComponents());
        newManifest.setId(FormatUtil.hash(newManifest));
        if(!writeNewManifest()) {
            LOGGER.warn("Unable to inherit {} component: unable to write manifest to file", type.toString().toLowerCase());
            return null;
        }

        if(!copyFiles(inheritsFrom, newManifest)) {
            LOGGER.warn("Unable to inherit {} component: unable to copy files", type.toString().toLowerCase());
            return null;
        }

        return newManifest.getId();
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

    public boolean writeNewManifest() {
        if(!isValid() || newManifest == null) {
            LOGGER.warn("Unable to write manifest: invalid parameters");
            return false;
        }
        newManifest.setDirectory(componentsManifest.getDirectory() + componentsManifest.getPrefix() + "_" + newManifest.getId() + "/");
        if(!newManifest.writeToFile(newManifest.getDirectory() + Config.MANIFEST_FILE_NAME)) {
            LOGGER.warn("Unable to write manifest: unable to write manifest to file");
            return false;
        }
        ArrayList<String> components = new ArrayList<>(componentsManifest.getComponents());
        components.add(newManifest.getId());
        componentsManifest.setComponents(components);
        if(!componentsManifest.writeToFile(componentsManifest.getDirectory() + Config.MANIFEST_FILE_NAME)) {
            LOGGER.warn("Unable to write manifest: unable to write parent manifest to file");
            return false;
        }
        if(newManifest.getIncludedFiles() != null) {
            if(!FileUtil.createDir(newManifest.getDirectory() + Config.INCLUDED_FILES_DIR)) {
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

    public LauncherManifest getNewManifest() {
        return newManifest;
    }

    public String getDetails() {
        return details;
    }

    public LauncherManifestType getType() {
        return type;
    }

    public LauncherManifest getUses() {
        return uses;
    }

    public LauncherManifest getInheritsFrom() {
        return inheritsFrom;
    }

    public String getName() {
        return name;
    }

    public Map<String, LauncherManifestType> getTypeConversion() {
        return typeConversion;
    }

    public List<String> getIncludedFiles() {
        return includedFiles;
    }

    public LauncherManifest getComponentsManifest() {
        return componentsManifest;
    }
}
