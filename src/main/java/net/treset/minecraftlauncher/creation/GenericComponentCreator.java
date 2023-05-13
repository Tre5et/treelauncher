package net.treset.minecraftlauncher.creation;

import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.util.FileUtil;
import net.treset.minecraftlauncher.util.FormatUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class GenericComponentCreator implements ComponentCreator {
    private static final Logger LOGGER = LogManager.getLogger(GenericComponentCreator.class);

    private LauncherManifestType type;
    private LauncherManifest uses;
    private LauncherManifest inheritsFrom;
    private String name;
    private Map<String, LauncherManifestType> typeConversion;
    private List<String> includedFiles;
    private String details;
    private LauncherManifest componentsManifest;
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
            LOGGER.warn("Unable to create {} component: unable to write manifest: id={}", type.toString().toLowerCase(), newManifest.getId());
            attemptCleanup();
            return null;
        }
        LOGGER.debug("Created {} component: id={}", type.toString().toLowerCase(), newManifest.getId());
        return newManifest.getId();
    }

    public String useComponent() {
        if(uses.getType() != type || uses.getId() == null) {
            LOGGER.warn("Unable to use {} component: invalid component specified", type.toString().toLowerCase());
            return null;
        }
        LOGGER.debug("Using {} component: id={}", type.toString().toLowerCase(), uses.getId());
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
            LOGGER.warn("Unable to inherit {} component: unable to write manifest to file: id={}", type.toString().toLowerCase(), newManifest.getId());
            attemptCleanup();
            return null;
        }

        if(!copyFiles(inheritsFrom, newManifest)) {
            LOGGER.warn("Unable to inherit {} component: unable to copy files: id={}", type.toString().toLowerCase(), newManifest.getId());
            attemptCleanup();
            return null;
        }

        LOGGER.debug("Inherited {} component: id={}", type.toString().toLowerCase(), newManifest.getId());
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
        if(!FileUtil.copyContents(oldManifest.getDirectory(), newManifest.getDirectory(), (filename) -> !filename.equals(LauncherApplication.config.MANIFEST_FILE_NAME), StandardCopyOption.REPLACE_EXISTING)) {
            LOGGER.warn("Unable to copy files: unable to copy files");
            return false;
        }
        LOGGER.debug("Copied files: src={}, dst={}", oldManifest.getDirectory(), newManifest.getDirectory());
        return true;
    }

    public boolean writeNewManifest() {
        if(!isValid() || newManifest == null) {
            LOGGER.warn("Unable to write manifest: invalid parameters");
            return false;
        }
        newManifest.setDirectory(componentsManifest.getDirectory() + componentsManifest.getPrefix() + "_" + newManifest.getId() + "/");
        if(!newManifest.writeToFile(newManifest.getDirectory() + LauncherApplication.config.MANIFEST_FILE_NAME)) {
            LOGGER.warn("Unable to write manifest: unable to write manifest to file: id={}, path={}", newManifest.getId(), newManifest.getDirectory() + LauncherApplication.config.MANIFEST_FILE_NAME);
            return false;
        }
        ArrayList<String> components = new ArrayList<>(componentsManifest.getComponents());
        components.add(newManifest.getId());
        componentsManifest.setComponents(components);
        if(!componentsManifest.writeToFile(componentsManifest.getDirectory() + getParentManifestFileName())) {
            LOGGER.warn("Unable to write manifest: unable to write parent manifest to file: id={}, path={}", newManifest.getId(), componentsManifest.getDirectory() + getParentManifestFileName());
            return false;
        }
        if(newManifest.getIncludedFiles() != null) {
            if(!FileUtil.createDir(newManifest.getDirectory() + LauncherApplication.config.INCLUDED_FILES_DIR)) {
                LOGGER.warn("Unable to write manifest: unable to create included files directory: id={}, path={}", newManifest.getId(), newManifest.getDirectory() + LauncherApplication.config.INCLUDED_FILES_DIR);
                return false;
            }
        }
        LOGGER.debug("Wrote manifest: path={}", newManifest.getDirectory() + LauncherApplication.config.MANIFEST_FILE_NAME);
        return true;
    }

    protected boolean attemptCleanup() {
        LOGGER.debug("Attempting cleanup");
        boolean success = true;
        if(newManifest != null && newManifest.getDirectory() != null) {
            File directory = new File(newManifest.getDirectory());
            if(directory.isDirectory()) {
                if (!FileUtil.deleteDir(directory)) {
                    LOGGER.warn("Unable to cleanup: unable to delete directory: continuing: path={}", newManifest.getDirectory());
                    success = false;
                } else {
                    LOGGER.debug("Cleaned up manifest: path={}", newManifest.getDirectory());
                }
            }
        }
        if(componentsManifest != null && componentsManifest.getComponents() != null && newManifest != null && newManifest.getId() != null && componentsManifest.getComponents().contains(newManifest.getId())) {
            List<String> components = new ArrayList<>(componentsManifest.getComponents());
            components.remove(newManifest.getId());
            componentsManifest.setComponents(components);
            if(!componentsManifest.writeToFile(componentsManifest.getDirectory() + getParentManifestFileName())) {
                LOGGER.warn("Unable to cleanup: unable to write parent manifest to file: continuing: id={}, path={}", newManifest.getId(), componentsManifest.getDirectory() + getParentManifestFileName());
                success = false;
            } else {
                LOGGER.debug("Cleaned up parent manifest: id={}", newManifest.getId());
            }
        }
        LOGGER.debug(success ? "Cleanup successful" : "Cleanup unsuccessful");
        return success;
    }

    protected String getParentManifestFileName() {
        return LauncherApplication.config.MANIFEST_FILE_NAME;
    }

    public String getManifestType(LauncherManifestType type, Map<String, LauncherManifestType> typeConversion) {
        for(Map.Entry<String, LauncherManifestType> e : typeConversion.entrySet()) {
            if(e.getValue() == type) {
                return e.getKey();
            }
        }
        LOGGER.warn("Unable to get manifest type: no type found: type={}", type.toString().toLowerCase());
        return null;
    }

    private boolean isValid() {
        return componentsManifest != null && isComponentManifest() && componentsManifest.getDirectory() != null && componentsManifest.getPrefix() != null;
    }

    private boolean isComponentManifest() {
        return componentsManifest.getType() == LauncherManifestType.INSTANCES || componentsManifest.getType() == LauncherManifestType.OPTIONS || componentsManifest.getType() == LauncherManifestType.VERSIONS || componentsManifest.getType() == LauncherManifestType.RESOURCEPACKS || componentsManifest.getType() == LauncherManifestType.SAVES || componentsManifest.getType() == LauncherManifestType.MODS || componentsManifest.getType() == LauncherManifestType.JAVAS;
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

    public void setType(LauncherManifestType type) {
        this.type = type;
    }

    public void setUses(LauncherManifest uses) {
        this.uses = uses;
    }

    public void setInheritsFrom(LauncherManifest inheritsFrom) {
        this.inheritsFrom = inheritsFrom;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTypeConversion(Map<String, LauncherManifestType> typeConversion) {
        this.typeConversion = typeConversion;
    }

    public void setIncludedFiles(List<String> includedFiles) {
        this.includedFiles = includedFiles;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setComponentsManifest(LauncherManifest componentsManifest) {
        this.componentsManifest = componentsManifest;
    }
}
