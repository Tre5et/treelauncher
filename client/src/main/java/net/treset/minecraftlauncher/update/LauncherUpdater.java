package net.treset.minecraftlauncher.update;

import net.treset.mc_version_loader.json.JsonUtils;
import net.treset.minecraftlauncher.util.file.LauncherFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.TriConsumer;

import java.io.IOException;
import java.util.ArrayList;

public class LauncherUpdater {
    private static final Logger LOGGER = LogManager.getLogger(LauncherUpdater.class);

    private final UpdateService updateService = new UpdateService();
    private Update update;

    private boolean readyToUpdate = false;

    public Update getUpdate() throws IOException {
        if(update == null) {
            return fetchUpdate();
        }
        return update;
    }

    public Update fetchUpdate() throws IOException {
        update = updateService.update();
        return update;
    }

    public void executeUpdate(TriConsumer<Integer, Integer, String> changeCallback) throws IOException {
        changeCallback.accept(0, 0, "Checking for updates...");
        if(update == null) {
            fetchUpdate();
        }
        if(update.getId() == null) {
            throw new IOException("No Update available");
        }
        int total = update.getChanges().size();
        int current = 0;
        changeCallback.accept(current, total, "Downloading files...");

        ArrayList<Update.Change> updaterChanges = new ArrayList<>();
        ArrayList<Exception> exceptions = new ArrayList<>();
        ArrayList<LauncherFile> backedUpFiles = new ArrayList<>();

        for(Update.Change change : update.getChanges()) {
            changeCallback.accept(++current, total, change.getPath());
            LauncherFile targetFile = LauncherFile.of(change.getPath());
            LauncherFile updateFile = LauncherFile.of(change.getPath() + ".up");
            LauncherFile backupFile = LauncherFile.of(change.getPath() + ".bak");
            switch(change.getMode()) {
                case FILE:
                    try {
                        LOGGER.debug("Downloading file: " + change.getPath());
                        updateFile.write(updateService.file(update.getId(), change.getPath()));
                    } catch (IOException e) {
                        exceptions.add(e);
                    }
                    if(change.isUpdater()) {
                        LOGGER.debug("Delegating file to updater: " + change.getPath());
                        updaterChanges.add(change);
                    } else {
                        LOGGER.debug("Moving file to target: " + change.getPath());
                        try {
                            if(targetFile.isFile()) {
                                targetFile.moveTo(backupFile);
                                backedUpFiles.add(backupFile);
                            }
                            updateFile.moveTo(targetFile);
                        } catch (IOException e) {
                            exceptions.add(e);
                        }
                    }
                    break;
                case DELETE:
                    if(change.isUpdater()) {
                        LOGGER.debug("Delegating file to updater: " + change.getPath());
                        updaterChanges.add(change);
                    } else {
                        LOGGER.debug("Deleting file: " + change.getPath());
                        try {
                            if(targetFile.isFile()) {
                                targetFile.moveTo(backupFile);
                                backedUpFiles.add(backupFile);
                            } else {
                                LOGGER.warn("File to delete does not exist: " + targetFile);
                            }
                        } catch (IOException e) {
                            exceptions.add(e);
                        }
                    }
                    break;
                default:
                    LOGGER.debug("Delegating file to updater: " + change.getPath());
                    updaterChanges.add(change);
                    break;
            }
        }

        if(!exceptions.isEmpty()) {
            LOGGER.debug("Update failed. Reverting changes");
            changeCallback.accept(0, 0, "Update Failed. Reverting changes...");
            for(LauncherFile file : backedUpFiles) {
                LOGGER.debug("Reverting file: " + file);
                try {
                    file.moveTo(LauncherFile.of(file.getPath().substring(0, file.getPath().length() - 4)));
                } catch (IOException e) {
                    throw new IOException("Failed to revert changes", e);
                }
            }
            throw new IOException("Failed to update the launcher", exceptions.get(0));
        }

        LOGGER.debug("Removing backed up files");
        for(LauncherFile file: backedUpFiles) {
            try {
                file.remove();
            } catch (IOException ignored) {}
        }

        LOGGER.debug("Writing updater file");
        changeCallback.accept(total, total, "Writing updater file...");

        LauncherFile updaterFile = LauncherFile.of("update.json");
        try {
            updaterFile.write(
                    JsonUtils.getGson().toJson(updaterChanges)
            );
            readyToUpdate = true;
        } catch (IOException e) {
            throw new IOException("Failed to write updater file", e);
        }
    }

    public void startUpdater(boolean restart) throws IOException {
        if(!readyToUpdate) {
            return;
        }

        LOGGER.info("Starting updater...");

        ProcessBuilder pb = new ProcessBuilder(LauncherFile.of(System.getProperty("java.home"), "bin", "java").getPath(), "-jar", "app/updater.jar");
        if(restart) {
            pb.command().add("-gui");
            if(new LauncherFile("TreeLauncher.exe").isFile()) {
                LOGGER.info("Restarting TreeLauncher.exe after update");
                pb.command().add("-rTreeLauncher.exe");
            } else {
                LOGGER.warn("TreeLauncher.exe not found to restart, searching alternative file...");
                LauncherFile[] files = new LauncherFile(".").listFiles();
                if(files == null) {
                    LOGGER.error("Failed to list files!");
                } else {
                    for(LauncherFile file : files) {
                        if(file.getName().endsWith(".exe")) {
                            LOGGER.info("Restarting alternative file " + file.getName() + " after update");
                            pb.command().add("-r" + file.getName());
                            break;
                        }
                    }
                }
            }
        }
        pb.start();
    }
}
