package net.treset.minecraftlauncher.creation;

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
    String librariesDir;
    public VersionCreator(String name,  Map<String, LauncherManifestType> typeConversion, LauncherManifest componentsManifest) {
        super(LauncherManifestType.VERSION_COMPONENT, null, null, name, typeConversion, null, null, componentsManifest);
    }

    public VersionCreator(LauncherManifest uses) {
        super(LauncherManifestType.VERSION_COMPONENT, uses, null, null, null, null, null, null);
    }

    @Override
    public String createComponent() {
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
                mcVersion.getJavaVersion().getComponent(),
                null,
                mcVersion.getMainClass(),
                null
        );
        if(!addArguments(details)) {
            LOGGER.warn("Failed to add arguments to version");
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

    private boolean addLibraries(LauncherVersionDetails details) {
        if(mcVersion.getLibraries() == null) {
            LOGGER.warn("Mc libraries is null");
            return false;
        }
        File baseDir = new File(librariesDir);
        if(!baseDir.isDirectory()) {
            LOGGER.warn("Libraries dir is not a directory");
            return false;
        }
        List<String> result = new ArrayList<>();
        for(MinecraftLibrary l : mcVersion.getLibraries()) {
            if(l == null || l.getDownloads() == null || l.getDownloads().getArtifacts() == null || l.getDownloads().getArtifacts().getUrl() == null || l.getDownloads().getArtifacts().getPath() == null) {
                LOGGER.debug("Invalid library in mc libraries");
                continue;
            }

            if(!MinecraftVersionFileDownloader.downloadVersionLibrary(l, baseDir)) {
                LOGGER.warn("Failed to download library: path={}", l.getDownloads().getArtifacts().getPath());
                return false;
            }

            result.add(l.getDownloads().getArtifacts().getPath());
        }
        details.setLibraries(result);
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
        return true;
    }

    private boolean addArguments(LauncherVersionDetails details) {
        details.setGameArguments(translateArguments(mcVersion.getLaunchArguments().getGame()));
        details.setJvmArguments(translateArguments(mcVersion.getLaunchArguments().getJvm()));
        if(details.getGameArguments() == null || details.getJvmArguments() == null) {
            LOGGER.warn("Failed to add arguments to version");
            return false;
        }
        return true;
    }

    private List<LauncherLaunchArgument> translateArguments(List<MinecraftLaunchArgument> args) {
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
        return result;
    }
}
