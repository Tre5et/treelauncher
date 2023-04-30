package net.treset.minecraftlauncher.launching;

import javafx.util.Pair;
import net.hycrafthd.minecraft_authenticator.login.User;
import net.treset.mc_version_loader.format.FormatUtils;
import net.treset.mc_version_loader.launcher.*;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.config.Config;
import net.treset.minecraftlauncher.file_loading.InstanceData;
import net.treset.minecraftlauncher.file_loading.LauncherFiles;
import net.treset.minecraftlauncher.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameLauncher {
    private static Logger LOGGER = Logger.getLogger(GameLauncher.class.getName());

    public static Process launchGame(Pair<LauncherManifest, LauncherInstanceDetails> instance, LauncherFiles files, User minecraftUser) {
        if(!files.isValid() || !files.reloadAll()) {
            LOGGER.log(Level.WARNING, "Unable to launch game: file reload failed");
            return null;
        }

        if(files.getLauncherDetails().getActiveInstance() != null && !files.getLauncherDetails().getActiveInstance().isBlank()) {
            LOGGER.log(Level.INFO, "Cleaning up old instance resources: id=" + files.getLauncherDetails().getActiveInstance());
            boolean found = false;
            for(Pair<LauncherManifest, LauncherInstanceDetails> i : files.getInstanceComponents()) {
                if(Objects.equals(i.getKey().getId(), files.getLauncherDetails().getActiveInstance())) {
                    InstanceData instanceData = InstanceData.of(i, files);
                    if(instanceData == null) {
                        LOGGER.log(Level.WARNING, "Unable to cleanup old instance: instance data loaded incorrectly");
                        return null;
                    }

                    if(!cleanupResources(instanceData, Config.BASE_DIR + files.getLauncherDetails().getGamedataDir() + "/", files.getGameDetailsManifest().getComponents(), files.getModsManifest().getPrefix(), files.getSavesManifest().getPrefix())) {
                        LOGGER.log(Level.WARNING, "Unable to cleanup old instance: unable to cleanup resources");
                        return null;
                    }
                    found = true;
                    break;
                }
            }
            if(!found) {
                LOGGER.log(Level.WARNING, "Unable to cleanup old instance: instance not found");
                return null;
            }
            files.getLauncherDetails().setActiveInstance(null);
            if(!files.getLauncherDetails().writeToFile(files.getMainManifest().getDirectory() + files.getMainManifest().getDetails())) {
                LOGGER.log(Level.WARNING, "Unable abort launch correctly: unable to write launcher details");
                return null;
            }
        }

        InstanceData instanceData = InstanceData.of(instance, files);
        if(instanceData == null) {
            LOGGER.log(Level.WARNING, "Unable to start game: instance data loaded incorrectly");
            return null;
        }


        files.getLauncherDetails().setActiveInstance(instance.getKey().getId());
        if(!files.getLauncherDetails().writeToFile(files.getMainManifest().getDirectory() + files.getMainManifest().getDetails())) {
            LOGGER.log(Level.WARNING, "Unable to launch game: unable to write launcher details");
            return null;
        }

        if(!prepareResources(instanceData, Config.BASE_DIR + files.getLauncherDetails().getGamedataDir() + "/")) {
            LOGGER.log(Level.WARNING, "Unable to launch game: unable to prepare resources");
            abortInstanceLaunch(files, instanceData);
            return null;
        }

        String command = createStartCommand(instanceData, Config.BASE_DIR + files.getLauncherDetails().getGamedataDir() + "/", Config.BASE_DIR + files.getLauncherDetails().getAssetsDir() + "/", Config.BASE_DIR + files.getLauncherDetails().getLibrariesDir() + "/", minecraftUser);
        if(command == null) {
            LOGGER.log(Level.WARNING, "Unable to launch game: unable to create start command");
            abortInstanceLaunch(files, instanceData);
            return null;
        }

        LOGGER.log(Level.INFO, "Launching game: command=" + command);

        Runtime runtime = Runtime.getRuntime();
        try {
            return runtime.exec(command);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unable to launch game: unable to execute command: " + command, e);
            abortInstanceLaunch(files, instanceData);
            return null;
        }
    }


    private static void abortInstanceLaunch(LauncherFiles files, InstanceData instanceData) {
        if(!cleanupResources(instanceData, Config.BASE_DIR + files.getLauncherDetails().getGamedataDir() + "/", files.getGameDetailsManifest().getComponents(), files.getModsManifest().getPrefix(), files.getSavesManifest().getPrefix())) {
            LOGGER.log(Level.WARNING, "Unable abort launch correctly: unable to cleanup resources");
            return;
        }
        files.getLauncherDetails().setActiveInstance(null);
        if(!files.getLauncherDetails().writeToFile(files.getMainManifest().getDirectory() + files.getMainManifest().getDetails())) {
            LOGGER.log(Level.WARNING, "Unable abort launch correctly: unable to write launcher details");
        }
    }

    public static boolean cleanupResources(InstanceData instanceData, String gameDataPath, List<String> gameDataManifests, String prefixMods, String prefixSaves) {
        try {
            Files.move(Path.of(instanceData.getSavesComponent().getDirectory()), Path.of(gameDataPath + "saves_" + instanceData.getSavesComponent().getId()));
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unable to cleanup launch resources: rename saves file failed", e);
            return false;
        }
        instanceData.getSavesComponent().setDirectory(gameDataPath + "saves_" + instanceData.getSavesComponent().getId() + "/");

        if(instanceData.getModsComponent() != null) {
            try {
                Files.move(Path.of(instanceData.getModsComponent().getKey().getDirectory()), Path.of(gameDataPath + "mods_" + instanceData.getModsComponent().getKey().getId()));
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Unable to cleanup launch resources: rename mods file failed", e);
                return false;
            }
            instanceData.getModsComponent().getKey().setDirectory(gameDataPath + "mods_" + instanceData.getModsComponent().getKey().getId() + "/");
        }

        File gameDataDir = new File(gameDataPath);
        if(!gameDataDir.isDirectory()) {
            LOGGER.log(Level.WARNING, "Unable to cleanup launch resources: game data directory not found");
            return false;
        }

        File[] gameDataFiles = gameDataDir.listFiles();
        if(gameDataFiles == null) {
            LOGGER.log(Level.WARNING, "Unable to cleanup launch resources: game data directory is empty");
            return false;
        }

        List<File> gameDataFilesList = new ArrayList<>(Arrays.asList(gameDataFiles));
        List<File> toRemove = new ArrayList<>();
        for(File f : gameDataFilesList) {
            if(f.getName().equals(Config.MANIFEST_FILE_NAME) || gameDataManifests.contains(f.getName()) || f.getName().startsWith(prefixMods) || f.getName().startsWith(prefixSaves)) {
                toRemove.add(f);
            }
        }
        gameDataFilesList.removeAll(toRemove);

        List<File> remainingFiles = removeIncludedFiles(List.of(instanceData.getSavesComponent(), instanceData.getModsComponent().getKey(), instanceData.getOptionsComponent(), instanceData.getResourcepacksComponent()), gameDataFilesList);
        if(remainingFiles == null) {
            LOGGER.log(Level.WARNING, "Unable to cleanup launch resources: unable to remove included files");
            return false;
        }

        File includedFilesDir = new File(instanceData.getInstance().getKey().getDirectory() + Config.INCLUDED_FILES_DIR);
        if(includedFilesDir.exists()) {
            if(!FileUtil.deleteDir(includedFilesDir)) {
                LOGGER.log(Level.WARNING, "Unable to cleanup launch resources: unable to delete instance included files directory");
                return false;
            }
        }
        if(!includedFilesDir.mkdir()) {
            LOGGER.log(Level.WARNING, "Unable to cleanup launch resources: unable to create instance included files directory");
            return false;
        }
        for(File f : remainingFiles) {
            if(instanceData.getInstance().getValue().getIgnoredFiles().contains(f.getName())) {
                try {
                    Files.delete(Path.of(f.getPath()));
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Unable to cleanup launch resources: unable to delete file: " + f.getPath(), e);
                    return false;
                }
            }
            else {
                try {
                    Files.move(Path.of(f.getPath()), Path.of(instanceData.getInstance().getKey().getDirectory() + Config.INCLUDED_FILES_DIR + "/" + f.getName()), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Unable to cleanup launch resources: unable to move file: " + f.getPath(), e);
                    return false;
                }
            }
        }

        return true;
    }

    public static List<File> removeIncludedFiles(List<LauncherManifest> components, List<File> files) {
        for(LauncherManifest component : components) {
            if(component.getIncludedFiles() == null || component.getIncludedFiles().isEmpty()) {
                continue;
            }
            files = removeIncludedFiles(component, files);
            if(files == null) {
                LOGGER.log(Level.WARNING, "Unable to remove included files: component_type=" + component.getType().name().toLowerCase() + " component=" + component.getId());
                return null;
            }
        }
        return files;
    }

    public static List<File> removeIncludedFiles(LauncherManifest component, List<File> files) {
        File includedFilesDir = new File(component.getDirectory() + Config.INCLUDED_FILES_DIR);
        if(includedFilesDir.exists()) {
            if(!FileUtil.deleteDir(includedFilesDir)) {
                LOGGER.log(Level.WARNING, "Unable to remove included files: unable to delete included files directory: component_type=" + component.getType().name().toLowerCase() + " component=" + component.getId());
                return null;
            }
        }
        if(!includedFilesDir.mkdirs()) {
            LOGGER.log(Level.WARNING, "Unable to remove included files: unable to create included files directory: component_type=" + component.getType().name().toLowerCase() + " component=" + component.getId());
            return null;
        }
        List<File> result = new ArrayList<>();
        for(File f : files) {
            String fName = f.isDirectory() ? f.getName() + "/" : f.getName();
            boolean found = false;
            for(String i : component.getIncludedFiles()) {
                if(FormatUtils.matches(fName, i)) {
                    try {
                        Files.move(Path.of(f.getPath()), Path.of(component.getDirectory() + Config.INCLUDED_FILES_DIR + "/" + f.getName()), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Unable to remove included file: file=" + f.getAbsolutePath(), e);
                        return null;
                    }
                    found = true;
                    break;
                }
            }
            if(!found) {
                result.add(f);
            }
        }
        return result;
    }

    public static boolean prepareResources(InstanceData instanceData, String gameDataPath) {

        if(!addIncludedFiles(instanceData.getInstance().getKey(), gameDataPath)) {
            LOGGER.log(Level.WARNING, "Unable to prepare launch resources: included files copy for instance failed");
            return false;
        }

        if(!addIncludedFiles(instanceData.getOptionsComponent(), gameDataPath)) {
            LOGGER.log(Level.WARNING, "Unable to prepare launch resources: included files copy for options failed");
            return false;
        }

        if(!addIncludedFiles(instanceData.getResourcepacksComponent(), gameDataPath)) {
            LOGGER.log(Level.WARNING, "Unable to prepare launch resources: included files copy for resourcepacks failed");
            return false;
        }

        try {
            Files.move(Path.of(instanceData.getSavesComponent().getDirectory()), Path.of(gameDataPath + "saves"));
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unable to prepare launch resources: rename saves file failed", e);
            return false;
        }
        instanceData.getSavesComponent().setDirectory(gameDataPath + "saves/");

        if(instanceData.getModsComponent() != null) {
            if(!addIncludedFiles(instanceData.getModsComponent().getKey(), gameDataPath)) {
                LOGGER.log(Level.WARNING, "Unable to prepare launch resources: included files copy for mods failed");
                return false;
            }

            try {
                Files.move(Path.of(instanceData.getModsComponent().getKey().getDirectory()), Path.of(gameDataPath + "mods"));
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Unable to prepare launch resources: rename mods file failed", e);
                return false;
            }
            instanceData.getModsComponent().getKey().setDirectory(gameDataPath + "mods/");
        }

        if(!addIncludedFiles(instanceData.getSavesComponent(), gameDataPath)) {
            LOGGER.log(Level.WARNING, "Unable to prepare launch resources: included files copy for saves failed");
            return false;
        }

        LOGGER.log(Level.INFO, "Prepared resources for launch");
        return true;
    }

    public static String createStartCommand(InstanceData instanceData, String gameDataDir, String assetsDir, String libraryDir, User minecraftUser) {
        if(instanceData.getVersionComponents() == null || instanceData.getVersionComponents().isEmpty() || instanceData.getJavaComponent() == null || instanceData.getResourcepacksComponent() == null || libraryDir == null) {
            LOGGER.log(Level.WARNING, "Unable to create start command: unmet preconditions");
            return null;
        }

        String assetsIndex = null;
        for(Pair<LauncherManifest, LauncherVersionDetails> v : instanceData.getVersionComponents()) {
            if(v.getValue().getAssets() != null && !v.getValue().getAssets().isBlank()) {
                assetsIndex = v.getValue().getAssets();
                break;
            }
        }
        if(assetsIndex == null) {
            LOGGER.log(Level.WARNING, "Unable to create start command: unable to determine asset index");
            return null;
        }

        String mainClass = null;
        for(Pair<LauncherManifest, LauncherVersionDetails> v : instanceData.getVersionComponents()) {
            if(v.getValue().getMainFile() != null && !v.getValue().getMainFile().isBlank()) {
                mainClass = v.getValue().getMainClass();
                break;
            }
        }
        if(mainClass == null) {
            LOGGER.log(Level.WARNING, "Unable to create start command: unable to determine main class");
            return null;
        }

        List<String> libraries = new ArrayList<>();
        for(Pair<LauncherManifest, LauncherVersionDetails> v : instanceData.getVersionComponents()) {
            for(String l : v.getValue().getLibraries()) {
                libraries.add(libraryDir + l);
            }
        }
        if(libraries.isEmpty()) {
            LOGGER.log(Level.WARNING, "Unable to create start command: unable to determine libraries");
            return null;
        }
        for(Pair<LauncherManifest, LauncherVersionDetails> v : instanceData.getVersionComponents()) {
            if(v.getValue().getMainFile() == null ||v.getValue().getMainFile().isBlank()) {
                LOGGER.log(Level.WARNING, "Unable to create start command: unable to determine main file: version=" + v.getKey().getId());
                return null;
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

        StringBuilder sb = new StringBuilder(instanceData.getJavaComponent().getDirectory() + "bin" + "/" + "java ");
        for(Pair<LauncherManifest, LauncherVersionDetails> v : instanceData.getVersionComponents()) {
            if(!appendArguments(v.getValue().getJvmArguments(), instanceData, minecraftUser, gameDataDir, assetsDir, assetsIndex, libraries, mainClass, resX, resY, sb)) {
                LOGGER.log(Level.WARNING, "Unable to create start command: unable to append jvm arguments: version=" + v.getKey().getId());
                return null;
            }
        }
        for(Pair<LauncherManifest, LauncherVersionDetails> v : instanceData.getVersionComponents()) {
            if(!appendArguments(v.getValue().getGameArguments(), instanceData, minecraftUser, gameDataDir, assetsDir, assetsIndex, libraries, mainClass, resX, resY, sb)) {
                LOGGER.log(Level.WARNING, "Unable to create start command: unable to append jvm arguments: version=" + v.getKey().getId());
                return null;
            }
        }

        return sb.toString();
    }

    private static boolean appendArguments(List<LauncherLaunchArgument> args, InstanceData instanceData, User minecraftUser, String gameDataDir, String assetsDir, String assetsIndex, List<String> libraries, String mainClass, String resX, String resY, StringBuilder sb) {
        for(LauncherLaunchArgument a : args) {
            if(!appendArgument(instanceData, minecraftUser, gameDataDir, assetsDir, assetsIndex, libraries, mainClass, resX, resY, sb, a)) {
                LOGGER.log(Level.WARNING, "Unable to append arguments: unable to append argument: argument=" + a);
                return false;
            }
        }
        return true;
    }

    private static boolean appendArgument(InstanceData instanceData, User minecraftUser, String gameDataDir, String assetsDir, String assetsIndex, List<String> libraries, String mainClass, String resX, String resY, StringBuilder sb, LauncherLaunchArgument a) {
        Map<String, String> replacement = new HashMap<>();
        if(a.isActive(instanceData.getInstance().getValue().getFeatures())) {
            for (String r : a.getReplacementValues()) {
                replacement.put(r, computeReplacementValue(r, gameDataDir, instanceData.getJavaComponent().getDirectory(), assetsDir, instanceData.getResourcepacksComponent().getDirectory(), assetsIndex, libraries, mainClass, minecraftUser, LauncherApplication.stringLocalizer.get("game.version_name", LauncherApplication.stringLocalizer.get("launcher.slug") ), LauncherApplication.stringLocalizer.get("game.version_type", LauncherApplication.stringLocalizer.get("launcher.name"), LauncherApplication.stringLocalizer.get("launcher.version") ), resX, resY));
                if(replacement.get(r) == null) {
                    LOGGER.log(Level.WARNING, "Unable to append argument: unable to compute replacement value: key=" + r);
                    return false;
                }
            }
            a.replace(replacement);
            if(!a.isFinished()) {
                LOGGER.log(Level.WARNING, "Unable to append argument: can't replace all values: argument=" + a.getArgument());
                return false;
            }
            sb.append(a.getParsedArgument()).append(" ");
        }
        return true;
    }

    public static String computeReplacementValue(String key, String gameDir, String javaDir, String assetsDir, String resourcepackDir, String assetsIndex, List<String> libraries, String mainClass, User minecraftUser, String versionName, String versionType, String resX, String resY) {
        switch(key) {
            case "natives_directory" -> {
                return "\"" + javaDir + "lib" + "\"";
            }
            case "launcher_name" -> {
                return "\"" + LauncherApplication.stringLocalizer.get("launcher.name") + "\"";
            }
            case "launcher_version" -> {
                return "\"" + LauncherApplication.stringLocalizer.get("launcher.version") + "\"";
            }
            case "classpath" -> {
                StringBuilder sb = new StringBuilder("\"");
                for(String l : libraries) {
                    sb.append(l).append(";");
                }
                return sb.substring(0, sb.length() - 1) + "\" " + mainClass;
            }
            case "auth_player_name" -> {
                return "\"" + minecraftUser.name() + "\"";
            }
            case "version_name" -> {
                return "\"" + versionName + "\"";
            }
            case "game_directory" -> {
                return "\"" + gameDir + "\"";
            }
            case "resourcepack_directory" -> {
                return "\"" + resourcepackDir + "\"";
            }
            case "assets_root" -> {
                return "\"" + assetsDir + "\"";
            }
            case "assets_index_name" -> {
                return "\"" + assetsIndex + "\"";
            }
            case "auth_uuid" -> {
                return "\"" + minecraftUser.uuid() + "\"";
            }
            case "auth_xuid" -> {
                return "\"" + minecraftUser.xuid() + "\"";
            }
            case "auth_access_token" -> {
                return "\"" + minecraftUser.accessToken() + "\"";
            }
            case "clientId" -> {
                return "\"" + minecraftUser.clientId() + "\"";
            }
            case "user_type" -> {
                return "\"" + minecraftUser.type() + "\"";
            }
            case "version_type" -> {
                return "\"" + versionType + "\"";
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

    public static boolean addIncludedFiles(LauncherManifest manifest, String gameDataDir) {
        if(manifest == null) {
            return false;
        }
        if(manifest.getIncludedFiles() != null) {
            File includedFilesDir = new File(manifest.getDirectory() + Config.INCLUDED_FILES_DIR);
            if(!includedFilesDir.isDirectory()) {
                LOGGER.log(Level.WARNING, "Unable to move included files: folder doesn't exist: manifestId=" + manifest.getId());
                return false;
            }
            File[] files = includedFilesDir.listFiles();
            if(files == null) {
                LOGGER.log(Level.WARNING, "Unable to move included files: unable to get files: manifestId=" + manifest.getId());
                return false;
            }
            boolean success = true;
            for(File f : files) {
                if(f.isFile()) {
                    try {
                        Files.copy(Path.of(f.getPath()), Path.of(gameDataDir + f.getName()), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Unable to move included files: unable to copy file: manifestId=" + manifest.getId(), e);
                        success = false;
                    }
                }
                else if(f.isDirectory() && !FileUtil.copyDirectory(f.getPath(), gameDataDir + f.getName())){
                    LOGGER.log(Level.WARNING, "Unable to move included files: unable to copy directory: manifestId=" + manifest.getId());
                    success = false;
                }
            }
            return success;
        }
        return true;
    }
}
