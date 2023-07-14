package net.treset.minecraftlauncher.launching;

import net.treset.mc_version_loader.format.FormatUtils;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.data.InstanceData;
import net.treset.minecraftlauncher.util.FileUtil;
import net.treset.minecraftlauncher.util.FormatUtil;
import net.treset.minecraftlauncher.util.exception.GameResourceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResourceManager {
    private static final Logger LOGGER = LogManager.getLogger(ResourceManager.class);
    
    private InstanceData instanceData;
    private boolean prepared = false;

    public ResourceManager(InstanceData instanceData) {
        this.instanceData = instanceData;
    }

    public void prepareResources() throws GameResourceException {
        addIncludedFiles(List.of(instanceData.getInstance().getKey(), instanceData.getOptionsComponent(), instanceData.getResourcepacksComponent(), instanceData.getSavesComponent()));
        if(instanceData.getModsComponent() != null) {
            addIncludedFiles(List.of(instanceData.getModsComponent().getKey()));
        }
        renameComponents();

        try {
            instanceData.setActive(true);
        } catch (IOException e) {
            throw new GameResourceException("Failed to prepare resources: unable to set instance active", e);
        }

        LOGGER.info("Prepared resources for launch, instance=" + instanceData.getInstance().getKey().getId());
    }

    public void setLastPlayedTime() throws IOException {
        LOGGER.debug("Setting last played time: instance={}", instanceData.getInstance().getKey().getId());
        if(instanceData.getInstance().getValue() == null) {
            throw new IOException("Unable to set last played time: instance details are null");
        }
        instanceData.getInstance().getValue().setLastPlayedTime(LocalDateTime.now());
        instanceData.getInstance().getValue().writeToFile(instanceData.getInstance().getKey().getDirectory() + instanceData.getInstance().getKey().getDetails());
        LOGGER.debug("Set last played time: instance={}", instanceData.getInstance().getKey().getId());
    }

    public void addPlayDuration(long duration) throws IOException {
        LOGGER.debug("Adding play duration: instance={}, duration={}", instanceData.getInstance().getKey().getId(), duration);
        if(instanceData.getInstance().getValue() == null) {
            throw new IOException("Unable to set last played time: instance details are null");
        }
        long oldTime = instanceData.getInstance().getValue().getTotalTime();
        instanceData.getInstance().getValue().setTotalTime(oldTime + duration);
        instanceData.getInstance().getValue().writeToFile(instanceData.getInstance().getKey().getDirectory() + instanceData.getInstance().getKey().getDetails());
        LOGGER.debug("Added play duration: instance={}, duration={}, totalTime={}", instanceData.getInstance().getKey().getId(), duration, instanceData.getInstance().getValue().getTotalTime());
    }

    public void cleanupGameFiles() throws GameResourceException {
        try {
            LOGGER.debug("Cleaning up game files: instance={}", instanceData.getInstance().getKey().getId());
            try {
                undoRenameComponents();
                ArrayList<File> gameDataFilesList = getGameDataFiles();
                removeExcludedFiles(gameDataFilesList);
                removeIncludedFiles(List.of(instanceData.getSavesComponent()), gameDataFilesList);
                if (instanceData.getModsComponent() != null) {
                    removeIncludedFiles(List.of(instanceData.getModsComponent().getKey()), gameDataFilesList);
                }
                removeIncludedFiles(List.of(instanceData.getOptionsComponent(), instanceData.getResourcepacksComponent()), gameDataFilesList);
                removeRemainingFiles(gameDataFilesList);
            } catch (GameResourceException e) {
                throw new GameResourceException("Unable to cleanup game files", e);
            }

            try {
                instanceData.setActive(false);
            } catch (IOException e) {
                throw new GameResourceException("Unable to cleanup game files: unable to set instance inactive", e);
            }
        } catch (Exception e) {
            throw new GameResourceException("Unable to cleanup game files", e);
        }

        LOGGER.info("Game files cleaned up");
    }

    private void renameComponents() throws GameResourceException {
        LOGGER.debug("Renaming components: instance={}", instanceData.getInstance().getKey().getId());
        try {
            Files.move(Path.of(instanceData.getSavesComponent().getDirectory()), Path.of(instanceData.getGameDataDir() + "saves"), StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new GameResourceException("Unable to rename saves file", e);
        }
        instanceData.getSavesComponent().setDirectory(FormatUtil.absoluteDirPath(instanceData.getGameDataDir(), "saves"));

        if(instanceData.getModsComponent() != null) {
            try {
                Files.move(Path.of(instanceData.getModsComponent().getKey().getDirectory()), Path.of(instanceData.getGameDataDir() + "mods"), StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                throw new GameResourceException("Unable to rename mods file", e);
            }
            instanceData.getModsComponent().getKey().setDirectory(FormatUtil.absoluteDirPath(instanceData.getGameDataDir(), "mods"));
        }
    }

    private void addIncludedFiles(List<LauncherManifest> components) throws GameResourceException {
        LOGGER.debug("Adding included files: instance={}", instanceData.getInstance().getKey().getId());
        List<GameResourceException> exceptionQueue = new ArrayList<>();
        for(LauncherManifest c : components) {
            if(c == null) {
                continue;
            }
            try {
                addIncludedFiles(c);
            } catch (GameResourceException e) {
                exceptionQueue.add(e);
                LOGGER.warn("Unable to get included files: manifestId=" + c.getId(), e);
            }
        }
        if(!exceptionQueue.isEmpty()) {
            throw new GameResourceException("Unable to get included files for " + exceptionQueue.size() + " components", exceptionQueue.get(0));
        }
        LOGGER.debug("Added included files: instance={}", instanceData.getInstance().getKey().getId());
    }

    private void addIncludedFiles(LauncherManifest manifest) throws GameResourceException {
        if(manifest == null || manifest.getIncludedFiles() == null) {
            throw new GameResourceException("Unable to get included files: unmet requirements");
        }
        LOGGER.debug("Adding included files: {}Id={}", manifest.getType(), manifest.getId());
        File includedFilesDir = new File(manifest.getDirectory() + LauncherApplication.config.INCLUDED_FILES_DIR);
        if(!includedFilesDir.isDirectory()) {
            throw new GameResourceException("Included files directory doesn't exist: manifestId=" + manifest.getId());
        }
        File[] files = includedFilesDir.listFiles();
        if(files == null) {
            throw new GameResourceException("Unable to get included files: manifestId=" + manifest.getId());
        }
        LOGGER.debug("Adding included files: {}Id={}, includedFiles={}", manifest.getType(), manifest.getId(), files);
        List<IOException> exceptionQueue = new ArrayList<>();
        for(File f : files) {
            LOGGER.debug("Adding included file: manifestId={}, file={}", manifest.getId(), f.getName());
            if(f.isFile()) {
                try {
                    Files.copy(Path.of(f.getPath()), Path.of(instanceData.getGameDataDir() + f.getName()), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    exceptionQueue.add(e);
                }
            }
            else if(f.isDirectory()) {
                try {
                    FileUtil.copyDirectory(f.getPath(), instanceData.getGameDataDir() + f.getName(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    exceptionQueue.add(e);
                    LOGGER.warn("Unable to move included files: unable to copy directory: manifestId=" + manifest.getId());
                }
            } else {
                exceptionQueue.add(new IOException("Included files directory contains invalid file type: manifestId=" + manifest.getId()));
            }
        }
        if(!exceptionQueue.isEmpty()) {
            throw new GameResourceException("Unable to move included files: unable to copy " + exceptionQueue.size() + " files", exceptionQueue.get(0));
        }
        LOGGER.debug("Added included files: manifestId={}", manifest.getId());
    }

    private void undoRenameComponents() throws GameResourceException {
        LOGGER.debug("Undoing component renames: instance={}", instanceData.getInstance().getKey().getId());
        try {
            Files.move(Path.of(instanceData.getSavesComponent().getDirectory()), Path.of(instanceData.getGameDataDir() + instanceData.getSavesPrefix() + "_" + instanceData.getSavesComponent().getId()));
        } catch (IOException e) {
            throw new GameResourceException("Unable to cleanup launch resources: rename saves file failed", e);
        }
        instanceData.getSavesComponent().setDirectory(FormatUtil.absoluteDirPath(instanceData.getGameDataDir(), instanceData.getSavesPrefix() + "_" + instanceData.getSavesComponent().getId()));

        if(instanceData.getModsComponent() != null) {
            try {
                Files.move(Path.of(instanceData.getModsComponent().getKey().getDirectory()), Path.of(instanceData.getGameDataDir() + instanceData.getModsPrefix() + "_" + instanceData.getModsComponent().getKey().getId()));
            } catch (IOException e) {
                throw new GameResourceException("Unable to cleanup launch resources: rename mods file failed", e);
            }
            instanceData.getModsComponent().getKey().setDirectory(instanceData.getGameDataDir() + instanceData.getModsPrefix() + "_" + instanceData.getModsComponent().getKey().getId() + "/");
        }
        LOGGER.debug("Undid component renames: instance={}", instanceData.getInstance().getKey().getId());
    }

    private ArrayList<File> getGameDataFiles() throws GameResourceException {
        LOGGER.debug("Getting game data files: instance={}", instanceData.getInstance().getKey().getId());
        File gameDataDir = new File(instanceData.getGameDataDir());
        if(!gameDataDir.isDirectory()) {
            throw new GameResourceException("Unable to cleanup launch resources: game data directory not found");
        }

        File[] gameDataFiles = gameDataDir.listFiles();
        if(gameDataFiles == null) {
            throw new GameResourceException("Unable to cleanup launch resources: unable to get game data files");
        }

        LOGGER.debug("Got game data files: instance={}", instanceData.getInstance().getKey().getId());
        return new ArrayList<>(Arrays.asList(gameDataFiles));
    }

    private void removeExcludedFiles(ArrayList<File> files) {
        LOGGER.debug("Removing excluded files: instance={}, files={}", instanceData.getInstance().getKey().getId(), files);
        List<File> toRemove = new ArrayList<>();
        for(File f : files) {
            if(f.getName().equals(LauncherApplication.config.MANIFEST_FILE_NAME) || FormatUtil.matchesAny(f.getName(), instanceData.getGameDataExcludedFiles())) {
                LOGGER.debug("Removing excluded file: instance={}, file={}", instanceData.getInstance().getKey().getId(), f.getName());
                toRemove.add(f);
            }
        }
        files.removeAll(toRemove);
        LOGGER.debug("Removed excluded files: instance={}", instanceData.getInstance().getKey().getId());
    }

    private void removeIncludedFiles(List<LauncherManifest> components, ArrayList<File> files) throws GameResourceException {
        LOGGER.debug("Removing included files: instance={}, files={}", instanceData.getInstance().getKey().getId(), files);
        List<GameResourceException> exceptionQueue = new ArrayList<>();
        for(LauncherManifest component : components) {
            if(component.getIncludedFiles() == null || component.getIncludedFiles().isEmpty()) {
                LOGGER.debug("No included files: manifestId={}", component.getId());
                continue;
            }
            LOGGER.debug("Removing included files: manifestId={}", component.getId());
            try {
                removeIncludedFiles(component, files);
            } catch (GameResourceException e) {
                exceptionQueue.add(e);
                LOGGER.warn("Unable to remove included files: manifestId=" + component.getId(), e);
            }
        }
        if(!exceptionQueue.isEmpty()) {
            throw new GameResourceException("Unable to remove included files for " + exceptionQueue.size() + " components", exceptionQueue.get(0));
        }
        LOGGER.debug("Removed included files: instance={}", instanceData.getInstance().getKey().getId());
    }

    private void removeIncludedFiles(LauncherManifest component, ArrayList<File> files) throws GameResourceException {
        LOGGER.debug("Removing included files: {}Id={}, includedFiles={}, files={}", component.getType(), component.getId(), component.getIncludedFiles(), files);
        File includedFilesDir = new File(component.getDirectory() + LauncherApplication.config.INCLUDED_FILES_DIR);
        if(includedFilesDir.exists()) {
            try {
                FileUtil.deleteDir(includedFilesDir);
            } catch (IOException e) {
                throw new GameResourceException("Unable to remove included files: unable to delete included files directory: component_type=" + component.getType().name().toLowerCase() + " component=" + component.getId(), e);
            }
        }
        if(!includedFilesDir.mkdirs()) {
            throw new GameResourceException("Unable to remove included files: unable to create included files directory: component_type=" + component.getType().name().toLowerCase() + " component=" + component.getId());
        }
        List<File> toRemove = new ArrayList<>();
        List<IOException> exceptionQueue = new ArrayList<>();
        for(File f : files) {
            String fName = f.isDirectory() ? FormatUtil.absoluteDirPath(f.getName()): FormatUtil.absoluteFilePath(f.getName());
            boolean found = false;
            for(String i : component.getIncludedFiles()) {
                if(FormatUtils.matches(fName, i)) {
                    LOGGER.debug("Removing included file: manifestId={}, file={}, matches={}", component.getId(), fName, i);
                    try {
                        Files.move(Path.of(f.getPath()), Path.of(component.getDirectory() + LauncherApplication.config.INCLUDED_FILES_DIR + "/" + f.getName()), StandardCopyOption.REPLACE_EXISTING);
                        found = true;
                    } catch (IOException e) {
                        exceptionQueue.add(e);
                        LOGGER.warn("Unable to remove included files: unable to move file: component_type=" + component.getType().name().toLowerCase() + " component=" + component.getId() + " file=" + f.getName());
                    }
                    break;
                }
            }
            if(found) {
                toRemove.add(f);
            } else {
                LOGGER.debug("Skipping included file: manifestId={}, file={}", component.getId(), f.getName());
            }
        }
        files.removeAll(toRemove);
        if(!exceptionQueue.isEmpty()) {
            throw new GameResourceException("Unable to remove included files: unable to move " + exceptionQueue.size() + " files: component_type=" + component.getType().name().toLowerCase() + " component=" + component.getId(), exceptionQueue.get(0));
        }
        LOGGER.debug("Removed included files: manifestId={}", component.getId());
    }

    private void removeRemainingFiles(ArrayList<File> files) throws GameResourceException {
        LOGGER.debug("Removing remaining files: instance={}, files={}", instanceData.getInstance().getKey().getId(), files);
        File includedFilesDir = new File(instanceData.getInstance().getKey().getDirectory() + LauncherApplication.config.INCLUDED_FILES_DIR);
        if(includedFilesDir.exists()) {
            try {
                FileUtil.deleteDir(includedFilesDir);
            } catch (IOException e) {
                throw new GameResourceException("Unable to cleanup launch resources: unable to delete instance included files directory", e);
            }
        }
        if(!includedFilesDir.mkdir()) {
            throw new GameResourceException("Unable to cleanup launch resources: unable to create instance included files directory");
        }
        List<IOException> exceptionQueue = new ArrayList<>();
        for(File f : files) {
            if(FormatUtil.matchesAny(f.getName(), instanceData.getInstance().getValue().getIgnoredFiles())) {
                LOGGER.debug("Deleting ignored file: instance={}, file={}", instanceData.getInstance().getKey().getId(), f.getName());
                try {
                    if(f.isDirectory()) {
                        FileUtil.deleteDir(f);
                    } else {
                        Files.delete(Path.of(f.getPath()));
                    }
                } catch (IOException e) {
                    exceptionQueue.add(e);
                    LOGGER.warn("Unable to cleanup launch resources: unable to delete non-included file: " + f.getName());
                }
            }
            else {
                LOGGER.debug("Moving non-included file: instance={}, file={}", instanceData.getInstance().getKey().getId(), f.getName());
                try {
                    Files.move(Path.of(f.getPath()), Path.of(FormatUtil.absoluteFilePath(instanceData.getInstance().getKey().getDirectory(), LauncherApplication.config.INCLUDED_FILES_DIR , f.getName())), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    exceptionQueue.add(e);
                    LOGGER.warn("Unable to cleanup launch resources: unable to move non-included file: " + f.getName());
                }
            }
        }
        if(!exceptionQueue.isEmpty()) {
            throw new GameResourceException("Unable to cleanup launch resources: unable to cleanup " + exceptionQueue.size() + " non-included files", exceptionQueue.get(0));
        }
        LOGGER.debug("Removed remaining files: instance={}", instanceData.getInstance().getKey().getId());
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
