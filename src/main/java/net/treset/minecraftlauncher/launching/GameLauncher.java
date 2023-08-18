package net.treset.minecraftlauncher.launching;

import javafx.util.Pair;
import net.hycrafthd.minecraft_authenticator.login.User;
import net.treset.mc_version_loader.launcher.LauncherInstanceDetails;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.minecraftlauncher.data.InstanceData;
import net.treset.minecraftlauncher.data.LauncherFiles;
import net.treset.minecraftlauncher.util.QuickPlayData;
import net.treset.minecraftlauncher.util.exception.FileLoadException;
import net.treset.minecraftlauncher.util.exception.GameCommandException;
import net.treset.minecraftlauncher.util.exception.GameLaunchException;
import net.treset.minecraftlauncher.util.exception.GameResourceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class GameLauncher {
    private static final Logger LOGGER = LogManager.getLogger(GameLauncher.class);

    private InstanceData instance;
    private LauncherFiles files;
    private User minecraftUser;
    private QuickPlayData quickPlayData;
    private List<Consumer<String>> exitCallbacks;
    private ResourceManager resourceManager;
    private GameListener gameListener;

    public GameLauncher(InstanceData instance, LauncherFiles files, User minecraftUser, QuickPlayData quickPlayData, List<Consumer<String>> exitCallbacks) {
        this.instance = instance;
        this.files = files;
        this.minecraftUser = minecraftUser;
        this.quickPlayData = quickPlayData;
        this.exitCallbacks = exitCallbacks;
    }

    public GameLauncher(InstanceData instance, LauncherFiles files, User minecraftUser, QuickPlayData quickPlayData) {
        this(instance, files, minecraftUser, quickPlayData, null);
    }

    public GameLauncher(InstanceData instance, LauncherFiles files, User minecraftUser, List<Consumer<String>> exitCallbacks) {
        this(instance, files, minecraftUser, null, exitCallbacks);
    }

    public GameLauncher(InstanceData instance, LauncherFiles files, User minecraftUser) {
        this(instance, files, minecraftUser, null, null);
    }

    public void launch(boolean cleanupActiveInstance, Consumer<Exception> doneCallback) throws GameLaunchException {
        try {
            files.reloadAll();
        } catch (FileLoadException e) {
            throw new GameLaunchException("Unable to launch game: file reload failed", e);
        }

        if(files.getLauncherDetails().getActiveInstance() != null && !files.getLauncherDetails().getActiveInstance().isBlank()) {
            if(!cleanupActiveInstance) {
                throw new GameLaunchException("Unable to launch game: active instance already exists");
            }
            try {
                cleanUpOldInstance();
            } catch (GameLaunchException e) {
                throw new GameLaunchException("Unable to launch game: unable to clean up old instance", e);
            }
        }

        if(instance == null) {
            throw new GameLaunchException("Unable to launch game: instance data is null");
        }
        resourceManager = new ResourceManager(instance);


        files.getLauncherDetails().setActiveInstance(instance.getInstance().getKey().getId());
        try {
            files.getLauncherDetails().writeToFile(files.getMainManifest().getDirectory() + files.getMainManifest().getDetails());
        } catch (IOException e) {
            throw new GameLaunchException("Unable to launch game: unable to write launcher details", e);
        }

        new Thread(() -> {
            try {
                resourceManager.prepareResources();
                resourceManager.setLastPlayedTime();
            } catch (GameResourceException | IOException e) {
                GameLaunchException gle = new GameLaunchException("Unable to launch game: unable to prepare resources", e);
                try {
                    abortLaunch();
                } catch (GameLaunchException ex) {
                    doneCallback.accept(new GameLaunchException("Unable to launch game: unable to cleanup after fail", gle));
                }
                doneCallback.accept(gle);
                return;
            }
            try {
                finishLaunch();
                doneCallback.accept(null);
            } catch (GameLaunchException e) {
                doneCallback.accept(e);
            }
        }).start();


    }

    private void finishLaunch() throws GameLaunchException {
        ProcessBuilder pb = new ProcessBuilder().redirectOutput(ProcessBuilder.Redirect.PIPE);
        CommandBuilder commandBuilder = new CommandBuilder(pb, instance, minecraftUser, quickPlayData);
        try {
            commandBuilder.makeStartCommand();
        } catch (GameCommandException e) {
            abortLaunch();
            throw new GameLaunchException("Unable to launch game: unable to set start command", e);
        }

        LOGGER.info("Starting game");
        LOGGER.debug("command=" + pb.command());

        try {
            Process p = pb.start();
            gameListener = new GameListener(p, resourceManager, exitCallbacks);
            gameListener.start();
        } catch (IOException e) {
            abortLaunch();
            throw new GameLaunchException("Unable to launch game: unable to execute command", e);
        }
    }

    private void cleanUpOldInstance() throws GameLaunchException {
        LOGGER.debug("Cleaning up old instance resources: id=" + files.getLauncherDetails().getActiveInstance());
        boolean found = false;
        for(Pair<LauncherManifest, LauncherInstanceDetails> i : files.getInstanceComponents()) {
            if(Objects.equals(i.getKey().getId(), files.getLauncherDetails().getActiveInstance())) {
                InstanceData instanceData;
                try {
                    instanceData = InstanceData.of(i, files);
                } catch (FileLoadException e) {
                    throw new GameLaunchException("Unable to cleanup old instance: unable to load instance data", e);
                }
                ResourceManager resourceManager = new ResourceManager(instanceData);

                try {
                    resourceManager.cleanupGameFiles();
                } catch (GameResourceException e) {
                    throw new GameLaunchException("Unable to cleanup old instance: unable to cleanup resources", e);
                }
                found = true;
                break;
            }
        }
        if(!found) {
            throw new GameLaunchException("Unable to cleanup old instance: instance not found");
        }
        LOGGER.info("Old instance resources cleaned up");
    }

    private void abortLaunch() throws GameLaunchException {
        LOGGER.warn("Aborting launch");
        try {
            resourceManager.cleanupGameFiles();
        } catch (GameResourceException e) {
            throw new GameLaunchException("Unable to abort launch correctly: failed to cleanup game files");
        }
        files.getLauncherDetails().setActiveInstance(null);
        try {
            files.getLauncherDetails().writeToFile(files.getMainManifest().getDirectory() + files.getMainManifest().getDetails());
        } catch (IOException e) {
            throw new GameLaunchException("Unable to abort launch correctly: failed to write launcher details");
        }
    }

    public InstanceData getInstance() {
        return instance;
    }

    public void setInstance(InstanceData instance) {
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

    public QuickPlayData getQuickPlayData() {
        return quickPlayData;
    }

    public void setQuickPlayData(QuickPlayData quickPlayData) {
        this.quickPlayData = quickPlayData;
    }

    public void setExitCallbacks(List<Consumer<String>> exitCallbacks) {
        this.exitCallbacks = exitCallbacks;
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public GameListener getGameListener() {
        return gameListener;
    }

    public void setGameListener(GameListener gameListener) {
        this.gameListener = gameListener;
    }
}
