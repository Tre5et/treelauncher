package net.treset.minecraftlauncher.creation;

import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.util.CreationStatus;
import net.treset.minecraftlauncher.util.FormatUtil;
import net.treset.minecraftlauncher.util.exception.ComponentCreationException;
import net.treset.minecraftlauncher.util.file.LauncherFile;
import net.treset.minecraftlauncher.util.string.PatternString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class GenericComponentCreator implements ComponentCreator {
    private static final Logger LOGGER = LogManager.getLogger(GenericComponentCreator.class);

    private LauncherManifestType type;
    private LauncherManifest uses;
    private LauncherManifest inheritsFrom;
    private String name;
    private Map<String, LauncherManifestType> typeConversion;
    private List<PatternString> includedFiles;
    private String details;
    private LauncherManifest componentsManifest;
    private LauncherManifest newManifest;
    private Consumer<CreationStatus> statusCallback;
    private CreationStatus defaultStatus;

    public GenericComponentCreator(LauncherManifestType type, LauncherManifest uses, LauncherManifest inheritsFrom, String name, Map<String, LauncherManifestType> typeConversion, List<PatternString> includedFiles, String details, LauncherManifest componentsManifest) {
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
    public String getId() throws ComponentCreationException {
        if(defaultStatus != null) {
            setStatus(defaultStatus);
        }
        if(uses != null) {
            return useComponent();
        }

        if(name == null) {
            throw new ComponentCreationException("Unable to create " + type.toString().toLowerCase() + " component: invalid parameters");
        }

        if(inheritsFrom != null) {
            return inheritComponent();
        }

        return createComponent();
    }


    public String createComponent() throws ComponentCreationException {
        if(typeConversion == null) {
            throw new ComponentCreationException("Unable to create " + type.toString().toLowerCase() + " component: invalid parameters");
        }

        String manifestType = getManifestType(type, typeConversion);
        if(manifestType == null) {
            throw new ComponentCreationException("Unable to create " + type.toString().toLowerCase() + " component: invalid parameters");
        }
        newManifest = new LauncherManifest(manifestType, typeConversion, null, details, null, name, includedFiles == null ? null : includedFiles.stream().map(PatternString::get).toList(), null);
        newManifest.setId(FormatUtil.hash(newManifest));
        try {
            writeNewManifest();
        } catch (ComponentCreationException e) {
            attemptCleanup();
            throw new ComponentCreationException("Unable to create " + type.toString().toLowerCase() + " component: unable to write manifest", e);
        }
        LOGGER.debug("Created {} component: id={}", type.toString().toLowerCase(), newManifest.getId());
        return newManifest.getId();
    }

    public String useComponent() throws ComponentCreationException {
        if(uses.getType() != type || uses.getId() == null) {
            throw new ComponentCreationException("Unable to use " + type.toString().toLowerCase() + " component: invalid component specified");
        }
        LOGGER.debug("Using {} component: id={}", type.toString().toLowerCase(), uses.getId());
        return uses.getId();
    }

    public String inheritComponent() throws ComponentCreationException {
        if(inheritsFrom.getType() != type) {
            throw new ComponentCreationException("Unable to inherit " + type.toString().toLowerCase() + " component: invalid component specified");
        }
        String manifestType = getManifestType(type, inheritsFrom.getTypeConversion());
        if(manifestType == null) {
            throw new ComponentCreationException("Unable to inherit " + type.toString().toLowerCase() + " component: unable to get manifest type");
        }
        newManifest = new LauncherManifest(manifestType, inheritsFrom.getTypeConversion(), null, inheritsFrom.getDetails(), inheritsFrom.getPrefix(), name, inheritsFrom.getIncludedFiles(), inheritsFrom.getComponents());
        newManifest.setId(FormatUtil.hash(newManifest));
        try {
            writeNewManifest();
        } catch (ComponentCreationException e){
            attemptCleanup();
            throw new ComponentCreationException("Unable to inherit " + type.toString().toLowerCase() + " component: unable to write manifest: id=" + newManifest.getId(), e);
        }

        try {
            copyFiles(inheritsFrom, newManifest);
        } catch (ComponentCreationException e) {
            attemptCleanup();
            throw new ComponentCreationException("Unable to inherit " + type.toString().toLowerCase() + " component: unable to copy files: id=" + newManifest.getId(), e);
        }

        LOGGER.debug("Inherited {} component: id={}", type.toString().toLowerCase(), newManifest.getId());
        return newManifest.getId();
    }

    public void copyFiles(LauncherManifest oldManifest, LauncherManifest newManifest) throws ComponentCreationException {
        if(!isValid() || oldManifest == null || newManifest == null || oldManifest.getDirectory() == null || newManifest.getDirectory() == null) {
            throw new ComponentCreationException("Unable to copy files: invalid parameters");
        }
        try {
            LauncherFile.of(oldManifest.getDirectory()).copyTo(LauncherFile.of(newManifest.getDirectory()), (filename) -> !filename.equals(LauncherApplication.config.MANIFEST_FILE_NAME), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ComponentCreationException("Unable to copy files: unable to copy files", e);
        }
        LOGGER.debug("Copied files: src={}, dst={}", oldManifest.getDirectory(), newManifest.getDirectory());
    }

    public void writeNewManifest() throws ComponentCreationException {
        if(!isValid() || newManifest == null) {
            throw new ComponentCreationException("Unable to write manifest: invalid parameters");
        }
        newManifest.setDirectory(LauncherFile.of(componentsManifest.getDirectory(), componentsManifest.getPrefix() + "_" + newManifest.getId()).getPath());
        try {
            LauncherFile.of(newManifest.getDirectory(), LauncherApplication.config.MANIFEST_FILE_NAME).write(newManifest);
        } catch (IOException e) {
            throw new ComponentCreationException("Unable to write manifest: unable to write manifest to file: id=" + newManifest.getId() + ", path=" + newManifest.getDirectory() + LauncherApplication.config.MANIFEST_FILE_NAME, e);
        }
        ArrayList<String> components = new ArrayList<>(componentsManifest.getComponents());
        components.add(newManifest.getId());
        componentsManifest.setComponents(components);
        try {
            LauncherFile.of(componentsManifest.getDirectory(), getParentManifestFileName()).write(componentsManifest);
        } catch (IOException e) {
            throw new ComponentCreationException("Unable to write manifest: unable to write parent manifest to file: id=" + newManifest.getId() + ", path=" + componentsManifest.getDirectory() + getParentManifestFileName(), e);
        }
        if(newManifest.getIncludedFiles() != null) {
            try {
                LauncherFile.of(newManifest.getDirectory(), LauncherApplication.config.INCLUDED_FILES_DIR).createDir();
            } catch (IOException e) {
                throw new ComponentCreationException("Unable to write manifest: unable to create included files directory: id=" + newManifest.getId() + ", path=" + newManifest.getDirectory() + LauncherApplication.config.INCLUDED_FILES_DIR);
            }
        }
        LOGGER.debug("Wrote manifest: path={}", newManifest.getDirectory() + LauncherApplication.config.MANIFEST_FILE_NAME);
    }

    protected boolean attemptCleanup() {
        LOGGER.debug("Attempting cleanup");
        boolean success = true;
        if(newManifest != null && newManifest.getDirectory() != null) {
            LauncherFile directory = LauncherFile.of(newManifest.getDirectory());
            if(directory.isDirectory()) {
                try{
                    directory.remove();
                    LOGGER.debug("Cleaned up manifest: path={}", newManifest.getDirectory());
                } catch (IOException e) {
                    LOGGER.warn("Unable to cleanup: unable to delete directory: continuing: path={}", newManifest.getDirectory());
                    success = false;
                }
            }
        }
        if(componentsManifest != null && componentsManifest.getComponents() != null && newManifest != null && newManifest.getId() != null && componentsManifest.getComponents().contains(newManifest.getId())) {
            List<String> components = new ArrayList<>(componentsManifest.getComponents());
            components.remove(newManifest.getId());
            componentsManifest.setComponents(components);
            try {
                LauncherFile.of(componentsManifest.getDirectory(), getParentManifestFileName()).write(componentsManifest);
                LOGGER.debug("Cleaned up parent manifest: id={}", newManifest.getId());
            } catch (IOException e) {
                LOGGER.warn("Unable to cleanup: unable to write parent manifest to file: continuing: id={}, path={}", newManifest.getId(), componentsManifest.getDirectory() + getParentManifestFileName());
                success = false;
            }
        }
        LOGGER.debug(success ? "Cleanup successful" : "Cleanup unsuccessful");
        return success;
    }

    protected String getParentManifestFileName() {
        return LauncherApplication.config.MANIFEST_FILE_NAME;
    }

    public String getManifestType(LauncherManifestType type, Map<String, LauncherManifestType> typeConversion) throws IllegalArgumentException {
        for(Map.Entry<String, LauncherManifestType> e : typeConversion.entrySet()) {
            if(e.getValue() == type) {
                return e.getKey();
            }
        }
        throw new IllegalArgumentException("Unable to get manifest type: no type found: type=" + type.toString().toLowerCase());
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

    public List<PatternString> getIncludedFiles() {
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

    public void setIncludedFiles(List<PatternString> includedFiles) {
        this.includedFiles = includedFiles;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setComponentsManifest(LauncherManifest componentsManifest) {
        this.componentsManifest = componentsManifest;
    }

    public Consumer<CreationStatus> getStatusCallback() {
        return statusCallback;
    }

    public void setStatusCallback(Consumer<CreationStatus> statusCallback) {
        this.statusCallback = statusCallback;
    }

    public void setStatus(CreationStatus status) {
        if(statusCallback != null) {
            statusCallback.accept(status);
        }
    }

    public void setDefaultStatus(CreationStatus status) {
        defaultStatus = status;
    }

    public CreationStatus getDefaultStatus() {
        return defaultStatus;
    }
}
