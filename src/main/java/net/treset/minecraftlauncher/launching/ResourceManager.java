package net.treset.minecraftlauncher.launching;

import net.treset.mc_version_loader.format.FormatUtils;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.minecraftlauncher.config.Config;
import net.treset.minecraftlauncher.data.InstanceData;
import net.treset.minecraftlauncher.util.FileUtil;
import net.treset.minecraftlauncher.util.FormatUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResourceManager {
    private static Logger LOGGER = LogManager.getLogger(ResourceManager.class);
    
    private InstanceData instanceData;
    private boolean prepared = false;

    public ResourceManager(InstanceData instanceData) {
        this.instanceData = instanceData;
    }

    public boolean prepareResources() {
        if(!addIncludedFiles(List.of(instanceData.getInstance().getKey(), instanceData.getOptionsComponent(), instanceData.getResourcepacksComponent(), instanceData.getSavesComponent(), instanceData.getModsComponent().getKey()))) {
            LOGGER.warn("Unable to prepare launch resources: included files copy failed");
            return false;
        }

        if(!renameComponents()) {
            LOGGER.warn("Unable to prepare launch resources: component rename failed");
            return false;
        }

        if(!instanceData.setActive(true)) {
            LOGGER.warn("Unable to prepare launch resources: unable to set instance active");
            return false;
        }

        LOGGER.info("Prepared resources for launch, instance=" + instanceData.getInstance().getKey().getId());
        return true;
    }

    public boolean cleanupGameFiles() {
        if(!undoRenameComponents()) {
            LOGGER.warn("Unable to cleanup launch resources: component rename failed");
            return false;
        }

        ArrayList<File> gameDataFilesList = getGameDataFiles();
        if(gameDataFilesList == null) {
            LOGGER.warn("Unable to cleanup launch resources: unable to get game data files");
            return false;
        }

        if(!removeExcludedFiles(gameDataFilesList)) {
            LOGGER.warn("Unable to cleanup launch resources: unable to find excluded files");
            return false;
        }

        if(!removeIncludedFiles(List.of(instanceData.getSavesComponent(), instanceData.getModsComponent().getKey(), instanceData.getOptionsComponent(), instanceData.getResourcepacksComponent()), gameDataFilesList)) {
            LOGGER.warn("Unable to cleanup launch resources: unable to remove included files");
            return false;
        }

        if(!removeRemainingFiles(gameDataFilesList)) {
            LOGGER.warn("Unable to cleanup launch resources: unable to remove remaining files");
            return false;
        }

        if(!instanceData.setActive(false)) {
            LOGGER.warn("Unable to cleanup launch resources: unable to set instance inactive");
            return false;
        }

        LOGGER.info("Game files cleaned up");
        return true;
    }

    private boolean renameComponents() {
        try {
            Files.move(Path.of(instanceData.getSavesComponent().getDirectory()), Path.of(instanceData.getGameDataDir() + "saves"));
        } catch (IOException e) {
            LOGGER.warn("Unable to prepare launch resources: rename saves file failed", e);
            return false;
        }
        instanceData.getSavesComponent().setDirectory(instanceData.getGameDataDir() + "saves/");

        if(instanceData.getModsComponent() != null) {
            try {
                Files.move(Path.of(instanceData.getModsComponent().getKey().getDirectory()), Path.of(instanceData.getGameDataDir() + "mods"));
            } catch (IOException e) {
                LOGGER.warn("Unable to prepare launch resources: rename mods file failed", e);
                return false;
            }
            instanceData.getModsComponent().getKey().setDirectory(instanceData.getGameDataDir() + "mods/");
        }
        return true;
    }

    private boolean addIncludedFiles(List<LauncherManifest> components) {
        for(LauncherManifest c : components) {
            if(c == null) {
                continue;
            }
            if(!addIncludedFiles(c)) {
                LOGGER.warn("Unable to prepare launch resources: included files copy for instance failed");
                return false;
            }
        }
        return true;
    }

    private boolean addIncludedFiles(LauncherManifest manifest) {
        if(manifest == null) {
            return false;
        }
        if(manifest.getIncludedFiles() != null) {
            File includedFilesDir = new File(manifest.getDirectory() + Config.INCLUDED_FILES_DIR);
            if(!includedFilesDir.isDirectory()) {
                LOGGER.warn("Unable to move included files: folder doesn't exist: manifestId=" + manifest.getId());
                return false;
            }
            File[] files = includedFilesDir.listFiles();
            if(files == null) {
                LOGGER.warn("Unable to move included files: unable to get files: manifestId=" + manifest.getId());
                return false;
            }
            boolean success = true;
            for(File f : files) {
                if(f.isFile()) {
                    try {
                        Files.copy(Path.of(f.getPath()), Path.of(instanceData.getGameDataDir() + f.getName()), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        LOGGER.warn("Unable to move included files: unable to copy file: manifestId=" + manifest.getId(), e);
                        success = false;
                    }
                }
                else if(f.isDirectory() && !FileUtil.copyDirectory(f.getPath(), instanceData.getGameDataDir() + f.getName(), StandardCopyOption.REPLACE_EXISTING)){
                    LOGGER.warn("Unable to move included files: unable to copy directory: manifestId=" + manifest.getId());
                    success = false;
                }
            }
            return success;
        }
        return true;
    }

    private boolean undoRenameComponents() {
        try {
            Files.move(Path.of(instanceData.getSavesComponent().getDirectory()), Path.of(instanceData.getGameDataDir() + instanceData.getSavesPrefix() + "_" + instanceData.getSavesComponent().getId()));
        } catch (IOException e) {
            LOGGER.warn("Unable to cleanup launch resources: rename saves file failed", e);
            return false;
        }
        instanceData.getSavesComponent().setDirectory(instanceData.getGameDataDir() + instanceData.getSavesPrefix() + "_" + instanceData.getSavesComponent().getId() + "/");

        if(instanceData.getModsComponent() != null) {
            try {
                Files.move(Path.of(instanceData.getModsComponent().getKey().getDirectory()), Path.of(instanceData.getGameDataDir() + instanceData.getModsPrefix() + "_" + instanceData.getModsComponent().getKey().getId()));
            } catch (IOException e) {
                LOGGER.warn("Unable to cleanup launch resources: rename mods file failed", e);
                return false;
            }
            instanceData.getModsComponent().getKey().setDirectory(instanceData.getGameDataDir() + instanceData.getModsPrefix() + "_" + instanceData.getModsComponent().getKey().getId() + "/");
        }
        return true;
    }

    private ArrayList<File> getGameDataFiles() {
        File gameDataDir = new File(instanceData.getGameDataDir());
        if(!gameDataDir.isDirectory()) {
            LOGGER.warn("Unable to cleanup launch resources: game data directory not found");
            return null;
        }

        File[] gameDataFiles = gameDataDir.listFiles();
        if(gameDataFiles == null) {
            LOGGER.warn("Unable to cleanup launch resources: game data files not found");
            return null;
        }

        return new ArrayList<>(Arrays.asList(gameDataFiles));
    }

    private boolean removeExcludedFiles(ArrayList<File> files) {
        List<File> toRemove = new ArrayList<>();
        for(File f : files) {
            if(f.getName().equals(Config.MANIFEST_FILE_NAME) || FormatUtil.matchesAny(f.getName(), instanceData.getGameDataExcludedFiles())) {
                toRemove.add(f);
            }
        }
        files.removeAll(toRemove);
        return true;
    }

    private boolean removeIncludedFiles(List<LauncherManifest> components, ArrayList<File> files) {
        for(LauncherManifest component : components) {
            if(component.getIncludedFiles() == null || component.getIncludedFiles().isEmpty()) {
                continue;
            }
            if(!removeIncludedFiles(component, files)) {
                LOGGER.warn("Unable to remove included files from component: component_type=" + component.getType().name().toLowerCase() + " component=" + component.getId());
                return false;
            }
        }
        return true;
    }

    private boolean removeIncludedFiles(LauncherManifest component, ArrayList<File> files) {
        File includedFilesDir = new File(component.getDirectory() + Config.INCLUDED_FILES_DIR);
        if(includedFilesDir.exists()) {
            if(!FileUtil.deleteDir(includedFilesDir)) {
                LOGGER.warn("Unable to remove included files: unable to delete included files directory: component_type=" + component.getType().name().toLowerCase() + " component=" + component.getId());
                return false;
            }
        }
        if(!includedFilesDir.mkdirs()) {
            LOGGER.warn("Unable to remove included files: unable to create included files directory: component_type=" + component.getType().name().toLowerCase() + " component=" + component.getId());
            return false;
        }
        List<File> toRemove = new ArrayList<>();
        for(File f : files) {
            String fName = f.isDirectory() ? f.getName() + "/" : f.getName();
            boolean found = false;
            for(String i : component.getIncludedFiles()) {
                if(FormatUtils.matches(fName, i)) {
                    try {
                        Files.move(Path.of(f.getPath()), Path.of(component.getDirectory() + Config.INCLUDED_FILES_DIR + "/" + f.getName()), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        LOGGER.warn("Unable to remove included file: file=" + f.getAbsolutePath(), e);
                        return false;
                    }
                    found = true;
                    break;
                }
            }
            if(found) {
                toRemove.add(f);
            }
        }
        files.removeAll(toRemove);
        return true;
    }

    private boolean removeRemainingFiles(ArrayList<File> files) {
        File includedFilesDir = new File(instanceData.getInstance().getKey().getDirectory() + Config.INCLUDED_FILES_DIR);
        if(includedFilesDir.exists()) {
            if(!FileUtil.deleteDir(includedFilesDir)) {
                LOGGER.warn("Unable to cleanup launch resources: unable to delete instance included files directory");
                return false;
            }
        }
        if(!includedFilesDir.mkdir()) {
            LOGGER.warn("Unable to cleanup launch resources: unable to create instance included files directory");
            return false;
        }
        for(File f : files) {
            if(instanceData.getInstance().getValue().getIgnoredFiles().contains(f.getName())) {
                try {
                    Files.delete(Path.of(f.getPath()));
                } catch (IOException e) {
                    LOGGER.warn("Unable to cleanup launch resources: unable to delete file: " + f.getPath(), e);
                    return false;
                }
            }
            else {
                try {
                    Files.move(Path.of(f.getPath()), Path.of(instanceData.getInstance().getKey().getDirectory() + Config.INCLUDED_FILES_DIR + "/" + f.getName()), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    LOGGER.warn("Unable to cleanup launch resources: unable to move file: " + f.getPath(), e);
                    return false;
                }
            }
        }
        return true;
    }

    public InstanceData getInstanceData() {
        return instanceData;
    }

    public void setInstanceData(InstanceData instanceData) {
        this.instanceData = instanceData;
    }

    public boolean isPrepared() {
        return prepared;
    }

    public void setPrepared(boolean prepared) {
        this.prepared = prepared;
    }
}
