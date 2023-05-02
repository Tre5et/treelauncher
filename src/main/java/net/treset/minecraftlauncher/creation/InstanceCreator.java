package net.treset.minecraftlauncher.creation;

import javafx.util.Pair;
import net.treset.mc_version_loader.fabric.FabricVersionDetails;
import net.treset.mc_version_loader.launcher.*;
import net.treset.mc_version_loader.minecraft.MinecraftVersionDetails;
import net.treset.minecraftlauncher.config.Config;
import net.treset.minecraftlauncher.data.LauncherFiles;
import net.treset.minecraftlauncher.util.FormatUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InstanceCreator {
    private static final Logger LOGGER = LogManager.getLogger(InstanceCreator.class);

    private LauncherFiles files;
    private MinecraftVersionDetails mcVersion;
    private FabricVersionDetails fabricVersion;
    private String name;
    private String optionsId;
    private String versionId;
    private String modsId;
    private String resourcepacksId;
    private String savesId;
    private List<String> includedFiles;
    private List<String> ignoredFiles;
    private List<LauncherLaunchArgument> jvmArguments;
    private List<LauncherFeature> features;
    private LauncherManifest instanceManifest;
    private LauncherInstanceDetails instanceDetails;
    private boolean ready = false;

    public InstanceCreator(LauncherFiles files, MinecraftVersionDetails mcVersion, FabricVersionDetails fabricVersion, String name, String optionsId, String versionId, String modsId, String resourcepacksId, String savesId, List<String> includedFiles, List<String> ignoredFiles, List<LauncherLaunchArgument> jvmArguments, List<LauncherFeature> features) {
        this.files = files;
        this.mcVersion = mcVersion;
        this.fabricVersion = fabricVersion;
        this.name = name;
        this.optionsId = optionsId;
        this.versionId = versionId;
        this.modsId = modsId;
        this.resourcepacksId = resourcepacksId;
        this.savesId = savesId;
        this.includedFiles = includedFiles;
        this.ignoredFiles = ignoredFiles;
        this.jvmArguments = jvmArguments;
        this.features = features;
    }

    public boolean createInstance() {
        if(name == null || files == null || !files.reloadAll() || !files.isValid()) {
            LOGGER.warn("Unable to create instance: invalid parameters");
            return false;
        }

        if(!createInstanceManifest()) {
            LOGGER.warn("Unable to create instance: unable to create instance manifest");
            return false;
        }

        if(!createInstanceDetails()) {
            LOGGER.warn("Unable to create instance: unable to create instance details");
            return false;
        }
        return true;
    }

    private boolean createInstanceManifest() {
        instanceManifest = new LauncherManifest(null, files.getLauncherDetails().getTypeConversion(), null, "instance.json", null, name, null, null);

        if(!setType()) {
            LOGGER.warn("Unable to create instance manifest: unable to set type");
            return false;
        }

        if(!addIncludedFiles()) {
            LOGGER.warn("Unable to create instance manifest: unable to add included files");
            return false;
        }

        if(!setId()) {
            LOGGER.warn("Unable to create instance manifest: unable to set id");
            return false;
        }
        return true;
    }

    private boolean createInstanceDetails() {
        instanceDetails = new LauncherInstanceDetails(null, null, null, null, null, null, null, null);

        if(!setFeatures()) {
            LOGGER.warn("Unable to create instance details: unable to set features");
            return false;
        }

        if(!setIgnoredFiles()) {
            LOGGER.warn("Unable to create instance details: unable to set ignored files");
            return false;
        }

        if(!setJvmArguments()) {
            LOGGER.warn("Unable to create instance details: unable to set jvm arguments");
            return false;
        }

        if(!setVersionComponent()) {
            LOGGER.warn("Unable to create instance details: unable to set version component");
            return false;
        }

        if(!setOptionsComponent()) {
            LOGGER.warn("Unable to create instance details: unable to set options component");
            return false;
        }

        if(!setSavesComponent()) {
            LOGGER.warn("Unable to create instance details: unable to set saves component");
            return false;
        }

        if(!setResourcepacksComponent()) {
            LOGGER.warn("Unable to create instance details: unable to set resourcepacks component");
            return false;
        }

        if(!setModsComponent()) {
            LOGGER.warn("Unable to create instance details: unable to set mods component");
            return false;
        }

        return true;
    }

    private boolean setVersionComponent() {
        // TODO: create version component
        instanceDetails.setVersionComponent(versionId);
        return false;
    }

    private boolean setSavesComponent() {
        // TODO: create saves component
        instanceDetails.setSavesComponent(savesId);
        return false;
    }

    private boolean setOptionsComponent() {
        // TODO: create options component
        instanceDetails.setOptionsComponent(optionsId);
        return false;
    }

    private boolean setResourcepacksComponent() {
        // TODO: create resourcepacks component
        instanceDetails.setResourcepacksComponent(resourcepacksId);
        return false;
    }

    private boolean setModsComponent() {
        // TODO: create mods component
        instanceDetails.setModsComponent(modsId.equals("none") ? null : modsId);
        return false;
    }

    private boolean setJvmArguments() {
        instanceDetails.setJvm_arguments(jvmArguments == null ? List.of() : jvmArguments);
        return true;
    }

    private boolean setFeatures() {
        instanceDetails.setFeatures(features == null ? List.of() : features);
        return true;
    }

    private boolean setIgnoredFiles() {
        ArrayList<String> resultFiles = new ArrayList<>(Config.INSTANCE_DEFAULT_IGNORED_FILES);
        if(ignoredFiles != null) {
            resultFiles.addAll(ignoredFiles);
        }
        instanceDetails.setIgnoredFiles(resultFiles);
        return true;
    }

    private boolean setType() {
        for(Map.Entry<String, LauncherManifestType> e : files.getLauncherDetails().getTypeConversion().entrySet()) {
            if(e.getValue() == LauncherManifestType.INSTANCE_COMPONENT) {
                instanceManifest.setType(e.getKey());
                return true;
            }
        }
        LOGGER.warn("Unable to set instance type: no type found");
        return false;
    }

    private boolean addIncludedFiles() {
        ArrayList<String> resultFiles = new ArrayList<>(Config.INSTANCE_DEFAULT_INCLUDED_FILES);
        if(includedFiles != null) {
            resultFiles.addAll(includedFiles);
        }
        instanceManifest.setIncludedFiles(resultFiles);
        return true;
    }

    private boolean setId() {
        instanceManifest.setId(FormatUtil.hash(instanceManifest));
        return true;
    }

    public LauncherFiles getFiles() {
        return files;
    }

    public void setFiles(LauncherFiles files) {
        this.files = files;
    }

    public MinecraftVersionDetails getMcVersion() {
        return mcVersion;
    }

    public void setMcVersion(MinecraftVersionDetails mcVersion) {
        this.mcVersion = mcVersion;
    }

    public FabricVersionDetails getFabricVersion() {
        return fabricVersion;
    }

    public void setFabricVersion(FabricVersionDetails fabricVersion) {
        this.fabricVersion = fabricVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOptionsId() {
        return optionsId;
    }

    public void setOptionsId(String optionsId) {
        this.optionsId = optionsId;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public String getModsId() {
        return modsId;
    }

    public void setModsId(String modsId) {
        this.modsId = modsId;
    }

    public String getResourcepacksId() {
        return resourcepacksId;
    }

    public void setResourcepacksId(String resourcepacksId) {
        this.resourcepacksId = resourcepacksId;
    }

    public String getSavesId() {
        return savesId;
    }

    public void setSavesId(String savesId) {
        this.savesId = savesId;
    }

    public List<String> getIncludedFiles() {
        return includedFiles;
    }

    public void setIncludedFiles(List<String> includedFiles) {
        this.includedFiles = includedFiles;
    }

    public List<String> getIgnoredFiles() {
        return ignoredFiles;
    }

    public void setIgnoredFiles(List<String> ignoredFiles) {
        this.ignoredFiles = ignoredFiles;
    }

    public List<LauncherFeature> getFeatures() {
        return features;
    }

    public void setFeatures(List<LauncherFeature> features) {
        this.features = features;
    }

    public List<LauncherLaunchArgument> getJvmArguments() {
        return jvmArguments;
    }

    public void setJvmArguments(List<LauncherLaunchArgument> jvmArguments) {
        this.jvmArguments = jvmArguments;
    }

    public boolean isReady() {
        return ready;
    }

    public Pair<LauncherManifest, LauncherInstanceDetails> getInstance() {
        if(!isReady()) {
            return null;
        }
        return new Pair<>(instanceManifest, instanceDetails);
    }
}
