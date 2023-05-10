package net.treset.minecraftlauncher.launching;

import javafx.util.Pair;
import net.hycrafthd.minecraft_authenticator.login.User;
import net.treset.mc_version_loader.launcher.LauncherInstanceDetails;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.minecraftlauncher.data.InstanceData;
import net.treset.minecraftlauncher.data.LauncherFiles;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class GameLauncher {
    private static final Logger LOGGER = LogManager.getLogger(GameLauncher.class);

    private Pair<LauncherManifest, LauncherInstanceDetails> instance;
    private LauncherFiles files;
    private User minecraftUser;
    private List<Consumer<String>> exitCallbacks;
    private ResourceManager resourceManager;
    private GameListener gameListener;

    public GameLauncher(Pair<LauncherManifest, LauncherInstanceDetails> instance, LauncherFiles files, User minecraftUser, List<Consumer<String>> exitCallbacks) {
        this.instance = instance;
        this.files = files;
        this.minecraftUser = minecraftUser;
        this.exitCallbacks = exitCallbacks;
    }

    public boolean launch() {
        if(!files.isValid() || !files.reloadAll()) {
            LOGGER.warn("Unable to launch game: file reload failed");
            return false;
        }

        if(files.getLauncherDetails().getActiveInstance() != null && !files.getLauncherDetails().getActiveInstance().isBlank() && !cleanUpOldInstance()) {
            LOGGER.warn("Unable to launch game: unable to clean up old instance");
            return false;
        }

        InstanceData instanceData = InstanceData.of(instance, files);
        if(instanceData == null) {
            LOGGER.warn("Unable to launch game: instance data loaded incorrectly");
            return false;
        }
        resourceManager = new ResourceManager(instanceData);


        files.getLauncherDetails().setActiveInstance(instance.getKey().getId());
        if(!files.getLauncherDetails().writeToFile(files.getMainManifest().getDirectory() + files.getMainManifest().getDetails())) {
            LOGGER.warn("Unable to launch game: unable to write launcher details");
            return false;
        }

        if(!resourceManager.prepareResources()) {
            LOGGER.warn("Unable to launch game: unable to prepare resources");
            abortLaunch();
            return false;
        }

        if(!resourceManager.setLastPlayedTime()) {
            LOGGER.warn("Unable to launch game: unable to set last played time");
            abortLaunch();
            return false;
        }

        ProcessBuilder pb = new ProcessBuilder().redirectOutput(ProcessBuilder.Redirect.PIPE);
        CommandBuilder commandBuilder = new CommandBuilder(pb, instanceData, minecraftUser);
        if(!commandBuilder.makeStartCommand()) {
            LOGGER.warn("Unable to launch game: unable to set start command");
            abortLaunch();
            return false;
        }

        LOGGER.info("Starting game");
        LOGGER.debug("command=" + pb.command());
        try {
            Process p = pb.start();
            gameListener = new GameListener(p, resourceManager, exitCallbacks);
            gameListener.start();
            return true;
        } catch (IOException e) {
            LOGGER.warn("Unable to launch game: unable to execute command", e);
            abortLaunch();
            return false;
        }
    }

    private boolean cleanUpOldInstance() {
        LOGGER.debug("Cleaning up old instance resources: id=" + files.getLauncherDetails().getActiveInstance());
        boolean found = false;
        for(Pair<LauncherManifest, LauncherInstanceDetails> i : files.getInstanceComponents()) {
            if(Objects.equals(i.getKey().getId(), files.getLauncherDetails().getActiveInstance())) {
                InstanceData instanceData = InstanceData.of(i, files);
                if(instanceData == null) {
                    LOGGER.warn("Unable to cleanup old instance: instance data loaded incorrectly");
                    return false;
                }
                ResourceManager resourceManager = new ResourceManager(instanceData);

                if(!resourceManager.cleanupGameFiles()) {
                    LOGGER.warn("Unable to cleanup old instance: unable to cleanup resources");
                    return false;
                }
                found = true;
                break;
            }
        }
        if(!found) {
            LOGGER.warn("Unable to cleanup old instance: instance not found");
            return false;
        }
        LOGGER.info("Old instance resources cleaned up");
        return true;
    }

    private boolean abortLaunch() {
        LOGGER.warn("Aborting launch");
        if(!resourceManager.cleanupGameFiles()) {
            LOGGER.warn("Unable to abort launch correctly: failed to cleanup game files");
            return false;
        }
        files.getLauncherDetails().setActiveInstance(null);
        if(!files.getLauncherDetails().writeToFile(files.getMainManifest().getDirectory() + files.getMainManifest().getDetails())) {
            LOGGER.warn("Unable to abort launch: unable to write launcher details");
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

    public LauncherFiles getFiles() {
        return files;
    }

    public void setFiles(LauncherFiles files) {
        this.files = files;
    }

    public User getMinecraftUser() {
        return minecraftUser;
    }

    public void setMinecraftUser(User minecraftUser) {
        this.minecraftUser = minecraftUser;
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public GameListener getGameListener() {
        return gameListener;
    }

    public void setResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public void setGameListener(GameListener gameListener) {
        this.gameListener = gameListener;
    }

    public void setExitCallbacks(List<Consumer<String>> exitCallbacks) {
        this.exitCallbacks = exitCallbacks;
    }
}
