package net.treset.minecraftlauncher.launching;

import javafx.util.Pair;
import net.hycrafthd.minecraft_authenticator.login.User;
import net.treset.mc_version_loader.launcher.*;
import net.treset.minecraftlauncher.config.Config;
import net.treset.minecraftlauncher.file_loading.LauncherFiles;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameLauncher {
    private static Logger LOGGER = Logger.getLogger(LauncherFiles.class.getName());

    public static boolean prepareResources(Pair<LauncherManifest, LauncherInstanceDetails> instance, LauncherFiles files, User minecraftUser) {
        if(!files.reloadAll()) {
            LOGGER.log(Level.WARNING, "Unable to prepare launch resources: file reload failed");
            return false;
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
            LOGGER.log(Level.WARNING, "Unable to prepare launch resources: unable to find version component: versionId=" + instance.getValue().getVersionComponent());
            return false;
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
                LOGGER.log(Level.WARNING, "Unable to prepare launch resources: unable to find dependent version component");
                return false;
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
            LOGGER.log(Level.WARNING, "Unable to prepare launch resources: unable to find suitable java component");
            return false;
        }
        LauncherManifest optionsComponent = null;
        for(LauncherManifest o : files.getOptionsComponents()) {
            if(Objects.equals(o.getId(), instance.getValue().getOptionsComponent())) {
                optionsComponent = o;
                break;
            }
        }
        if(optionsComponent == null) {
            LOGGER.log(Level.WARNING, "Unable to prepare launch resources: unable to find options component: optionsId=" + instance.getValue().getOptionsComponent());
            return false;
        }
        LauncherManifest resourcepacksComponent = null;
        for(LauncherManifest r : files.getResourcepackComponents()) {
            if(Objects.equals(r.getId(), instance.getValue().getResourcepacksComponent())) {
                resourcepacksComponent = r;
                break;
            }
        }
        if(resourcepacksComponent == null) {
            LOGGER.log(Level.WARNING, "Unable to prepare launch resources: unable to find resourcepacks component: resourcepacksId=" + instance.getValue().getResourcepacksComponent());
            return false;
        }
        LauncherManifest savesComponent = null;
        for(LauncherManifest s : files.getSavesComponents()) {
            if(Objects.equals(s.getId(), instance.getValue().getSavesComponent())) {
                savesComponent = s;
                break;
            }
        }
        if(savesComponent == null) {
            LOGGER.log(Level.WARNING, "Unable to prepare launch resources: unable to find saves component: savesId=" + instance.getValue().getSavesComponent());
            return false;
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
                LOGGER.log(Level.WARNING, "Unable to prepare launch resources: unable to find mods component: modsId=" + instance.getValue().getModsComponent());
                return false;
            }
        }

        if(!copyIncludedFiles(savesComponent, files.getGameDetailsManifest())) {
            LOGGER.log(Level.WARNING, "Unable to prepare launch resources: included files copy for saves failed");
            return false;
        }
        try {
            Files.move(Path.of(savesComponent.getDirectory()), Path.of(files.getGameDetailsManifest().getDirectory() + "saves"));
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unable to prepare launch resources: rename saves file failed", e);
            return false;
        }

        if(modsComponent != null) {
            if(!copyIncludedFiles(modsComponent.getKey(), files.getGameDetailsManifest())) {
                LOGGER.log(Level.WARNING, "Unable to prepare launch resources: included files copy for mods failed");
                return false;
            }

            try {
                Files.move(Path.of(modsComponent.getKey().getDirectory()), Path.of(files.getGameDetailsManifest().getDirectory() + "mods"));
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Unable to prepare launch resources: rename mods file failed", e);
                return false;
            }
        }

        if(!copyIncludedFiles(optionsComponent, files.getGameDetailsManifest())) {
            LOGGER.log(Level.WARNING, "Unable to prepare launch resources: included files copy for options failed");
            return false;
        }

        if(!copyIncludedFiles(resourcepacksComponent, files.getGameDetailsManifest())) {
            LOGGER.log(Level.WARNING, "Unable to prepare launch resources: included files copy for resourcepacks failed");
            return false;
        }

        if(!copyIncludedFiles(instance.getKey(), files.getGameDetailsManifest())) {
            LOGGER.log(Level.WARNING, "Unable to prepare launch resources: included files copy for instance failed");
            return false;
        }

        String command = createStartCommand(instance.getValue(), versionComponents, javaComponent, resourcepacksComponent, files.getLauncherDetails(), minecraftUser);
        if(command == null) {
            LOGGER.log(Level.WARNING, "Unable to prepare launch resources: unable to create start command");
            return false;
        }

        LOGGER.log(Level.INFO, "Prepared resources for launch");
        return true;
    }

    public static String createStartCommand(LauncherInstanceDetails instance, List<Pair<LauncherManifest, LauncherVersionDetails>> versions, LauncherManifest java, LauncherManifest resourcepacks, LauncherDetails details, User minecraftUser) {
        if(versions == null || versions.isEmpty() || java == null || resourcepacks == null || details == null) {
            LOGGER.log(Level.WARNING, "Unable to create start command: unmet preconditions");
            return null;
        }

        String assetsIndex = null;
        for(Pair<LauncherManifest, LauncherVersionDetails> v : versions) {
            if(v.getValue().getAssets() != null && !v.getValue().getAssets().isBlank()) {
                assetsIndex = v.getValue().getAssets();
                break;
            }
        }
        if(assetsIndex == null) {
            LOGGER.log(Level.WARNING, "Unable to create start command: unable to determine asset index");
            return null;
        }

        String mainFile = null;
        String mainClass = null;
        for(Pair<LauncherManifest, LauncherVersionDetails> v : versions) {
            if(v.getValue().getMainFile() != null && !v.getValue().getMainFile().isBlank()) {
                mainFile = v.getKey().getDirectory() + v.getValue().getMainFile();
                mainClass = v.getValue().getMainClass();
                break;
            }
        }
        if(mainFile == null || mainClass == null) {
            LOGGER.log(Level.WARNING, "Unable to create start command: unable to determine main file");
            return null;
        }

        List<String> libraries = new ArrayList<>();
        for(Pair<LauncherManifest, LauncherVersionDetails> v : versions) {
            for(String l : v.getValue().getLibraries()) {
                libraries.add(Config.BASE_DIR + details.getLibrariesDir() + "/" + l);
            }
        }
        if(libraries.isEmpty()) {
            LOGGER.log(Level.WARNING, "Unable to create start command: unable to determine libraries");
            return null;
        }
        libraries.add(mainFile);

        String resX = null;
        String resY = null;
        for(LauncherFeature f : instance.getFeatures()) {
            if(f.getFeature().equals("resolution_x")) {
                resX = f.getValue();
            }
            if(f.getFeature().equals("resolution_y")) {
                resY = f.getValue();
            }
        }

        StringBuilder sb = new StringBuilder(java.getDirectory() + "bin" + "/" + "java ");
        for(Pair<LauncherManifest, LauncherVersionDetails> v : versions) {
            if(!appendArguments(v.getValue().getJvmArguments(), instance, java, details, resourcepacks, minecraftUser, assetsIndex, libraries, mainClass, resX, resY, sb)) {
                LOGGER.log(Level.WARNING, "Unable to create start command: unable to append jvm arguments: version=" + v.getKey().getId());
                return null;
            }
        }
        for(Pair<LauncherManifest, LauncherVersionDetails> v : versions) {
            if(!appendArguments(v.getValue().getGameArguments(), instance, java, details, resourcepacks, minecraftUser, assetsIndex, libraries, mainClass, resX, resY, sb)) {
                LOGGER.log(Level.WARNING, "Unable to create start command: unable to append jvm arguments: version=" + v.getKey().getId());
                return null;
            }
        }

        return sb.toString();
    }

    private static boolean appendArguments(List<LauncherLaunchArgument> args, LauncherInstanceDetails instance, LauncherManifest java, LauncherDetails details, LauncherManifest resourcepacks, User minecraftUser, String assetsIndex, List<String> libraries, String mainClass, String resX, String resY, StringBuilder sb) {
        for(LauncherLaunchArgument a : args) {
            if(!appendArgument(instance, java, details, resourcepacks, minecraftUser, assetsIndex, libraries, mainClass, resX, resY, sb, a)) {
                LOGGER.log(Level.WARNING, "Unable to append arguments: unable to append argument: argument=" + a);
                return false;
            }
        }
        return true;
    }

    private static boolean appendArgument(LauncherInstanceDetails instance, LauncherManifest java, LauncherDetails details, LauncherManifest resourcepacks, User minecraftUser, String assetsIndex, List<String> libraries, String mainClass, String resX, String resY, StringBuilder sb, LauncherLaunchArgument a) {
        Map<String, String> replacement = new HashMap<>();
        if(a.isActive(instance.getFeatures())) {
            for (String r : a.getReplacementValues()) {
                replacement.put(r, computeReplacementValue(r, details.getGamedataDir(), java.getDirectory(), Config.BASE_DIR + details.getAssetsDir(), resourcepacks.getDirectory(), assetsIndex, libraries, mainClass, minecraftUser, "great version", "modded probably", resX, resY));
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
                return "\"" + Config.LAUNCHER_NAME + "\"";
            }
            case "launcher_version" -> {
                return "\"" + Config.LAUNCHER_VERSION + "\"";
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

    public static boolean copyIncludedFiles(LauncherManifest manifest, LauncherManifest gameDataManifest) {
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
            boolean success = true;
            for(File f : files) {
                try {
                    Files.copy(f.toPath(), Path.of(gameDataManifest.getDirectory() + f.getName()), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Unable to move included files: unable to move file: manifestId=" + manifest.getId(), e);
                    success = false;
                }
            }
            return success;
        }
        return true;
    }
}
