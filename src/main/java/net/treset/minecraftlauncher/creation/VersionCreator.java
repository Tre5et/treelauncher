package net.treset.minecraftlauncher.creation;

import javafx.util.Pair;
import net.treset.mc_version_loader.fabric.FabricVersionDetails;
import net.treset.mc_version_loader.files.MinecraftVersionFileDownloader;
import net.treset.mc_version_loader.launcher.LauncherLaunchArgument;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.mc_version_loader.launcher.LauncherVersionDetails;
import net.treset.mc_version_loader.minecraft.MinecraftLaunchArgument;
import net.treset.mc_version_loader.minecraft.MinecraftLibrary;
import net.treset.mc_version_loader.minecraft.MinecraftRule;
import net.treset.mc_version_loader.minecraft.MinecraftVersionDetails;
import net.treset.minecraftlauncher.config.Config;
import net.treset.minecraftlauncher.data.LauncherFiles;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VersionCreator extends GenericComponentCreator {
    private static final Logger LOGGER = LogManager.getLogger(VersionCreator.class);

    MinecraftVersionDetails mcVersion;
    FabricVersionDetails fabricVersion;
    LauncherFiles files;
    String librariesDir;
    public VersionCreator(String name,  Map<String, LauncherManifestType> typeConversion, LauncherManifest componentsManifest, MinecraftVersionDetails mcVersion, LauncherFiles files, String librariesDir) {
        super(LauncherManifestType.VERSION_COMPONENT, null, null, name, typeConversion, null, Config.VERSION_DEFAULT_DETAILS, componentsManifest);
        this.mcVersion = mcVersion;
        this.files = files;
        this.librariesDir = librariesDir;
    }

    public VersionCreator(Pair<LauncherManifest, LauncherVersionDetails> uses) {
        super(LauncherManifestType.VERSION_COMPONENT, uses.getKey(), null, null, null, null, null, null);
    }

    @Override
    public String createComponent() {
        for(Pair<LauncherManifest, LauncherVersionDetails> v : files.getVersionComponents()) {
            if(v.getValue().getVersionId() != null && v.getValue().getVersionId().equals(mcVersion.getId())) {
                LOGGER.debug("Matching version already exists, using instead: versionId={}, usingId={}", mcVersion.getId(), v.getKey().getId());
                this.setUses(v.getKey());
                return useComponent();
            }
        }

        String result = super.createComponent();
        if(result == null || getNewManifest() == null || mcVersion == null) {
            LOGGER.warn("Failed to create version component: invalid data");
            return null;
        }

        if(!makeVersion()) {
            LOGGER.warn("Failed to create version component: failed to create mc version");
            return null;
        }
        return result;
    }

    @Override
    public String inheritComponent() {
        LOGGER.warn("Unable to inherit version: not supported");
        return null;
    }

    private boolean makeVersion() {
        // TODO: fabric version

        LauncherVersionDetails details = new LauncherVersionDetails(
                mcVersion.getAssets(),
                null,
                null,
                null,
                null,
                null,
                mcVersion.getMainClass(),
                null,
                mcVersion.getId()
        );
        if(!addArguments(details)) {
            LOGGER.warn("Failed to add arguments to version");
            return false;
        }
        if(!addJava(details)) {
            LOGGER.warn("Failed to add java to version");
            return false;
        }
        if(!addLibraries(details)) {
            LOGGER.warn("Failed to add libraries to version");
            return false;
        }
        if(!addFile(details)) {
            LOGGER.warn("Failed to add file to version");
            return false;
        }

        if(!details.writeToFile(getNewManifest().getDirectory() + getNewManifest().getDetails())) {
            LOGGER.warn("Failed to write version to file");
            return false;
        }
        return true;
    }

    private boolean addJava(LauncherVersionDetails details) {
        String javaName = mcVersion.getJavaVersion().getComponent();

        if(javaName == null) {
            LOGGER.warn("Unable to add java component: java name is null");
            return false;
        }
        for(LauncherManifest j : files.getJavaComponents()) {
            if(j != null && javaName.equals(j.getName())) {
                details.setJava(j.getId());
                return true;
            }
        }

        details.setJava(new JavaComponentCreator(javaName, getTypeConversion(), files.getJavaManifest()).createComponent());

        if(details.getJava() == null) {
            LOGGER.warn("Unable to add java component: failed to create java component");
            return false;
        }
        return true;
    }

    private boolean addLibraries(LauncherVersionDetails details) {
        if(mcVersion.getLibraries() == null) {
            LOGGER.warn("Unable to add libraries: libraries is null");
            return false;
        }
        File baseDir = new File(librariesDir);
        if(!baseDir.isDirectory()) {
            LOGGER.warn("Unable to add libraries: libraries dir is not a directory");
            return false;
        }
        List<String> result = new ArrayList<>();
        for(MinecraftLibrary l : mcVersion.getLibraries()) {
            if(l == null || l.getDownloads() == null || l.getDownloads().getArtifacts() == null || l.getDownloads().getArtifacts().getUrl() == null || l.getDownloads().getArtifacts().getPath() == null) {
                LOGGER.debug("Inconsistency while adding libraries: invalid library in mc libraries");
                continue;
            }

            if(!MinecraftVersionFileDownloader.downloadVersionLibrary(l, baseDir)) {
                LOGGER.warn("Unable to add libraries: failed to download library: path={}", l.getDownloads().getArtifacts().getPath());
                return false;
            }

            result.add(l.getDownloads().getArtifacts().getPath());
        }
        details.setLibraries(result);
        LOGGER.debug("Added libraries: {}", result);
        return true;
    }

    private boolean addFile(LauncherVersionDetails details) {
        File baseDir = new File(getNewManifest().getDirectory());
        if(!baseDir.isDirectory()) {
            LOGGER.warn("Version dir is not a directory");
            return false;
        }
        if(!MinecraftVersionFileDownloader.downloadVersionDownload(mcVersion.getDownloads().getClient(), baseDir)) {
            LOGGER.warn("Failed to download client: url={}", mcVersion.getDownloads().getClient().getUrl());
            return false;
        }
        String[] urlParts = mcVersion.getDownloads().getClient().getUrl().split("/");
        details.setMainFile(urlParts[urlParts.length - 1]);
        LOGGER.debug("Added main file: {}", details.getMainFile());
        return true;
    }

    private boolean addArguments(LauncherVersionDetails details) {
        details.setGameArguments(translateArguments(mcVersion.getLaunchArguments().getGame(), Config.MINECRAFT_DEFAULT_GAME_ARGUMENTS));
        details.setJvmArguments(translateArguments(mcVersion.getLaunchArguments().getJvm(), Config.MINECRAFT_DEFAULT_JVM_ARGUMENTS));
        if(details.getGameArguments() == null || details.getJvmArguments() == null) {
            LOGGER.warn("Failed to add arguments to version");
            return false;
        }
        LOGGER.debug("Added arguments");
        return true;
    }

    private List<LauncherLaunchArgument> translateArguments(List<MinecraftLaunchArgument> args, List<LauncherLaunchArgument> defaultArgs) {
        if(args == null) {
            LOGGER.warn("Mc arguments is null");
            return List.of();
        }
        List<LauncherLaunchArgument> result = new ArrayList<>();
        for(MinecraftLaunchArgument a : args) {
            String feature = null;
            String osName = null;
            String osVersion = null;
            String osArch = null;
            if(a == null) {
                LOGGER.debug("Null argument in mc arguments");
                continue;
            }
            if(a.isGated()) {
                for(MinecraftRule r : a.getRules()) {
                    if(r.getFeatures() != null) {
                        if(r.getFeatures().isHasCustomResolution()) {
                            feature = "resolution_x";
                        } else if(r.getFeatures().isDemoUser()) {
                            feature = "is_demo_user";
                        }
                    }
                    if(r.getOs() != null) {
                        if(r.getOs().getName() != null) {
                            osName = r.getOs().getName();
                        }
                        if(r.getOs().getVersion() != null) {
                            osVersion = r.getOs().getVersion();
                        }
                        if(r.getOs().getArch() != null) {
                            osArch = r.getOs().getArch();
                        }
                    }
                }
            }
            result.add(new LauncherLaunchArgument(a.getName(), feature, osName, osVersion, osArch));
        }
        result.addAll(defaultArgs);
        LOGGER.debug("Translated arguments: {}", result);
        return result;
    }
}
