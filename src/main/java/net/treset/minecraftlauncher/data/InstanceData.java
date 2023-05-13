package net.treset.minecraftlauncher.data;

import javafx.util.Pair;
import net.treset.mc_version_loader.launcher.*;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.util.FormatUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InstanceData {
    private static Logger LOGGER = LogManager.getLogger(InstanceData.class);

    private LauncherDetails launcherDetails;
    private String launcherDetailsFile;
    private Pair<LauncherManifest, LauncherInstanceDetails> instance;
    private List<Pair<LauncherManifest, LauncherVersionDetails>> versionComponents;
    private LauncherManifest javaComponent = null;
    LauncherManifest optionsComponent = null;
    LauncherManifest resourcepacksComponent = null;
    LauncherManifest savesComponent = null;
    Pair<LauncherManifest, LauncherModsDetails> modsComponent = null;
    String gameDataDir;
    String assetsDir;
    String librariesDir;
    String modsPrefix;
    String savesPrefix;
    List<String> gameDataExcludedFiles;

    public static InstanceData of(Pair<LauncherManifest, LauncherInstanceDetails> instance, LauncherFiles files) {
        if(!files.reloadAll()) {
            LOGGER.warn("Unable to prepare launch resources: file reload failed");
            return null;
        }

        List<Pair<LauncherManifest, LauncherVersionDetails>> versionComponents = new ArrayList<>();
        Pair<LauncherManifest, LauncherVersionDetails> currentComponent = null;
        for (Pair<LauncherManifest, LauncherVersionDetails> v : files.getVersionComponents()) {
            if (Objects.equals(v.getKey().getId(), instance.getValue().getVersionComponent())) {
                currentComponent = v;
                break;
            }
        }
        if(currentComponent == null) {
            LOGGER.warn("Unable to prepare launch resources: unable to find version component: versionId=" + instance.getValue().getVersionComponent());
            return null;
        }
        versionComponents.add(currentComponent);

        while(currentComponent.getValue().getDepends() != null && !currentComponent.getValue().getDepends().isBlank()) {
            boolean found = false;
            for (Pair<LauncherManifest, LauncherVersionDetails> v : files.getVersionComponents()) {
                if (Objects.equals(v.getKey().getId(), currentComponent.getValue().getDepends())) {
                    currentComponent = v;
                    found = true;
                    break;
                }
            }
            if(!found) {
                LOGGER.warn("Unable to prepare launch resources: unable to find dependent version component: versionId=" + currentComponent.getValue().getDepends());
                return null;
            }
            versionComponents.add(currentComponent);
        }


        LauncherManifest javaComponent = null;
        for(Pair<LauncherManifest, LauncherVersionDetails> v : versionComponents) {
            if(v.getValue().getJava() != null && !v.getValue().getJava().isBlank()) {
                for (LauncherManifest j : files.getJavaComponents()) {
                    if (Objects.equals(j.getId(), v.getValue().getJava())) {
                        javaComponent = j;
                        break;
                    }
                }
                break;
            }
        }
        if(javaComponent == null) {
            LOGGER.warn("Unable to prepare launch resources: unable to find suitable java component");
            return null;
        }
        LauncherManifest optionsComponent = null;
        for(LauncherManifest o : files.getOptionsComponents()) {
            if(Objects.equals(o.getId(), instance.getValue().getOptionsComponent())) {
                optionsComponent = o;
                break;
            }
        }
        if(optionsComponent == null) {
            LOGGER.warn("Unable to prepare launch resources: unable to find options component: optionsId=" + instance.getValue().getOptionsComponent());
            return null;
        }
        LauncherManifest resourcepacksComponent = null;
        for(LauncherManifest r : files.getResourcepackComponents()) {
            if(Objects.equals(r.getId(), instance.getValue().getResourcepacksComponent())) {
                resourcepacksComponent = r;
                break;
            }
        }
        if(resourcepacksComponent == null) {
            LOGGER.warn("Unable to prepare launch resources: unable to find resourcepacks component: resourcepacksId=" + instance.getValue().getResourcepacksComponent());
            return null;
        }
        LauncherManifest savesComponent = null;
        for(LauncherManifest s : files.getSavesComponents()) {
            if(Objects.equals(s.getId(), instance.getValue().getSavesComponent())) {
                savesComponent = s;
                break;
            }
        }
        if(savesComponent == null) {
            LOGGER.warn("Unable to prepare launch resources: unable to find saves component: savesId=" + instance.getValue().getSavesComponent());
            return null;
        }
        Pair<LauncherManifest, LauncherModsDetails> modsComponent = null;
        if(instance.getValue().getModsComponent() != null && !instance.getValue().getModsComponent().isBlank()) {
            for(Pair<LauncherManifest, LauncherModsDetails> m : files.getModsComponents()) {
                if(Objects.equals(m.getKey().getId(), instance.getValue().getModsComponent())) {
                    modsComponent = m;
                    break;
                }
            }
            if(modsComponent == null) {
                LOGGER.warn("Unable to prepare launch resources: unable to find mods component: modsId=" + instance.getValue().getModsComponent());
                return null;
            }
        }

        ArrayList<String> gameDataExcludedFiles = new ArrayList<>();
        for(String c : files.getGameDetailsManifest().getComponents()) {
            gameDataExcludedFiles.add(FormatUtil.toRegexPattern(c));
        }
        gameDataExcludedFiles.add(FormatUtil.toRegexPattern(files.getModsManifest().getPrefix()) + ".*");
        gameDataExcludedFiles.add(FormatUtil.toRegexPattern(files.getSavesManifest().getPrefix()) + ".*");

        return new InstanceData(
                files.getLauncherDetails(),
                files.getMainManifest().getDirectory() + files.getMainManifest().getDetails(),
                instance,
                versionComponents,
                javaComponent,
                optionsComponent,
                resourcepacksComponent,
                savesComponent,
                modsComponent,
                LauncherApplication.config.BASE_DIR + files.getLauncherDetails().getGamedataDir() + "/",
                LauncherApplication.config.BASE_DIR + files.getLauncherDetails().getAssetsDir() + "/",
                LauncherApplication.config.BASE_DIR + files.getLauncherDetails().getLibrariesDir() + "/",
                files.getModsManifest().getPrefix(),
                files.getSavesManifest().getPrefix(),
                gameDataExcludedFiles
        );
    }

    public InstanceData(LauncherDetails launcherDetails, String launcherDetailsFile, Pair<LauncherManifest, LauncherInstanceDetails> instance, List<Pair<LauncherManifest, LauncherVersionDetails>> versionComponents, LauncherManifest javaComponent, LauncherManifest optionsComponent, LauncherManifest resourcepacksComponent, LauncherManifest savesComponent, Pair<LauncherManifest, LauncherModsDetails> modsComponent, String gameDataDir, String assetsDir, String librariesDir, String modsPrefix, String savesPrefix, List<String> gameDataExcludedFiles) {
        this.launcherDetails = launcherDetails;
        this.launcherDetailsFile = launcherDetailsFile;
        this.instance = instance;
        this.versionComponents = versionComponents;
        this.javaComponent = javaComponent;
        this.optionsComponent = optionsComponent;
        this.resourcepacksComponent = resourcepacksComponent;
        this.savesComponent = savesComponent;
        this.modsComponent = modsComponent;
        this.gameDataDir = gameDataDir;
        this.assetsDir = assetsDir;
        this.librariesDir = librariesDir;
        this.modsPrefix = modsPrefix;
        this.savesPrefix = savesPrefix;
        this.gameDataExcludedFiles = gameDataExcludedFiles;
    }

    public boolean setActive(boolean active) {
        launcherDetails.setActiveInstance(active ? instance.getKey().getId()  : null);
        if(!launcherDetails.writeToFile(launcherDetailsFile)) {
            LOGGER.warn("Unable to launch game: unable to write launcher details");
            return false;
        }
        return true;
    }

    public Pair<LauncherManifest, LauncherInstanceDetails> getInstance() {
        return instance;
    }

    public void setInstance(Pair<LauncherManifest, LauncherInstanceDetails> instance) {
        this.instance = instance;
    }
    public List<Pair<LauncherManifest, LauncherVersionDetails>> getVersionComponents() {
        return versionComponents;
    }

    public void setVersionComponents(List<Pair<LauncherManifest, LauncherVersionDetails>> versionComponents) {
        this.versionComponents = versionComponents;
    }

    public LauncherManifest getJavaComponent() {
        return javaComponent;
    }

    public void setJavaComponent(LauncherManifest javaComponent) {
        this.javaComponent = javaComponent;
    }

    public LauncherManifest getOptionsComponent() {
        return optionsComponent;
    }

    public void setOptionsComponent(LauncherManifest optionsComponent) {
        this.optionsComponent = optionsComponent;
    }

    public LauncherManifest getResourcepacksComponent() {
        return resourcepacksComponent;
    }

    public void setResourcepacksComponent(LauncherManifest resourcepacksComponent) {
        this.resourcepacksComponent = resourcepacksComponent;
    }

    public LauncherManifest getSavesComponent() {
        return savesComponent;
    }

    public void setSavesComponent(LauncherManifest savesComponent) {
        this.savesComponent = savesComponent;
    }

    public Pair<LauncherManifest, LauncherModsDetails> getModsComponent() {
        return modsComponent;
    }

    public void setModsComponent(Pair<LauncherManifest, LauncherModsDetails> modsComponent) {
        this.modsComponent = modsComponent;
    }

    public String getGameDataDir() {
        return gameDataDir;
    }

    public void setGameDataDir(String gameDataDir) {
        this.gameDataDir = gameDataDir;
    }

    public String getAssetsDir() {
        return assetsDir;
    }

    public void setAssetsDir(String assetsDir) {
        this.assetsDir = assetsDir;
    }

    public String getLibrariesDir() {
        return librariesDir;
    }

    public void setLibrariesDir(String librariesDir) {
        this.librariesDir = librariesDir;
    }

    public String getModsPrefix() {
        return modsPrefix;
    }

    public void setModsPrefix(String modsPrefix) {
        this.modsPrefix = modsPrefix;
    }

    public String getSavesPrefix() {
        return savesPrefix;
    }

    public void setSavesPrefix(String savesPrefix) {
        this.savesPrefix = savesPrefix;
    }

    public List<String> getGameDataExcludedFiles() {
        return gameDataExcludedFiles;
    }

    public void setGameDataExcludedFiles(List<String> gameDataExcludedFiles) {
        this.gameDataExcludedFiles = gameDataExcludedFiles;
    }

    public LauncherDetails getLauncherDetails() {
        return launcherDetails;
    }

    public void setLauncherDetails(LauncherDetails launcherDetails) {
        this.launcherDetails = launcherDetails;
    }

    public String getLauncherDetailsFile() {
        return launcherDetailsFile;
    }

    public void setLauncherDetailsFile(String launcherDetailsFile) {
        this.launcherDetailsFile = launcherDetailsFile;
    }
}
