package net.treset.minecraftlauncher.data;

import javafx.util.Pair;
import net.treset.mc_version_loader.json.GenericJsonParsable;
import net.treset.mc_version_loader.launcher.*;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.util.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class LauncherFiles {
    private static Logger LOGGER = LogManager.getLogger(LauncherFiles.class);

    private LauncherManifest mainManifest;
    private LauncherDetails launcherDetails;
    private LauncherManifest gameDetailsManifest;
    private LauncherManifest modsManifest;
    private List<Pair<LauncherManifest, LauncherModsDetails>> modsComponents;
    private LauncherManifest savesManifest;
    private List<LauncherManifest> savesComponents;
    private LauncherManifest instanceManifest;
    private List<Pair<LauncherManifest, LauncherInstanceDetails>> instanceComponents;
    private LauncherManifest javaManifest;
    private List<LauncherManifest> javaComponents;
    private LauncherManifest optionsManifest;
    private List<LauncherManifest> optionsComponents;
    private LauncherManifest resourcepackManifest;
    private List<LauncherManifest> resourcepackComponents;
    private LauncherManifest versionManifest;
    private List<Pair<LauncherManifest, LauncherVersionDetails>> versionComponents;
    private boolean valid = false;

    public LauncherFiles() {
        if(reloadMainManifest()) {
            if(reloadLauncherDetails()) {
                this.valid = true;
                LOGGER.debug("Loaded launcher details");
            }
        }
    }

    public boolean reloadAll() {
        return reloadMainManifest() && reloadLauncherDetails() && reloadGameDetailsManifest() && reloadModsManifest() && reloadModsComponents() && reloadSavesManifest() && reloadSavesComponents() && reloadInstanceManifest() && reloadInstanceComponents()
                && reloadJavaManifest() && reloadJavaComponents() && reloadOptionsManifest() && reloadOptionsComponents() && reloadResourcepackManifest() && reloadResourcepackComponents() && reloadVersionManifest() && reloadVersionComponents();
    }

    public LauncherManifest getMainManifest() {
        return mainManifest;
    }

    public boolean reloadMainManifest() {
        String versionFile = FileUtil.loadFile(LauncherApplication.config.BASE_DIR + LauncherApplication.config.MANIFEST_FILE_NAME);
        if(versionFile == null) {
            LOGGER.warn("Unable to load launcher manifest: file error");
            return false;
        }
        mainManifest = LauncherManifest.fromJson(versionFile);
        if(mainManifest == null || mainManifest.getType() != LauncherManifestType.LAUNCHER) {
            mainManifest = null;
            LOGGER.warn("Unable to load launcher manifest: incorrect contents");
            return false;
        }
        mainManifest.setDirectory(LauncherApplication.config.BASE_DIR);
        LOGGER.debug("Loaded launcher manifest");
        return true;
    }

    public LauncherDetails getLauncherDetails() {
        return launcherDetails;
    }

    public boolean reloadLauncherDetails() {
        if(getMainManifest() == null || getMainManifest().getDetails() == null) {
            LOGGER.warn("Unable to load launcher details: invalid main file");
            return false;
        }
        String detailsFile = FileUtil.loadFile(LauncherApplication.config.BASE_DIR + mainManifest.getDetails());
        if(detailsFile == null) {
            LOGGER.warn("Unable to load launcher details: file error");
            return false;
        }
        this.launcherDetails = LauncherDetails.fromJson(detailsFile);
        if(this.launcherDetails == null || this.launcherDetails.getVersionDir() == null || this.launcherDetails.getVersionType() == null|| this.launcherDetails.getVersionComponentType() == null|| this.launcherDetails.getSavesType() == null || this.launcherDetails.getSavesComponentType() == null || this.launcherDetails.getResourcepacksType() == null|| this.launcherDetails.getResourcepacksComponentType() == null|| this.launcherDetails.getResourcepacksDir() == null || this.launcherDetails.getAssetsDir() == null|| this.launcherDetails.getGamedataDir() == null|| this.launcherDetails.getGamedataType() == null || this.launcherDetails.getInstancesDir() == null|| this.launcherDetails.getInstanceComponentType() == null|| this.launcherDetails.getInstancesType() == null || this.launcherDetails.getJavaComponentType() == null|| this.launcherDetails.getJavasDir() == null || this.launcherDetails.getJavasType() == null || this.launcherDetails.getLibrariesDir() == null|| this.launcherDetails.getModsComponentType() == null || this.launcherDetails.getModsType() == null || this.launcherDetails.getOptionsDir() == null || this.launcherDetails.getOptionsComponentType() == null || this.launcherDetails.getOptionsType() == null || this.launcherDetails.getSavesComponentType() == null|| this.launcherDetails.getSavesType() == null) {
            this.launcherDetails = null;
            LOGGER.warn("Unable to load launcher details: incorrect contents");
            return false;
        }
        LOGGER.debug("Loaded launcher details");
        return true;
    }

    public LauncherManifest getGameDetailsManifest() {
        if(gameDetailsManifest == null) {
            reloadGameDetailsManifest();
        }
        return gameDetailsManifest;
    }

    public boolean reloadGameDetailsManifest() {
        if(!isValid()) {
            LOGGER.warn("Unable to load game data manifest: invalid configuration");
            return false;
        }
        gameDetailsManifest = reloadManifest(getLauncherDetails().getGamedataDir(), LauncherManifestType.GAME);
        return gameDetailsManifest != null;
    }

    public LauncherManifest getModsManifest() {
        if(modsManifest == null) {
            reloadModsManifest();
        }
        return modsManifest;
    }

    public boolean reloadModsManifest() {
        if(!isValid() || getGameDetailsManifest() == null || getGameDetailsManifest().getComponents() == null || getGameDetailsManifest().getComponents().size() != 2) {
            LOGGER.warn("Unable to load mods manifest: invalid configuration");
            return false;
        }
        modsManifest = reloadManifest(getLauncherDetails().getGamedataDir(), getGameDetailsManifest().getComponents().get(0), LauncherManifestType.MODS);
        return modsManifest != null;
    }

    public List<Pair<LauncherManifest, LauncherModsDetails>> getModsComponents() {
        if(modsComponents == null) {
            reloadModsComponents();
        }
        return modsComponents;
    }

    public boolean reloadModsComponents() {
        if(!isValid()) {
            LOGGER.warn("Unable to load mods components: invalid configuration");
            return false;
        }
        modsComponents = reloadComponents(getModsManifest(), getLauncherDetails().getGamedataDir(), LauncherManifestType.MODS_COMPONENT, LauncherModsDetails.class, getLauncherDetails().getGamedataDir() + "/mods/");
        return modsComponents != null;
    }

    public LauncherManifest getSavesManifest() {
        if(savesManifest == null) {
            reloadSavesManifest();
        }
        return savesManifest;
    }

    public boolean reloadSavesManifest() {
        if(!isValid() || getGameDetailsManifest() == null || getGameDetailsManifest().getComponents() == null || getGameDetailsManifest().getComponents().size() != 2) {
            LOGGER.warn("Unable to load saves manifest: invalid configuration");
            return false;
        }
        savesManifest = reloadManifest(getLauncherDetails().getGamedataDir(), getGameDetailsManifest().getComponents().get(1), LauncherManifestType.SAVES);
        return savesManifest != null;
    }

    public List<LauncherManifest> getSavesComponents() {
        if(savesComponents == null) {
            reloadSavesComponents();
        }
        return savesComponents;
    }

    public boolean reloadSavesComponents() {
        if(!isValid()) {
            LOGGER.warn("Unable to load saves components: invalid configuration");
            return false;
        }
        savesComponents = reloadComponents(getSavesManifest(), getLauncherDetails().getGamedataDir(), LauncherManifestType.SAVES_COMPONENT, getLauncherDetails().getGamedataDir() + "/saves/");
        return savesComponents != null;
    }

    public LauncherManifest getInstanceManifest() {
        if(instanceManifest == null) {
            reloadInstanceManifest();
        }
        return instanceManifest;
    }

    public boolean reloadInstanceManifest() {
        if(!isValid()) {
            LOGGER.warn("Unable to load instance manifest: invalid configuration");
            return false;
        }
        instanceManifest = reloadManifest(getLauncherDetails().getInstancesDir(), LauncherManifestType.INSTANCES);
        return instanceManifest != null;
    }

    public List<Pair<LauncherManifest, LauncherInstanceDetails>> getInstanceComponents() {
        if(instanceComponents == null) {
            reloadInstanceComponents();
        }
        return instanceComponents;
    }

    public boolean reloadInstanceComponents() {
        if(!isValid()) {
            LOGGER.warn("Unable to load instance components: invalid configuration");
            return false;
        }
        instanceComponents = reloadComponents(getInstanceManifest(), getLauncherDetails().getInstancesDir(), LauncherManifestType.INSTANCE_COMPONENT, LauncherInstanceDetails.class, null);
        return instanceComponents != null;
    }


    public LauncherManifest getJavaManifest() {
        if(javaManifest == null) {
            reloadJavaManifest();
        }
        return javaManifest;
    }

    public boolean reloadJavaManifest() {
        if(!isValid()) {
            LOGGER.warn("Unable to load java manifest: invalid configuration");
            return false;
        }
        javaManifest = reloadManifest(getLauncherDetails().getJavasDir(), LauncherManifestType.JAVAS);
        return javaManifest != null;
    }

    public List<LauncherManifest> getJavaComponents() {
        if(javaComponents == null) {
            reloadJavaComponents();
        }
        return javaComponents;
    }

    public boolean reloadJavaComponents() {
        if(!isValid()) {
            LOGGER.warn("Unable to load java components: invalid configuration");
            return false;
        }
        javaComponents = reloadComponents(getJavaManifest(), getLauncherDetails().getJavasDir(), LauncherManifestType.JAVA_COMPONENT, null);
        return savesComponents != null;
    }

    public LauncherManifest getOptionsManifest() {
        if(optionsManifest == null) {
            reloadOptionsManifest();
        }
        return optionsManifest;
    }

    public boolean reloadOptionsManifest() {
        if(!isValid()) {
            LOGGER.warn("Unable to load options manifest: invalid configuration");
            return false;
        }
        optionsManifest = reloadManifest(getLauncherDetails().getOptionsDir(), LauncherManifestType.OPTIONS);
        return optionsManifest != null;
    }

    public List<LauncherManifest> getOptionsComponents() {
        if(optionsComponents == null) {
            reloadOptionsComponents();
        }
        return optionsComponents;
    }

    public boolean reloadOptionsComponents() {
        if(!isValid()) {
            LOGGER.warn("Unable to load options components: invalid configuration");
            return false;
        }
        optionsComponents = reloadComponents(getOptionsManifest(), getLauncherDetails().getOptionsDir(), LauncherManifestType.OPTIONS_COMPONENT, null);
        return optionsComponents != null;
    }

    public LauncherManifest getResourcepackManifest() {
        if(resourcepackManifest == null) {
            reloadResourcepackManifest();
        }
        return resourcepackManifest;
    }

    public boolean reloadResourcepackManifest() {
        if(!isValid()) {
            LOGGER.warn("Unable to load resourcepack manifest: invalid configuration");
            return false;
        }
        resourcepackManifest = reloadManifest(getLauncherDetails().getResourcepacksDir(), LauncherManifestType.RESOURCEPACKS);
        return resourcepackManifest != null;
    }

    public List<LauncherManifest> getResourcepackComponents() {
        if(resourcepackComponents == null) {
            reloadResourcepackComponents();
        }
        return resourcepackComponents;
    }

    public boolean reloadResourcepackComponents() {
        if(!isValid()) {
            LOGGER.warn("Unable to load resourcepack components: invalid configuration");
            return false;
        }
        resourcepackComponents = reloadComponents(getResourcepackManifest(), getLauncherDetails().getResourcepacksDir(), LauncherManifestType.RESOURCEPACKS_COMPONENT, null);
        return resourcepackComponents != null;
    }

    public LauncherManifest getVersionManifest() {
        if(versionManifest == null) {
            reloadVersionManifest();
        }
        return versionManifest;
    }

    public boolean reloadVersionManifest() {
        if(!isValid()) {
            LOGGER.warn("Unable to load version manifest: invalid configuration");
            return false;
        }
        versionManifest = reloadManifest(getLauncherDetails().getVersionDir(), LauncherManifestType.VERSIONS);
        return versionManifest != null;
    }

    public List<Pair<LauncherManifest, LauncherVersionDetails>> getVersionComponents() {
        if(versionComponents == null) {
            reloadVersionComponents();
        }
        return versionComponents;
    }

    public boolean reloadVersionComponents() {
        if(!isValid()) {
            LOGGER.warn("Unable to load version components: invalid configuration");
            return false;
        }
        versionComponents = reloadComponents(getVersionManifest(), getLauncherDetails().getVersionDir(), LauncherManifestType.VERSION_COMPONENT, LauncherVersionDetails.class, null);
        return versionComponents != null;
    }

    public boolean isValid() {
        return valid;
    }

    public LauncherManifest reloadManifest(String relativePath, LauncherManifestType expectedType) {
        return reloadManifest(relativePath, LauncherApplication.config.MANIFEST_FILE_NAME, expectedType);
    }

    public LauncherManifest reloadManifest(String relativePath, String filename, LauncherManifestType expectedType) {
        if(!isValid()) {
            LOGGER.warn("Unable to load " + expectedType.name().toLowerCase() + " manifest: invalid configuration");
            return null;
        }
        String versionFile = FileUtil.loadFile(LauncherApplication.config.BASE_DIR + relativePath + "/" + filename);
        if(versionFile == null) {
            LOGGER.warn("Unable to load " + expectedType.name().toLowerCase() + " manifest: file error");
            return null;
        }
        LauncherManifest out = LauncherManifest.fromJson(versionFile, getLauncherDetails().getTypeConversion());
        if(out == null || out.getType() != expectedType) {
            LOGGER.warn("Unable to load " + expectedType.name().toLowerCase() + " manifest: incorrect contents");
            return null;
        }
        out.setDirectory(LauncherApplication.config.BASE_DIR + relativePath + "/");
        LOGGER.debug("Loaded " + expectedType.name().toLowerCase() + " manifest");
        return out;
    }

    public List<LauncherManifest> reloadComponents(LauncherManifest parentManifest, String parentPath, LauncherManifestType expectedType, String fallbackPath) {
        return reloadComponents(parentManifest, parentPath, LauncherApplication.config.MANIFEST_FILE_NAME, expectedType, fallbackPath);
    }

    public List<LauncherManifest> reloadComponents(LauncherManifest parentManifest, String parentPath, String filename, LauncherManifestType expectedType, String fallbackPath) {
        if(!isValid() || parentManifest == null || parentManifest.getPrefix() == null || parentManifest.getComponents() == null) {
            LOGGER.warn("Unable to load " + expectedType.name().toLowerCase() + " components: invalid configuration");
            return null;
        }
        List<LauncherManifest> out = new ArrayList<>();
        for(String c : parentManifest.getComponents()) {
            if(!addComponent(out, LauncherApplication.config.BASE_DIR + parentPath + "/" + parentManifest.getPrefix() + "_" + c + "/", filename, expectedType, c, LauncherApplication.config.BASE_DIR + fallbackPath)) {
                LOGGER.warn("Unable to load " + expectedType.name().toLowerCase() + " components: component error: id=" + c);
                return null;
            }
        }
        LOGGER.debug("Loaded " + expectedType.name().toLowerCase() + " components");
        return out;
    }

    private boolean addComponent(List<LauncherManifest> list, String path, String filename, LauncherManifestType expectedType, String expectedId, String fallbackPath) {
        String manifestFile = FileUtil.loadFile(path + filename);
        if(manifestFile == null) {
            if(fallbackPath == null) {
                LOGGER.warn("Unable to load " + expectedType.name().toLowerCase() + " component: file error: id=" + expectedId);
                return false;
            }
            LOGGER.warn("Unable to load " + expectedType.name().toLowerCase() + " component: file error: id=" + expectedId);
            return addComponent(list, fallbackPath, filename, expectedType, expectedId, null);
        }
        LauncherManifest manifest = LauncherManifest.fromJson(manifestFile, getLauncherDetails().getTypeConversion());
        if(manifest == null || manifest.getType() == null || manifest.getType() != expectedType || manifest.getId() == null || !manifest.getId().equals(expectedId)) {
            if(fallbackPath == null) {
                LOGGER.warn("Unable to load " + expectedType.name().toLowerCase() + " component: incorrect contents: id=" + expectedId);
                return false;
            }
            LOGGER.debug("Falling back to fallback path loading " +  expectedType.name().toLowerCase() + " component id=" + expectedId);
            return addComponent(list, fallbackPath, filename, expectedType, expectedId, null);
        }
        manifest.setDirectory(path);
        list.add(manifest);
        return true;
    }

    public <T extends GenericJsonParsable> List<Pair<LauncherManifest, T>> reloadComponents(LauncherManifest parentManifest, String parentPath, LauncherManifestType expectedType, Class<T> targetClass, String fallbackPath) {
        return reloadComponents(parentManifest, parentPath, LauncherApplication.config.MANIFEST_FILE_NAME, expectedType, targetClass, fallbackPath);
    }

    public <T extends GenericJsonParsable> List<Pair<LauncherManifest, T>> reloadComponents(LauncherManifest parentManifest, String parentPath, String filename, LauncherManifestType expectedType, Class<T> targetClass, String fallbackPath) {
        if(!isValid() || parentManifest == null || parentManifest.getPrefix() == null || parentManifest.getComponents() == null) {
            LOGGER.warn("Unable to load " + expectedType.name().toLowerCase() + " components: invalid configuration");
            return null;
        }
        List<Pair<LauncherManifest, T>> out = new ArrayList<>();
        for(String c : parentManifest.getComponents()) {
            if(!addComponent(out, LauncherApplication.config.BASE_DIR + parentPath + "/" + parentManifest.getPrefix() + "_" + c + "/", filename, expectedType, targetClass, LauncherApplication.config.BASE_DIR + fallbackPath, c)) {
                LOGGER.warn("Unable to load " + expectedType.name().toLowerCase() + " components: component error: id=" + c);
                return null;
            }
        }
        LOGGER.debug("Loaded " + expectedType.name().toLowerCase() + " components");
        return out;
    }


    private <T extends GenericJsonParsable> boolean addComponent(List<Pair<LauncherManifest, T>> list, String path, String filename, LauncherManifestType expectedType, Class<T> targetClass, String fallbackPath, String expectedId) {
        String manifestFile = FileUtil.loadFile(path + filename);
        if(manifestFile == null) {
            if(fallbackPath == null) {
                LOGGER.warn("Unable to load " + expectedType.name().toLowerCase() + " component: file error: path=" + path);
                return false;
            }
            LOGGER.debug("Unable to load " + expectedType.name().toLowerCase() + " component: file error: path=" + path);
            return addComponent(list, fallbackPath, filename, expectedType, targetClass, null, expectedId);
        }
        LauncherManifest manifest = LauncherManifest.fromJson(manifestFile, getLauncherDetails().getTypeConversion());
        if(manifest == null || manifest.getType() == null || manifest.getType() != expectedType || manifest.getId() == null || !manifest.getId().equals(expectedId) || manifest.getDetails() == null) {
            if(fallbackPath == null) {
                LOGGER.warn("Unable to load " + expectedType.name().toLowerCase() + " component: incorrect contents: id=" + expectedId);
                return false;
            }
            LOGGER.debug("Falling back to fallback path loading " +  expectedType.name().toLowerCase() + " component id=" + expectedId);
            return addComponent(list, fallbackPath, filename, expectedType, targetClass, null, expectedId);
        }
        manifest.setDirectory(path);
        String detailsFile = FileUtil.loadFile(path + manifest.getDetails());
        if(detailsFile == null) {
            if(fallbackPath == null) {
                LOGGER.warn("Unable to load " + expectedType.name().toLowerCase() + " component details: file error: id=" + expectedId);
                return false;
            }
            LOGGER.debug("Falling back to fallback path loading " +  expectedType.name().toLowerCase() + " component id=" + expectedId);
            return addComponent(list, fallbackPath, filename, expectedType, targetClass, null, expectedId);
        }
        T details = GenericJsonParsable.fromJson(detailsFile, targetClass);
        if(details == null) {
            if(fallbackPath == null) {
                LOGGER.warn("Unable to load " + expectedType.name().toLowerCase() + " component details: incorrect contents: id=" + expectedId);
                return false;
            }
            LOGGER.debug("Falling back to fallback path loading " +  expectedType.name().toLowerCase() + " component id=" + expectedId);
            return addComponent(list, fallbackPath, filename, expectedType, targetClass, null, expectedId);
        }
        list.add(new Pair<>(manifest, details));
        return true;
    }
}
