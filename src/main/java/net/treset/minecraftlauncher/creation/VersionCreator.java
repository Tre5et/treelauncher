package net.treset.minecraftlauncher.creation;

import javafx.util.Pair;
import net.treset.mc_version_loader.VersionLoader;
import net.treset.mc_version_loader.assets.AssetIndex;
import net.treset.mc_version_loader.exception.FileDownloadException;
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
import net.treset.minecraftlauncher.util.exception.ComponentCreationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
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
    public String createComponent() throws ComponentCreationException {
        for(Pair<LauncherManifest, LauncherVersionDetails> v : files.getVersionComponents()) {
            if(v.getValue().getVersionId() != null && ((mcVersion != null && v.getValue().getVersionId().equals(mcVersion.getId())) || (fabricProfile != null && v.getValue().getVersionId().equals(fabricProfile.getId())))) {
                LOGGER.debug("Matching version already exists, using instead: versionId={}, usingId={}", v.getValue().getVersionId(), v.getKey().getId());
                this.setUses(v.getKey());
                return useComponent();
            }
        }

        String result = super.createComponent();
        if(result == null || getNewManifest() == null || (mcVersion == null && fabricVersion == null)) {
            attemptCleanup();
            throw new ComponentCreationException("Failed to create version component: invalid data");
        }

        makeVersion();

        LOGGER.debug("Created version component: id={}", getNewManifest().getId());
        return result;
    }

    @Override
    public String inheritComponent() throws ComponentCreationException {
        throw new ComponentCreationException("Unable to inherit version: not supported");
    }

    private void makeVersion() throws ComponentCreationException {
        if(fabricVersion != null) {
            makeFabricVersion();
        }
        makeMinecraftVersion();
    }

    private void makeFabricVersion() throws ComponentCreationException {
        if(fabricProfile == null || fabricProfile.getInheritsFrom() == null) {
            throw new ComponentCreationException("Unable to create fabric version: no valid fabric profile");
        }
        List<MinecraftVersion> versions;
        try {
            versions = VersionLoader.getVersions();
        } catch(FileDownloadException e) {
            throw new ComponentCreationException("Unable to create fabric version: failed to get mc versions", e);
        }
        for(MinecraftVersion m : versions) {
            if(fabricProfile.getInheritsFrom().equals(m.getId())) {
                String mcJson = null;
                try {
                    mcJson = Sources.getFileFromUrl(m.getUrl());
                } catch (FileDownloadException e) {
                    throw new ComponentCreationException("Unable to create fabric version: failed to download mc version details: versionId=" + fabricProfile.getInheritsFrom(), e);
                }
                VersionCreator mcCreator = new VersionCreator(getTypeConversion(), getComponentsManifest(), MinecraftVersionDetails.fromJson(mcJson), files, librariesDir);
                String dependsId;
                try {
                    dependsId = mcCreator.getId();
                } catch (ComponentCreationException e) {
                    throw new ComponentCreationException("Unable to create fabric version: failed to create mc version: versionId=" + fabricProfile.getInheritsFrom(), e);
                }

                LauncherVersionDetails details = new LauncherVersionDetails(
                        fabricProfile.getInheritsFrom(),
                        "fabric",
                        fabricVersion.getLoader().getVersion(),
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

                try {
                    addFabricArguments(details);
                    addFabricLibraries(details);
                    addFabricFile(details);
                } catch (ComponentCreationException e) {
                    mcCreator.attemptCleanup();
                    attemptCleanup();
                    throw new ComponentCreationException("Unable to create fabric version: versionId=" + fabricProfile.getInheritsFrom(), e);
                }

                try {
                    details.writeToFile(getNewManifest().getDirectory() + getNewManifest().getDetails());
                } catch (IOException e) {
                    throw new ComponentCreationException("Unable to create fabric version: failed to write version details: versionId=" + fabricProfile.getInheritsFrom(), e);
                }

                LOGGER.debug("Created fabric version: id={}", getNewManifest().getId());
            }
        }
        throw new ComponentCreationException("Unable to create fabric version: failed to find mc version: versionId=" + fabricProfile.getInheritsFrom());
    }

    private void addFabricFile(LauncherVersionDetails details) throws ComponentCreationException {
        File baseDir = new File(getNewManifest().getDirectory());
        if(!baseDir.isDirectory()) {
            throw new ComponentCreationException("Unable to add fabric file: base dir is not a directory");
        }
        try {
            FabricFileDownloader.downloadFabricLoader(baseDir, fabricVersion.getLoader());
        } catch (FileDownloadException e) {
            throw new ComponentCreationException("Unable to add fabric file: failed to download fabric loader", e);
        }
        details.setMainFile(LauncherApplication.config.FABRIC_DEFAULT_CLIENT_FILENAME);
        LOGGER.debug("Added fabric file: mainFile={}", details.getMainFile());
    }

    private void addFabricLibraries(LauncherVersionDetails details) throws ComponentCreationException {
        if(fabricProfile.getLibraries() == null) {
            throw new ComponentCreationException("Unable to add fabric libraries: no libraries");
        }
        File baseDir = new File(librariesDir);
        if(!baseDir.isDirectory() && !baseDir.mkdirs()) {
            throw new ComponentCreationException("Unable to add fabric libraries: failed to create libraries directory: path=" + librariesDir);
        }
        List<FabricLibrary> clientLibs = new ArrayList<>(fabricProfile.getLibraries()).stream().filter(f -> !FormatUtils.matches(f.getName(), ":fabric-loader:")).toList();

        List<String> libs;
        try {
            libs = FabricFileDownloader.downloadFabricLibraries(baseDir, clientLibs);
        } catch (FileDownloadException e) {
            throw new ComponentCreationException("Unable to add fabric libraries: failed to download libraries", e);
        }

        details.setLibraries(libs);
        LOGGER.debug("Added fabric libraries");
    }

    private void addFabricArguments(LauncherVersionDetails details) throws ComponentCreationException {
        details.setJvmArguments(translateArguments(fabricProfile.getLaunchArguments().getJvm(), LauncherApplication.config.FABRIC_DEFAULT_JVM_ARGUMENTS));
        details.setGameArguments(translateArguments(fabricProfile.getLaunchArguments().getGame(), LauncherApplication.config.FABRIC_DEFAULT_GAME_ARGUMENTS));
        LOGGER.debug("Added fabric arguments");
    }

    private void makeMinecraftVersion() throws ComponentCreationException {
        LauncherVersionDetails details = new LauncherVersionDetails(
                mcVersion.getId(),
                "vanilla",
                null,
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
        try {
            downloadAssets();
            addArguments(details);
            addJava(details);
            addLibraries(details);
            addFile(details);
        } catch (ComponentCreationException e) {
            attemptCleanup();
            throw new ComponentCreationException("Unable to create minecraft version", e);
        }

        try {
            details.writeToFile(getNewManifest().getDirectory() + getNewManifest().getDetails());
        } catch (IOException e) {
            attemptCleanup();
            throw new ComponentCreationException("Unable to write version details to file", e);
        }

        LOGGER.debug("Created minecraft version: id={}", getNewManifest().getId());
    }

    private void downloadAssets() throws ComponentCreationException {
        LOGGER.debug("Downloading assets...");
        String assetIndexUrl = mcVersion.getAssetIndex().getUrl();
        AssetIndex index;
        try {
            index = AssetIndex.fromJson(Sources.getFileFromUrl(assetIndexUrl));
        } catch (FileDownloadException e) {
            throw new ComponentCreationException("Unable to download assets: failed to download asset index", e);
        }
        if(index.getObjects() == null || index.getObjects().isEmpty()) {
            throw new ComponentCreationException("Unable to download assets: invalid index contents");
        }
        File baseDir = new File(LauncherApplication.config.BASE_DIR + files.getLauncherDetails().getAssetsDir());
        try {
            AssetsFileDownloader.downloadAssets(baseDir, index, assetIndexUrl, false);
        } catch (FileDownloadException e) {
            throw new ComponentCreationException("Unable to download assets: failed to download assets", e);
        }
        LOGGER.debug("Downloaded assets");
    }

    private void addJava(LauncherVersionDetails details) throws ComponentCreationException {
        String javaName = mcVersion.getJavaVersion().getComponent();

        if(javaName == null) {
            throw new ComponentCreationException("Unable to add java component: java name is null");
        }
        for(LauncherManifest j : files.getJavaComponents()) {
            if(j != null && javaName.equals(j.getName())) {
                details.setJava(j.getId());
                LOGGER.debug("Using existing java component: id={}", j.getId());
                return;
            }
        }

        JavaComponentCreator javaCreator = new JavaComponentCreator(javaName, getTypeConversion(), files.getJavaManifest());
        try {
            details.setJava(javaCreator.getId());
        } catch (ComponentCreationException e) {
            throw new ComponentCreationException("Unable to add java component: failed to create java component", e);
        }
    }

    private void addLibraries(LauncherVersionDetails details) throws ComponentCreationException {
        if(mcVersion.getLibraries() == null) {
            throw new ComponentCreationException("Unable to add libraries: libraries is null");
        }
        File baseDir = new File(librariesDir);
        if(!baseDir.isDirectory()) {
            throw new ComponentCreationException("Unable to add libraries: libraries dir is not a directory: dir=" + librariesDir);
        }

        List<String> result = null;
        try {
            result = MinecraftVersionFileDownloader.downloadVersionLibraries(mcVersion.getLibraries(), baseDir, List.of());
        } catch (FileDownloadException e) {
            throw new ComponentCreationException("Unable to add libraries: failed to download libraries", e);
        }

        details.setLibraries(result);
        LOGGER.debug("Added libraries: {}", result);
    }

    private void addFile(LauncherVersionDetails details) throws ComponentCreationException {
        File baseDir = new File(getNewManifest().getDirectory());
        if(!baseDir.isDirectory()) {
            throw new ComponentCreationException("Unable to add file: base dir is not a directory: dir=" + getNewManifest().getDirectory());
        }
        try {
            MinecraftVersionFileDownloader.downloadVersionDownload(mcVersion.getDownloads().getClient(), baseDir);
        } catch (FileDownloadException e){
            throw new ComponentCreationException("Unable to add file: Failed to download client: url=" + mcVersion.getDownloads().getClient().getUrl(), e);
        }
        String[] urlParts = mcVersion.getDownloads().getClient().getUrl().split("/");
        details.setMainFile(urlParts[urlParts.length - 1]);
        LOGGER.debug("Added file: mainFile={}", details.getMainFile());
    }

    private boolean addArguments(LauncherVersionDetails details) throws ComponentCreationException {
        details.setGameArguments(translateArguments(mcVersion.getLaunchArguments().getGame(), LauncherApplication.config.MINECRAFT_DEFAULT_GAME_ARGUMENTS));
        details.setJvmArguments(translateArguments(mcVersion.getLaunchArguments().getJvm(), LauncherApplication.config.MINECRAFT_DEFAULT_JVM_ARGUMENTS));
        LOGGER.debug("Added arguments");
        return true;
    }

    private List<LauncherLaunchArgument> translateArguments(List<MinecraftLaunchArgument> args, List<LauncherLaunchArgument> defaultArgs) throws ComponentCreationException {
        if(args == null) {
            throw new ComponentCreationException("Mc arguments is null");
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
