package net.treset.minecraftlauncher.launching;

import javafx.util.Pair;
import net.hycrafthd.minecraft_authenticator.login.User;
import net.treset.mc_version_loader.launcher.LauncherFeature;
import net.treset.mc_version_loader.launcher.LauncherLaunchArgument;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherVersionDetails;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.data.InstanceData;
import net.treset.minecraftlauncher.util.FormatUtil;
import net.treset.minecraftlauncher.util.QuickPlayData;
import net.treset.minecraftlauncher.util.exception.GameCommandException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandBuilder {
    private static final Logger LOGGER = LogManager.getLogger(CommandBuilder.class);

    private ProcessBuilder processBuilder;
    private InstanceData instanceData;
    private User minecraftUser;
    private QuickPlayData quickPlayData;

    public CommandBuilder(ProcessBuilder processBuilder, InstanceData instanceData, User minecraftUser, QuickPlayData quickPlayData) {
        this.processBuilder = processBuilder;
        this.instanceData = instanceData;
        this.minecraftUser = minecraftUser;
        this.quickPlayData = quickPlayData;
    }

    public void makeStartCommand() throws GameCommandException {
        if(instanceData.getVersionComponents() == null || instanceData.getVersionComponents().isEmpty() || instanceData.getJavaComponent() == null || instanceData.getResourcepacksComponent() == null || instanceData.getLibrariesDir() == null) {
            throw new GameCommandException("Unable to create start command: unmet requirements");
        }

        String assetsIndex = null;
        for(Pair<LauncherManifest, LauncherVersionDetails> v : instanceData.getVersionComponents()) {
            if(v.getValue().getAssets() != null && !v.getValue().getAssets().isBlank()) {
                assetsIndex = v.getValue().getAssets();
                break;
            }
        }
        if(assetsIndex == null) {
            throw new GameCommandException("Unable to create start command: unable to determine asset index");
        }

        String mainClass = null;
        for(Pair<LauncherManifest, LauncherVersionDetails> v : instanceData.getVersionComponents()) {
            if(v.getValue().getMainFile() != null && !v.getValue().getMainFile().isBlank()) {
                mainClass = v.getValue().getMainClass();
                break;
            }
        }
        if(mainClass == null) {
            throw new GameCommandException("Unable to create start command: unable to determine main class");
        }

        List<String> libraries = new ArrayList<>();
        for(Pair<LauncherManifest, LauncherVersionDetails> v : instanceData.getVersionComponents()) {
            for(String l : v.getValue().getLibraries()) {
                libraries.add(instanceData.getLibrariesDir() + l);
            }
        }
        if(libraries.isEmpty()) {
            throw new GameCommandException("Unable to create start command: unable to determine libraries");
        }
        for(Pair<LauncherManifest, LauncherVersionDetails> v : instanceData.getVersionComponents()) {
            if(v.getValue().getMainFile() == null ||v.getValue().getMainFile().isBlank()) {
                throw new GameCommandException("Unable to create start command: unable to determine main file: version=" + v.getKey().getId());
            }
            libraries.add(v.getKey().getDirectory() + v.getValue().getMainFile());
        }

        String resX = null;
        String resY = null;
        for(LauncherFeature f : instanceData.getInstance().getValue().getFeatures()) {
            if(f.getFeature().equals("resolution_x")) {
                resX = f.getValue();
            }
            if(f.getFeature().equals("resolution_y")) {
                resY = f.getValue();
            }
        }

        File gameDir = new File(instanceData.getGameDataDir());
        if(!gameDir.isDirectory()) {
            throw new GameCommandException("Unable to create start command: game directory is not a directory: directory=" + gameDir.getAbsolutePath());
        }

        processBuilder.directory(gameDir);
        processBuilder.command(new ArrayList<>());
        processBuilder.command().add(FormatUtil.absoluteFilePath(instanceData.getJavaComponent().getDirectory(), "bin", "java"));
        try {
            appendArguments(processBuilder, instanceData.getInstance().getValue().getJvm_arguments(), instanceData, minecraftUser, instanceData.getGameDataDir(), instanceData.getAssetsDir(), assetsIndex, libraries, resX, resY, quickPlayData);
        } catch(GameCommandException e) {
            throw new GameCommandException("Unable to create start command: unable to append instance jvm arguments: version=" + instanceData.getInstance().getKey().getId(), e);
        }
        for(Pair<LauncherManifest, LauncherVersionDetails> v : instanceData.getVersionComponents()) {
            try {
                appendArguments(processBuilder, v.getValue().getJvmArguments(), instanceData, minecraftUser, instanceData.getGameDataDir(), instanceData.getAssetsDir(), assetsIndex, libraries, resX, resY, quickPlayData);
            } catch(GameCommandException e) {
                throw new GameCommandException("Unable to create start command: unable to append jvm arguments: version=" + v.getKey().getId(), e);
            }
        }
        processBuilder.command().add(mainClass);
        for(Pair<LauncherManifest, LauncherVersionDetails> v : instanceData.getVersionComponents()) {
            try {
                appendArguments(processBuilder, v.getValue().getGameArguments(), instanceData, minecraftUser, instanceData.getGameDataDir(), instanceData.getAssetsDir(), assetsIndex, libraries, resX, resY, quickPlayData);
            } catch(GameCommandException e) {
                throw new GameCommandException("Unable to create start command: unable to append game arguments: version=" + v.getKey().getId(), e);
            }
        }

        LOGGER.info("Created start command, instance=" + instanceData.getInstance().getKey().getId());
    }

    private void appendArguments(ProcessBuilder pb, List<LauncherLaunchArgument> args, InstanceData instanceData, User minecraftUser, String gameDataDir, String assetsDir, String assetsIndex, List<String> libraries, String resX, String resY, QuickPlayData quickPlayData) throws GameCommandException {
        List<GameCommandException> exceptionQueue = new ArrayList<>();
        for(LauncherLaunchArgument a : args) {
            try {
                appendArgument(pb, instanceData, minecraftUser, gameDataDir, assetsDir, assetsIndex, libraries, resX, resY, a, quickPlayData);
            } catch (GameCommandException e) {
                exceptionQueue.add(e);
                LOGGER.warn("Unable to append arguments: unable to append argument: argument=" + a);
            }
        }
        if(!exceptionQueue.isEmpty()) {
            throw new GameCommandException("Unable to append " + exceptionQueue.size() + " arguments", exceptionQueue.get(0));
        }
    }

    private void appendArgument(ProcessBuilder pb, InstanceData instanceData, User minecraftUser, String gameDataDir, String assetsDir, String assetsIndex, List<String> libraries, String resX, String resY, LauncherLaunchArgument a, QuickPlayData quickPlayData) throws GameCommandException {
        if(a.isActive(instanceData.getInstance().getValue().getFeatures())) {
            Map<String, String> replacements = new HashMap<>();
            List<GameCommandException> exceptionQueue = new ArrayList<>();
            for (String r : a.getReplacementValues()) {
                try {
                    String replacement = getReplacement(r, gameDataDir, instanceData.getJavaComponent().getDirectory(), assetsDir, instanceData.getResourcepacksComponent().getDirectory(), assetsIndex, libraries, minecraftUser, LauncherApplication.stringLocalizer.getFormatted("game.version_name", LauncherApplication.stringLocalizer.get("launcher.slug"), LauncherApplication.stringLocalizer.get("launcher.version"), instanceData.getInstance().getKey().getId().substring(0, 3), instanceData.getInstance().getKey().getId().substring(instanceData.getInstance().getKey().getId().length()-2)), LauncherApplication.stringLocalizer.getFormatted("game.version_type", instanceData.getInstance().getKey().getName()), resX, resY, quickPlayData);
                    replacements.put(r, replacement);
                } catch (GameCommandException e) {
                    exceptionQueue.add(e);
                    LOGGER.warn("Unable to append argument: unable to replace variable: argument=" + a.getArgument() + ", variable=" + r, e);
                }
            }
            if(!exceptionQueue.isEmpty()) {
                throw new GameCommandException("Unable to append argument: unable to replace " + exceptionQueue.size() + " variables: argument=" + a.getArgument(), exceptionQueue.get(0));
            }
            a.replace(replacements);
            if(!a.isFinished()) {
                throw new GameCommandException("Unable to append argument: unable to replace all variables: argument=" + a);
            }
            pb.command().add(a.getParsedArgument());
        }
    }

    private String getReplacement(String key, String gameDir, String javaDir, String assetsDir, String resourcepackDir, String assetsIndex, List<String> libraries, User minecraftUser, String versionName, String versionType, String resX, String resY, QuickPlayData quickPlayData) throws GameCommandException {
        switch(key) {
            case "natives_directory" -> {
                return javaDir + "lib";
            }
            case "launcher_name" -> {
                return LauncherApplication.stringLocalizer.get("launcher.name");
            }
            case "launcher_version" -> {
                return LauncherApplication.stringLocalizer.get("launcher.version");
            }
            case "classpath" -> {
                StringBuilder sb = new StringBuilder();
                for(String l : libraries) {
                    sb.append(l).append(";");
                }
                return sb.substring(0, sb.length() - 1);
            }
            case "auth_player_name" -> {
                return minecraftUser.name();
            }
            case "version_name" -> {
                return versionName;
            }
            case "game_directory" -> {
                return gameDir;
            }
            case "resourcepack_directory" -> {
                return resourcepackDir;
            }
            case "assets_root" -> {
                return assetsDir;
            }
            case "assets_index_name" -> {
                return assetsIndex;
            }
            case "auth_uuid" -> {
                return minecraftUser.uuid();
            }
            case "auth_xuid" -> {
                return minecraftUser.xuid();
            }
            case "auth_access_token" -> {
                return minecraftUser.accessToken();
            }
            case "clientid" -> {
                return minecraftUser.clientId();
            }
            case "user_type" -> {
                return minecraftUser.type();
            }
            case "version_type" -> {
                return versionType;
            }
            case "resolution_width" -> {
                return resX;
            }
            case "resolution_height" -> {
                return resY;
            }
            case "quickPlayPath" -> {
                return "quickPlay/log.json";
            }
            case "quickPlaySingleplayer" -> {
                return quickPlayData != null && quickPlayData.getType() == QuickPlayData.Type.WORLD ? quickPlayData.getName() : "";
            }
            case "quickPlayMultiplayer" -> {
                return quickPlayData != null && quickPlayData.getType() == QuickPlayData.Type.SERVER ? quickPlayData.getName() : "";
            }
            case "quickPlayRealms" -> {
                return quickPlayData != null && quickPlayData.getType() == QuickPlayData.Type.REALM ? quickPlayData.getName() : "";
            }

            default -> throw new GameCommandException("Unknown environment variable: key=" + key);
        }
    }

    public ProcessBuilder getProcessBuilder() {
        return processBuilder;
    }

    public void setProcessBuilder(ProcessBuilder processBuilder) {
        this.processBuilder = processBuilder;
    }

    public InstanceData getInstanceData() {
        return instanceData;
    }

    public void setInstanceData(InstanceData instanceData) {
        this.instanceData = instanceData;
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
}
