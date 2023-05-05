package net.treset.minecraftlauncher.launching;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Consumer;

public class GameListener {
    private static final Logger LOGGER = LogManager.getLogger(GameListener.class);

    private Process gameProcess;
    private ResourceManager resourceManager;
    private List<Consumer<String>> exitCallbacks;
    private boolean valid = false;
    private boolean running = false;
    private boolean exited = false;
    private int exitCode = -1;

    public GameListener(Process gameProcess, ResourceManager resourceManager, List<Consumer<String>> exitCallbacks) {
        this.gameProcess = gameProcess;
        this.resourceManager = resourceManager;
        this.exitCallbacks = exitCallbacks;
        this.valid = true;
    }

    public void start() {
        Thread t = new Thread(this::listenToGameOutput, "GameListener");
        t.start();
    }

    private void listenToGameOutput() {
        running = true;
        LOGGER.info("Listening to game process: pid={}", gameProcess.pid());
        try(BufferedReader reader = gameProcess.inputReader()) {
            reader.lines().iterator().forEachRemaining(value -> {
                LOGGER.debug("Game: " + value);
            });
        } catch (IOException e) {
            LOGGER.debug("Game output forwarding failed: pid={}", gameProcess.pid(), e);
        }

        try {
            gameProcess.waitFor();
        } catch (InterruptedException e) {
            LOGGER.warn("Game listener interrupted: pid={}", gameProcess.pid(), e);
            valid = false;
            return;
        }

        onGameExit();
    }

    private void onGameExit() {
        running = false;
        exitCode = gameProcess.exitValue();
        String error = null;
        if(gameProcess.exitValue() != 0) {
            try (BufferedReader reader = gameProcess.errorReader()) {
                StringJoiner out = new StringJoiner("\n");
                reader.lines().iterator().forEachRemaining(out::add);
                LOGGER.warn("Game process exited with non-zero code: code={}, pid={}, error={}", gameProcess.exitValue(), gameProcess.pid(), out);
                error = out.toString();
            } catch (IOException e) {
                LOGGER.warn("Game process exited with non-zero code: pid={}, error=unable to read error stream",gameProcess.pid(), e);
                error = "Unable to read error stream";
            }
        }
        else {
            LOGGER.info("Game process exited successfully: pid={}", gameProcess.pid());
        }
        exited = true;
        if(!resourceManager.cleanupGameFiles()) {
            LOGGER.warn("Unable to clean up game files after exit: pid={}", gameProcess.pid());
            error = "Unable to clean up game files";
        }

        for(Consumer<String> c : exitCallbacks) {
            c.accept(error);
        }
    }

    public Process getGameProcess() {
        return gameProcess;
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isExited() {
        return exited;
    }

    public int getExitCode() {
        return exitCode;
    }
}
