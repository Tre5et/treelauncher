package net.treset.minecraftlauncher.launching;

import javafx.util.Pair;
import net.hycrafthd.minecraft_authenticator.login.User;
import net.treset.mc_version_loader.launcher.LauncherFeature;
import net.treset.mc_version_loader.launcher.LauncherLaunchArgument;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherVersionDetails;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.data.InstanceData;
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

    public CommandBuilder(ProcessBuilder processBuilder, InstanceData instanceData, User minecraftUser) {
        this.processBuilder = processBuilder;
        this.instanceData = instanceData;
        this.minecraftUser = minecraftUser;
    }

    public boolean makeStartCommand() {
        if(instanceData.getVersionComponents() == null || instanceData.getVersionComponents().isEmpty() || instanceData.getJavaComponent() == null || instanceData.getResourcepacksComponent() == null || instanceData.getLibrariesDir() == null) {
            LOGGER.warn("Unable to create start command: unmet preconditions");
            return false;
        }

        String assetsIndex = null;
        for(Pair<LauncherManifest, LauncherVersionDetails> v : instanceData.getVersionComponents()) {
            if(v.getValue().getAssets() != null && !v.getValue().getAssets().isBlank()) {
                assetsIndex = v.getValue().getAssets();
                break;
            }
        }
        if(assetsIndex == null) {
            LOGGER.warn("Unable to create start command: unable to determine asset index");
            return false;
        }

        String mainClass = null;
        for(Pair<LauncherManifest, LauncherVersionDetails> v : instanceData.getVersionComponents()) {
            if(v.getValue().getMainFile() != null && !v.getValue().getMainFile().isBlank()) {
                mainClass = v.getValue().getMainClass();
                break;
            }
        }
        if(mainClass == null) {
            LOGGER.warn("Unable to create start command: unable to determine main class");
            return false;
        }

        List<String> libraries = new ArrayList<>();
        for(Pair<LauncherManifest, LauncherVersionDetails> v : instanceData.getVersionComponents()) {
            for(String l : v.getValue().getLibraries()) {
                libraries.add(instanceData.getLibrariesDir() + l);
            }
        }
        if(libraries.isEmpty()) {
            LOGGER.warn("Unable to create start command: unable to determine libraries");
            return false;
        }
        for(Pair<LauncherManifest, LauncherVersionDetails> v : instanceData.getVersionComponents()) {
            if(v.getValue().getMainFile() == null ||v.getValue().getMainFile().isBlank()) {
                LOGGER.warn("Unable to create start command: unable to determine main file: version=" + v.getKey().getId());
                return false;
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
            LOGGER.warn("Unable to create start command: game directory is not a directory: directory=" + gameDir.getAbsolutePath());
            return false;
        }

        processBuilder.directory(gameDir);
        processBuilder.command(new ArrayList<>());
        processBuilder.command().add(instanceData.getJavaComponent().getDirectory() + "bin" + "/" + "java");
        for(Pair<LauncherManifest, LauncherVersionDetails> v : instanceData.getVersionComponents()) {
            if(!appendArguments(processBuilder, v.getValue().getJvmArguments(), instanceData, minecraftUser, instanceData.getGameDataDir(), instanceData.getAssetsDir(), assetsIndex, libraries, mainClass, resX, resY)) {
                LOGGER.warn("Unable to create start command: unable to append jvm arguments: version=" + v.getKey().getId());
                return false;
            }
        }
        processBuilder.command().add(mainClass);
        for(Pair<LauncherManifest, LauncherVersionDetails> v : instanceData.getVersionComponents()) {
            if(!appendArguments(processBuilder, v.getValue().getGameArguments(), instanceData, minecraftUser, instanceData.getGameDataDir(), instanceData.getAssetsDir(), assetsIndex, libraries, mainClass, resX, resY)) {
                LOGGER.warn("Unable to create start command: unable to append jvm arguments: version=" + v.getKey().getId());
                return false;
            }
        }

        LOGGER.info("Created start command, instance=" + instanceData.getInstance().getKey().getId());
        return true;
    }

    private boolean appendArguments(ProcessBuilder pb, List<LauncherLaunchArgument> args, InstanceData instanceData, User minecraftUser, String gameDataDir, String assetsDir, String assetsIndex, List<String> libraries, String mainClass, String resX, String resY) {
        for(LauncherLaunchArgument a : args) {
            if(!appendArgument(pb, instanceData, minecraftUser, gameDataDir, assetsDir, assetsIndex, libraries, mainClass, resX, resY, a)) {
                LOGGER.warn("Unable to append arguments: unable to append argument: argument=" + a);
                return false;
            }
        }
        return true;
    }

    private boolean appendArgument(ProcessBuilder pb, InstanceData instanceData, User minecraftUser, String gameDataDir, String assetsDir, String assetsIndex, List<String> libraries, String mainClass, String resX, String resY, LauncherLaunchArgument a) {
        if(a.isActive(instanceData.getInstance().getValue().getFeatures())) {
            Map<String, String> replacements = new HashMap<>();
            for (String r : a.getReplacementValues()) {
                String replacement = getReplacement(r, gameDataDir, instanceData.getJavaComponent().getDirectory(), assetsDir, instanceData.getResourcepacksComponent().getDirectory(), assetsIndex, libraries, mainClass, minecraftUser, LauncherApplication.stringLocalizer.get("game.version_name", LauncherApplication.stringLocalizer.get("launcher.slug") ), LauncherApplication.stringLocalizer.get("game.version_type", LauncherApplication.stringLocalizer.get("launcher.name"), LauncherApplication.stringLocalizer.get("launcher.version") ), resX, resY);
                if(replacement == null) {
                    LOGGER.warn("Unable to append argument: unable to append environment variable: key=" + r);
                    return false;
                }
                replacements.put(r, replacement);
            }
            a.replace(replacements);
            if(!a.isFinished()) {
                LOGGER.warn("Unable to append argument: unable to replace all environment variables: argument=" + a);
                return false;
            }
            pb.command().add(a.getParsedArgument());
        }
        return true;
    }

    private String getReplacement(String key, String gameDir, String javaDir, String assetsDir, String resourcepackDir, String assetsIndex, List<String> libraries, String mainClass, User minecraftUser, String versionName, String versionType, String resX, String resY) {
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
            default -> {
                return null;
            }
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
}
