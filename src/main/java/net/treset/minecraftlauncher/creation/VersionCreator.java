package net.treset.minecraftlauncher.creation;

import javafx.util.Pair;
import net.treset.mc_version_loader.VersionLoader;
import net.treset.mc_version_loader.assets.AssetIndex;
import net.treset.mc_version_loader.fabric.FabricLibrary;
import net.treset.mc_version_loader.fabric.FabricProfile;
import net.treset.mc_version_loader.fabric.FabricVersionDetails;
import net.treset.mc_version_loader.files.AssetsFileDownloader;
import net.treset.mc_version_loader.files.FabricFileDownloader;
import net.treset.mc_version_loader.files.MinecraftVersionFileDownloader;
import net.treset.mc_version_loader.files.Sources;
import net.treset.mc_version_loader.format.FormatUtils;
import net.treset.mc_version_loader.launcher.LauncherLaunchArgument;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.mc_version_loader.launcher.LauncherVersionDetails;
import net.treset.mc_version_loader.minecraft.*;
import net.treset.minecraftlauncher.LauncherApplication;
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
    FabricProfile fabricProfile;
    LauncherFiles files;
    String librariesDir;
    public VersionCreator(Map<String, LauncherManifestType> typeConversion, LauncherManifest componentsManifest, MinecraftVersionDetails mcVersion, LauncherFiles files, String librariesDir) {
        super(LauncherManifestType.VERSION_COMPONENT, null, null, mcVersion.getId(), typeConversion, null, LauncherApplication.config.VERSION_DEFAULT_DETAILS, componentsManifest);
        this.mcVersion = mcVersion;
        this.files = files;
        this.librariesDir = librariesDir;
    }

    public VersionCreator(Map<String, LauncherManifestType> typeConversion, LauncherManifest componentsManifest, FabricVersionDetails fabricVersion, FabricProfile fabricProfile, LauncherFiles files, String librariesDir) {
        super(LauncherManifestType.VERSION_COMPONENT, null, null, fabricProfile.getId(), typeConversion, null, LauncherApplication.config.VERSION_DEFAULT_DETAILS, componentsManifest);
        this.fabricVersion = fabricVersion;
        this.fabricProfile = fabricProfile;
        this.files = files;
        this.librariesDir = librariesDir;
    }

    public VersionCreator(Pair<LauncherManifest, LauncherVersionDetails> uses) {
        super(LauncherManifestType.VERSION_COMPONENT, uses.getKey(), null, null, null, null, null, null);
    }

    @Override
    public String createComponent() {
        for(Pair<LauncherManifest, LauncherVersionDetails> v : files.getVersionComponents()) {
            if(v.getValue().getVersionId() != null && ((mcVersion != null && v.getValue().getVersionId().equals(mcVersion.getId())) || (fabricProfile != null && v.getValue().getVersionId().equals(fabricProfile.getId())))) {
                LOGGER.debug("Matching version already exists, using instead: versionId={}, usingId={}", v.getValue().getVersionId(), v.getKey().getId());
                this.setUses(v.getKey());
                return useComponent();
            }
        }

        String result = super.createComponent();
        if(result == null || getNewManifest() == null || (mcVersion == null && fabricVersion == null)) {
            LOGGER.warn("Failed to create version component: invalid data");
            attemptCleanup();
            return null;
        }

        if(!makeVersion()) {
            LOGGER.warn("Failed to create version component: failed to create mc version");
            attemptCleanup();
            return null;
        }
        LOGGER.debug("Created version component: id={}", getNewManifest().getId());
        return result;
    }

    @Override
    public String inheritComponent() {
        LOGGER.warn("Unable to inherit version: not supported");
        return null;
    }

    private boolean makeVersion() {
        if(fabricVersion != null) {
            return makeFabricVersion();
        }
        return makeMinecraftVersion();
    }

    private boolean makeFabricVersion() {
        if(fabricProfile == null || fabricProfile.getInheritsFrom() == null) {
            LOGGER.warn("Unable to create fabric version: no valid fabric profile");
            return false;
        }
        for(MinecraftVersion m : VersionLoader.getVersions()) {
            if(fabricProfile.getInheritsFrom().equals(m.getId())) {
                String mcJson = Sources.getFileFromUrl(m.getUrl());
                if(mcJson == null) {
                    LOGGER.warn("Unable to create fabric version: failed to download mc version: versionId={}", fabricProfile.getInheritsFrom());
                    return false;
                }
                VersionCreator mcCreator = new VersionCreator(getTypeConversion(), getComponentsManifest(), MinecraftVersionDetails.fromJson(mcJson), files, librariesDir);
                String dependsId = mcCreator.getId();
                if(dependsId == null) {
                    LOGGER.warn("Unable to create fabric version: failed to create mc version: versionId={}", fabricProfile.getInheritsFrom());
                    mcCreator.attemptCleanup();
                    return false;
                }

                LauncherVersionDetails details = new LauncherVersionDetails(
                        null,
                        dependsId,
                        null,
                        null,
                        null,
                        null,
                        fabricProfile.getMainClass(),
                        null,
                        fabricProfile.getId()
                );

                if(!addFabricArguments(details)) {
                    LOGGER.warn("Unable to create fabric version: failed to add fabric arguments: versionId={}", fabricProfile.getInheritsFrom());
                    mcCreator.attemptCleanup();
                    return false;
                }

                if(!addFabricLibraries(details)) {
                    LOGGER.warn("Unable to create fabric version: failed to add fabric libraries: versionId={}", fabricProfile.getInheritsFrom());
                    mcCreator.attemptCleanup();
                    return false;
                }

                if(!addFabricFile(details)) {
                    LOGGER.warn("Unable to create fabric version: failed to add fabric file: versionId={}", fabricProfile.getInheritsFrom());
                    mcCreator.attemptCleanup();
                    return false;
                }

                if(!details.writeToFile(getNewManifest().getDirectory() + getNewManifest().getDetails())) {
                    LOGGER.warn("Unable to create fabric version: failed to write version details: versionId={}", fabricProfile.getInheritsFrom());
                    mcCreator.attemptCleanup();
                    return false;
                }

                LOGGER.debug("Created fabric version: id={}", getNewManifest().getId());
                return true;
            }
        }
        LOGGER.warn("Unable to create fabric version: failed to find mc version: versionId={}", fabricProfile.getInheritsFrom());
        return false;
    }

    private boolean addFabricFile(LauncherVersionDetails details) {
        File baseDir = new File(getNewManifest().getDirectory());
        if(!baseDir.isDirectory()) {
            LOGGER.warn("Failed to add fabric file: base dir is not a directory");
            return false;
        }
        if(!FabricFileDownloader.downloadFabricLoader(baseDir, fabricVersion.getLoader())) {
            LOGGER.warn("Failed to add fabric file: failed to download fabric loader");
            return false;
        }
        details.setMainFile(LauncherApplication.config.FABRIC_DEFAULT_CLIENT_FILENAME);
        LOGGER.debug("Added fabric file: mainFile={}", details.getMainFile());
        return true;
    }

    private boolean addFabricLibraries(LauncherVersionDetails details) {
        if(fabricProfile.getLibraries() == null) {
            LOGGER.warn("Unable to add fabric libraries: no libraries");
            return false;
        }
        File baseDir = new File(librariesDir);
        if(!baseDir.isDirectory() && !baseDir.mkdirs()) {
            LOGGER.warn("Unable to add fabric libraries: failed to create libraries directory: path={}", librariesDir);
            return false;
        }
        List<FabricLibrary> clientLibs = new ArrayList<>(fabricProfile.getLibraries()).stream().filter(f -> !FormatUtils.matches(f.getName(), ":fabric-loader:")).toList();

        List<String> libs = FabricFileDownloader.downloadFabricLibraries(baseDir, clientLibs);
        if(libs == null) {
            LOGGER.warn("Unable to add fabric libraries: failed to download libraries");
            return false;
        }

        details.setLibraries(libs);
        LOGGER.debug("Added fabric libraries");
        return true;
    }

    private boolean addFabricArguments(LauncherVersionDetails details) {
        details.setJvmArguments(translateArguments(fabricProfile.getLaunchArguments().getJvm(), LauncherApplication.config.FABRIC_DEFAULT_JVM_ARGUMENTS));
        details.setGameArguments(translateArguments(fabricProfile.getLaunchArguments().getGame(), LauncherApplication.config.FABRIC_DEFAULT_GAME_ARGUMENTS));
        if(details.getJvmArguments() == null || details.getGameArguments() == null) {
            LOGGER.warn("Failed to add fabric arguments to version");
            return false;
        }
        LOGGER.debug("Added fabric arguments");
        return true;
    }

    private boolean makeMinecraftVersion() {
        if(!downloadAssets()) {
            LOGGER.warn("Failed to download assets");
            return false;
        }

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

        LOGGER.debug("Created minecraft version: id={}", getNewManifest().getId());
        return true;
    }

    private boolean downloadAssets() {
        LOGGER.debug("Downloading assets...");
        String assetIndexUrl = mcVersion.getAssetIndex().getUrl();
        AssetIndex index = AssetIndex.fromJson(Sources.getFileFromUrl(assetIndexUrl));
        if(index.getObjects() == null || index.getObjects().isEmpty()) {
            LOGGER.warn("Unable to download assets: invalid contents");
            return false;
        }
        File baseDir = new File(LauncherApplication.config.BASE_DIR + files.getLauncherDetails().getAssetsDir());
        if(!AssetsFileDownloader.downloadAssets(baseDir, index, assetIndexUrl, false)) {
            LOGGER.warn("Unable to download assets: failed to download assets");
            return false;
        }
        LOGGER.debug("Downloaded assets");
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
                LOGGER.debug("Using existing java component: id={}", j.getId());
                return true;
            }
        }

        JavaComponentCreator javaCreator = new JavaComponentCreator(javaName, getTypeConversion(), files.getJavaManifest());
        details.setJava(javaCreator.getId());

        if(details.getJava() == null) {
            LOGGER.warn("Unable to add java component: failed to create java component");
            javaCreator.attemptCleanup();
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

        List<String> result = MinecraftVersionFileDownloader.downloadVersionLibraries(mcVersion.getLibraries(), baseDir, List.of());
        if(result == null) {
            LOGGER.warn("Unable to add libraries: failed to download libraries");
            return false;
        }

        details.setLibraries(result);
        LOGGER.debug("Added libraries: {}", result);
        return true;
    }

    private boolean addFile(LauncherVersionDetails details) {
        File baseDir = new File(getNewManifest().getDirectory());
        if(!baseDir.isDirectory()) {
            LOGGER.warn("Unable to add file: base dir is not a directory");
            return false;
        }
        if(!MinecraftVersionFileDownloader.downloadVersionDownload(mcVersion.getDownloads().getClient(), baseDir)) {
            LOGGER.warn("Unable to add file: Failed to download client: url={}", mcVersion.getDownloads().getClient().getUrl());
            return false;
        }
        String[] urlParts = mcVersion.getDownloads().getClient().getUrl().split("/");
        details.setMainFile(urlParts[urlParts.length - 1]);
        LOGGER.debug("Added file: mainFile={}", details.getMainFile());
        return true;
    }

    private boolean addArguments(LauncherVersionDetails details) {
        details.setGameArguments(translateArguments(mcVersion.getLaunchArguments().getGame(), LauncherApplication.config.MINECRAFT_DEFAULT_GAME_ARGUMENTS));
        details.setJvmArguments(translateArguments(mcVersion.getLaunchArguments().getJvm(), LauncherApplication.config.MINECRAFT_DEFAULT_JVM_ARGUMENTS));
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
