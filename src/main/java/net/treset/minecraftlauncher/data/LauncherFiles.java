package net.treset.minecraftlauncher.data;

import javafx.util.Pair;
import net.treset.mc_version_loader.json.GenericJsonParsable;
import net.treset.mc_version_loader.launcher.*;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.util.FileUtil;
import net.treset.minecraftlauncher.util.FormatUtil;
import net.treset.minecraftlauncher.util.exception.FileLoadException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LauncherFiles {
    private static final Logger LOGGER = LogManager.getLogger(LauncherFiles.class);

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

    public LauncherFiles() throws FileLoadException {
        reloadMainManifest();
        reloadLauncherDetails();

        LOGGER.debug("Loaded launcher details");
    }

    public void reloadAll() throws FileLoadException {
        reloadMainManifest();
        reloadLauncherDetails();
        reloadGameDetailsManifest();
        reloadModsManifest();
        reloadModsComponents();
        reloadSavesManifest();
        reloadSavesComponents();
        reloadInstanceManifest();
        reloadInstanceComponents();
        reloadJavaManifest();
        reloadJavaComponents();
        reloadOptionsManifest();
        reloadOptionsComponents();
        reloadResourcepackManifest();
        reloadResourcepackComponents();
        reloadVersionManifest();
        reloadVersionComponents();
    }

    public LauncherManifest getMainManifest() {
        return mainManifest;
    }

    public void reloadMainManifest() throws FileLoadException {
        String versionFile;
        try {
            versionFile = FileUtil.loadFile(LauncherApplication.config.BASE_DIR + LauncherApplication.config.MANIFEST_FILE_NAME);
        } catch (IOException e) {
            throw new FileLoadException("Unable to load launcher manifest: file error", e);
        }

        mainManifest = LauncherManifest.fromJson(versionFile);
        if(mainManifest == null || mainManifest.getType() != LauncherManifestType.LAUNCHER) {
            mainManifest = null;
            throw new FileLoadException("Unable to load launcher manifest: incorrect contents");
        }
        mainManifest.setDirectory(LauncherApplication.config.BASE_DIR);
        LOGGER.debug("Loaded launcher manifest");
    }

    public LauncherDetails getLauncherDetails() {
        return launcherDetails;
    }

    public void reloadLauncherDetails() throws FileLoadException {
        if(getMainManifest() == null || getMainManifest().getDetails() == null) {
            throw new FileLoadException("Unable to load launcher details: invalid main file");
        }
        String detailsFile;
        try {
            detailsFile = FileUtil.loadFile(LauncherApplication.config.BASE_DIR + mainManifest.getDetails());
        } catch (IOException e) {
            throw new FileLoadException("Unable to load launcher details: file error", e);
        }

        this.launcherDetails = LauncherDetails.fromJson(detailsFile);
        if(this.launcherDetails == null || this.launcherDetails.getVersionDir() == null || this.launcherDetails.getVersionType() == null|| this.launcherDetails.getVersionComponentType() == null|| this.launcherDetails.getSavesType() == null || this.launcherDetails.getSavesComponentType() == null || this.launcherDetails.getResourcepacksType() == null|| this.launcherDetails.getResourcepacksComponentType() == null|| this.launcherDetails.getResourcepacksDir() == null || this.launcherDetails.getAssetsDir() == null|| this.launcherDetails.getGamedataDir() == null|| this.launcherDetails.getGamedataType() == null || this.launcherDetails.getInstancesDir() == null|| this.launcherDetails.getInstanceComponentType() == null|| this.launcherDetails.getInstancesType() == null || this.launcherDetails.getJavaComponentType() == null|| this.launcherDetails.getJavasDir() == null || this.launcherDetails.getJavasType() == null || this.launcherDetails.getLibrariesDir() == null|| this.launcherDetails.getModsComponentType() == null || this.launcherDetails.getModsType() == null || this.launcherDetails.getOptionsDir() == null || this.launcherDetails.getOptionsComponentType() == null || this.launcherDetails.getOptionsType() == null || this.launcherDetails.getSavesComponentType() == null|| this.launcherDetails.getSavesType() == null) {
            this.launcherDetails = null;
            throw new FileLoadException("Unable to load launcher details: incorrect contents");
        }
        LOGGER.debug("Loaded launcher details");
    }

    public LauncherManifest getGameDetailsManifest() {
        return gameDetailsManifest;
    }

    public void reloadGameDetailsManifest() throws FileLoadException {
        gameDetailsManifest = reloadManifest(getLauncherDetails().getGamedataDir(), LauncherManifestType.GAME);
    }

    public LauncherManifest getModsManifest() {
        return modsManifest;
    }

    public void reloadModsManifest() throws FileLoadException {
        if(getGameDetailsManifest() == null || getGameDetailsManifest().getComponents() == null || getGameDetailsManifest().getComponents().size() != 2) {
            throw new FileLoadException("Unable to load mods manifest: invalid configuration");
        }
        modsManifest = reloadManifest(getLauncherDetails().getGamedataDir(), getGameDetailsManifest().getComponents().get(0), LauncherManifestType.MODS);
    }

    public List<Pair<LauncherManifest, LauncherModsDetails>> getModsComponents() {
        return modsComponents;
    }

    public void reloadModsComponents() throws FileLoadException {
        modsComponents = reloadComponents(getModsManifest(), getLauncherDetails().getGamedataDir(), LauncherManifestType.MODS_COMPONENT, LauncherModsDetails.class, FormatUtil.absoluteDirPath(getLauncherDetails().getGamedataDir(), "mods"));
    }

    public LauncherManifest getSavesManifest() {
        return savesManifest;
    }

    public void reloadSavesManifest() throws FileLoadException {
        savesManifest = reloadManifest(getLauncherDetails().getGamedataDir(), getGameDetailsManifest().getComponents().get(1), LauncherManifestType.SAVES);
    }

    public List<LauncherManifest> getSavesComponents() {
        return savesComponents;
    }

    public void reloadSavesComponents() throws FileLoadException {
        savesComponents = reloadComponents(getSavesManifest(), getLauncherDetails().getGamedataDir(), LauncherManifestType.SAVES_COMPONENT, FormatUtil.absoluteDirPath(getLauncherDetails().getGamedataDir() + "saves"));
    }

    public LauncherManifest getInstanceManifest() {
        return instanceManifest;
    }

    public void reloadInstanceManifest() throws FileLoadException {
        instanceManifest = reloadManifest(getLauncherDetails().getInstancesDir(), LauncherManifestType.INSTANCES);
    }

    public List<Pair<LauncherManifest, LauncherInstanceDetails>> getInstanceComponents() {
        return instanceComponents;
    }

    public void reloadInstanceComponents() throws FileLoadException {
        instanceComponents = reloadComponents(getInstanceManifest(), getLauncherDetails().getInstancesDir(), LauncherManifestType.INSTANCE_COMPONENT, LauncherInstanceDetails.class, null);
    }


    public LauncherManifest getJavaManifest() {
        return javaManifest;
    }

    public void reloadJavaManifest() throws FileLoadException {
        javaManifest = reloadManifest(getLauncherDetails().getJavasDir(), LauncherManifestType.JAVAS);
    }

    public List<LauncherManifest> getJavaComponents() {
        return javaComponents;
    }

    public void reloadJavaComponents() throws FileLoadException {
        javaComponents = reloadComponents(getJavaManifest(), getLauncherDetails().getJavasDir(), LauncherManifestType.JAVA_COMPONENT, null);
    }

    public LauncherManifest getOptionsManifest() {
        return optionsManifest;
    }

    public void reloadOptionsManifest() throws FileLoadException {
        optionsManifest = reloadManifest(getLauncherDetails().getOptionsDir(), LauncherManifestType.OPTIONS);
    }

    public List<LauncherManifest> getOptionsComponents() {
        return optionsComponents;
    }

    public void reloadOptionsComponents() throws FileLoadException {
        optionsComponents = reloadComponents(getOptionsManifest(), getLauncherDetails().getOptionsDir(), LauncherManifestType.OPTIONS_COMPONENT, null);
    }

    public LauncherManifest getResourcepackManifest() {
        return resourcepackManifest;
    }

    public void reloadResourcepackManifest() throws FileLoadException {
        resourcepackManifest = reloadManifest(getLauncherDetails().getResourcepacksDir(), LauncherManifestType.RESOURCEPACKS);
    }

    public List<LauncherManifest> getResourcepackComponents() {
        return resourcepackComponents;
    }

    public void reloadResourcepackComponents() throws FileLoadException {
        resourcepackComponents = reloadComponents(getResourcepackManifest(), getLauncherDetails().getResourcepacksDir(), LauncherManifestType.RESOURCEPACKS_COMPONENT, null);
    }

    public LauncherManifest getVersionManifest() {
        return versionManifest;
    }

    public void reloadVersionManifest() throws FileLoadException {
        versionManifest = reloadManifest(getLauncherDetails().getVersionDir(), LauncherManifestType.VERSIONS);
    }

    public List<Pair<LauncherManifest, LauncherVersionDetails>> getVersionComponents() {
        return versionComponents;
    }

    public void reloadVersionComponents() throws FileLoadException {
        versionComponents = reloadComponents(getVersionManifest(), getLauncherDetails().getVersionDir(), LauncherManifestType.VERSION_COMPONENT, LauncherVersionDetails.class, null);
    }

    public LauncherManifest reloadManifest(String relativePath, LauncherManifestType expectedType) throws FileLoadException {
        return reloadManifest(relativePath, LauncherApplication.config.MANIFEST_FILE_NAME, expectedType);
    }

    public LauncherManifest reloadManifest(String relativePath, String filename, LauncherManifestType expectedType) throws FileLoadException {
        String versionFile;
        try {
            versionFile = FileUtil.loadFile(FormatUtil.relativeFilePath(relativePath, filename));
        } catch (IOException e) {
            throw new FileLoadException("Unable to load " + expectedType.name().toLowerCase() + " manifest: file error", e);
        }
        if(versionFile == null) {
            throw new FileLoadException("Unable to load " + expectedType.name().toLowerCase() + " manifest: file error");
        }
        LauncherManifest out = LauncherManifest.fromJson(versionFile, getLauncherDetails().getTypeConversion());
        if(out == null || out.getType() != expectedType) {
            throw new FileLoadException("Unable to load " + expectedType.name().toLowerCase() + " manifest: incorrect contents");
        }
        out.setDirectory(FormatUtil.relativeDirPath(relativePath));
        LOGGER.debug("Loaded " + expectedType.name().toLowerCase() + " manifest");
        return out;
    }

    public List<LauncherManifest> reloadComponents(LauncherManifest parentManifest, String parentPath, LauncherManifestType expectedType, String fallbackPath) throws FileLoadException {
        return reloadComponents(parentManifest, parentPath, LauncherApplication.config.MANIFEST_FILE_NAME, expectedType, fallbackPath);
    }

    public List<LauncherManifest> reloadComponents(LauncherManifest parentManifest, String parentPath, String filename, LauncherManifestType expectedType, String fallbackPath) throws FileLoadException {
        List<LauncherManifest> out = new ArrayList<>();
        for(String c : parentManifest.getComponents()) {
            try {
                addComponent(out, FormatUtil.relativeDirPath(parentPath, parentManifest.getPrefix() + "_" + c), filename, expectedType, c, LauncherApplication.config.BASE_DIR + fallbackPath);
            } catch (FileLoadException e) {
                throw new FileLoadException("Unable to load " + expectedType.name().toLowerCase() + " components: component error: id=" + c);
            }
        }
        LOGGER.debug("Loaded " + expectedType.name().toLowerCase() + " components");
        return out;
    }

    private void addComponent(List<LauncherManifest> list, String path, String filename, LauncherManifestType expectedType, String expectedId, String fallbackPath) throws FileLoadException {
        String manifestFile;
        try {
            manifestFile = FileUtil.loadFile(path + filename);
        } catch (IOException e) {
            if(fallbackPath == null) {
                throw new FileLoadException("Unable to load " + expectedType.name().toLowerCase() + " component: file error: id=" + expectedId, e);
            }
            LOGGER.debug("Falling back to fallback path loading " + expectedType.name().toLowerCase() + " component: file error: id=" + expectedId);
            addComponent(list, fallbackPath, filename, expectedType, expectedId, null);
            return;
        }

        LauncherManifest manifest = LauncherManifest.fromJson(manifestFile, getLauncherDetails().getTypeConversion());
        if(manifest == null || manifest.getType() == null || manifest.getType() != expectedType || manifest.getId() == null || !manifest.getId().equals(expectedId)) {
            if(fallbackPath == null) {
                throw new FileLoadException("Unable to load " + expectedType.name().toLowerCase() + " component: incorrect contents: id=" + expectedId);
            }
            LOGGER.debug("Falling back to fallback path loading " +  expectedType.name().toLowerCase() + " component id=" + expectedId);
            addComponent(list, fallbackPath, filename, expectedType, expectedId, null);
            return;
        }
        manifest.setDirectory(path);
        list.add(manifest);
    }

    public <T extends GenericJsonParsable> List<Pair<LauncherManifest, T>> reloadComponents(LauncherManifest parentManifest, String parentPath, LauncherManifestType expectedType, Class<T> targetClass, String fallbackPath) throws FileLoadException {
        return reloadComponents(parentManifest, parentPath, LauncherApplication.config.MANIFEST_FILE_NAME, expectedType, targetClass, fallbackPath);
    }

    public <T extends GenericJsonParsable> List<Pair<LauncherManifest, T>> reloadComponents(LauncherManifest parentManifest, String parentPath, String filename, LauncherManifestType expectedType, Class<T> targetClass, String fallbackPath) throws FileLoadException {
        if(parentManifest == null || parentManifest.getPrefix() == null || parentManifest.getComponents() == null) {
            throw new FileLoadException("Unable to load " + expectedType.name().toLowerCase() + " components: invalid configuration");
        }
        List<Pair<LauncherManifest, T>> out = new ArrayList<>();
        List<FileLoadException> exceptionQueue = new ArrayList<>();
        for(String c : parentManifest.getComponents()) {
            try {
                addComponent(out, FormatUtil.relativeDirPath(parentPath, parentManifest.getPrefix() + "_" + c), filename, expectedType, targetClass, LauncherApplication.config.BASE_DIR + fallbackPath, c);
            } catch (FileLoadException e) {
                exceptionQueue.add(e);
            }
        }
        if(!exceptionQueue.isEmpty()) {
            throw new FileLoadException("Unable to load " + exceptionQueue.size() + " components: component error", exceptionQueue.get(0));
        }
        LOGGER.debug("Loaded " + expectedType.name().toLowerCase() + " components");
        return out;
    }


    private <T extends GenericJsonParsable> void addComponent(List<Pair<LauncherManifest, T>> list, String path, String filename, LauncherManifestType expectedType, Class<T> targetClass, String fallbackPath, String expectedId) throws FileLoadException {
        String manifestFile;
        try {
            manifestFile = FileUtil.loadFile(path + filename);
        } catch (IOException e) {
            if(fallbackPath == null) {
                throw new FileLoadException("Unable to load " + expectedType.name().toLowerCase() + " component: file error: id=" + expectedId, e);
            }
            LOGGER.debug("Falling back to fallback path loading " + expectedType.name().toLowerCase() + " component: file error: id=" + expectedId);
            addComponent(list, fallbackPath, filename, expectedType, targetClass, null, expectedId);
            return;
        }

        LauncherManifest manifest = LauncherManifest.fromJson(manifestFile, getLauncherDetails().getTypeConversion());
        if(manifest == null || manifest.getType() == null || manifest.getType() != expectedType || manifest.getId() == null || !manifest.getId().equals(expectedId) || manifest.getDetails() == null) {
            if(fallbackPath == null) {
                throw new FileLoadException("Unable to load " + expectedType.name().toLowerCase() + " component: incorrect contents: id=" + expectedId);
            }
            LOGGER.debug("Falling back to fallback path loading " +  expectedType.name().toLowerCase() + " component id=" + expectedId);
            addComponent(list, fallbackPath, filename, expectedType, targetClass, null, expectedId);
            return;
        }
        manifest.setDirectory(path);
        String detailsFile = null;
        try {
            detailsFile = FileUtil.loadFile(path + manifest.getDetails());
        } catch (IOException e) {
            if(fallbackPath == null) {
                throw new FileLoadException("Unable to load " + expectedType.name().toLowerCase() + " component details: file error: id=" + expectedId, e);
            }
            LOGGER.debug("Falling back to fallback path loading " +  expectedType.name().toLowerCase() + " component id=" + expectedId);
            addComponent(list, fallbackPath, filename, expectedType, targetClass, null, expectedId);
        }
        T details = GenericJsonParsable.fromJson(detailsFile, targetClass);
        if(details == null) {
            if(fallbackPath == null) {
                throw new FileLoadException("Unable to load " + expectedType.name().toLowerCase() + " component details: incorrect contents: id=" + expectedId);
            }
            LOGGER.debug("Falling back to fallback path loading " +  expectedType.name().toLowerCase() + " component id=" + expectedId);
            addComponent(list, fallbackPath, filename, expectedType, targetClass, null, expectedId);
            return;
        }
        list.add(new Pair<>(manifest, details));
    }
}
