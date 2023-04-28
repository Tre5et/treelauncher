package net.treset.minecraftlauncher.launching;

import javafx.util.Pair;
import net.treset.mc_version_loader.launcher.LauncherInstanceDetails;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherModsDetails;
import net.treset.mc_version_loader.launcher.LauncherVersionDetails;
import net.treset.minecraftlauncher.config.Config;
import net.treset.minecraftlauncher.file_loading.LauncherFiles;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameLauncher {
    private static Logger LOGGER = Logger.getLogger(LauncherFiles.class.getName());

    public static boolean prepareResources(Pair<LauncherManifest, LauncherInstanceDetails> instance, LauncherFiles files) {
        if(!files.reloadAll()) {
            LOGGER.log(Level.WARNING, "Unable to prepare launch resources: file reload failed");
            return false;
        }
        StringBuilder launchCommand  = new StringBuilder();
        LauncherVersionDetails versionComponent = null;
        for(Pair<LauncherManifest, LauncherVersionDetails> v : files.getVersionComponents()) {
            if(Objects.equals(v.getKey().getId(), instance.getValue().getVersionComponent())) {
                versionComponent = v.getValue();
                break;
            }
        }
        if(versionComponent == null) {
            LOGGER.log(Level.WARNING, "Unable to prepare launch resources: unable to find version component: versionId=" + instance.getValue().getVersionComponent());
            return false;
        }
        LauncherManifest javaComponent = null;
        for(LauncherManifest j : files.getJavaComponents()) {
            if(Objects.equals(j.getId(), versionComponent.getJava())) {
                javaComponent = j;
                break;
            }
        }
        if(javaComponent == null) {
            LOGGER.log(Level.WARNING, "Unable to prepare launch resources: unable to find java component: javaId=" + versionComponent.getJava());
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

        LOGGER.log(Level.INFO, "Prepared resources for launch");
        return true;
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
