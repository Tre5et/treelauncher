package net.treset.minecraftlauncher.creation;

import net.treset.mc_version_loader.launcher.*;
import net.treset.minecraftlauncher.LauncherApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InstanceCreator extends GenericComponentCreator {
    private static final Logger LOGGER = LogManager.getLogger(InstanceCreator.class);

    private final List<String> ignoredFiles;
    private final List<LauncherLaunchArgument> jvmArguments;
    private final List<LauncherFeature> features;
    private final ModsCreator modsCreator;
    private final OptionsCreator optionsCreator;
    private final ResourcepackCreator resourcepackCreator;
    private final SavesCreator savesCreator;
    private final VersionCreator versionCreator;

    public InstanceCreator(String name, Map<String, LauncherManifestType> typeConversion, LauncherManifest componentsManifest, List<String> ignoredFiles, List<LauncherLaunchArgument> jvmArguments, List<LauncherFeature> features, ModsCreator modsCreator, OptionsCreator optionsCreator, ResourcepackCreator resourcepackCreator, SavesCreator savesCreator, VersionCreator versionCreator) {
        super(LauncherManifestType.INSTANCE_COMPONENT, null, null, name, typeConversion, LauncherApplication.config.INSTANCE_DEFAULT_INCLUDED_FILES, LauncherApplication.config.INSTANCE_DEFAULT_DETAILS, componentsManifest);
        this.ignoredFiles = ignoredFiles;
        this.jvmArguments = jvmArguments;
        this.features = features;
        this.modsCreator = modsCreator;
        this.optionsCreator = optionsCreator;
        this.resourcepackCreator = resourcepackCreator;
        this.savesCreator = savesCreator;
        this.versionCreator = versionCreator;
    }


    @Override
    public String createComponent() {
        String result = super.createComponent();

        if(result == null || getNewManifest() == null) {
            LOGGER.warn("Failed to create instance component: invalid data");
            attemptCleanup();
            return null;
        }

        ArrayList<LauncherFeature> features = new ArrayList<>(this.features);
        features.addAll(LauncherApplication.config.INSTANCE_DEFAULT_FEATURES);

        ArrayList<String> ignoredFiles = new ArrayList<>(this.ignoredFiles);
        ignoredFiles.addAll(LauncherApplication.config.INSTANCE_DEFAULT_IGNORED_FILES);

        ArrayList<LauncherLaunchArgument> jvmArguments = new ArrayList<>(this.jvmArguments);
        jvmArguments.addAll(LauncherApplication.config.INSTANCE_DEFAULT_JVM_ARGUMENTS);

        LauncherInstanceDetails details = new LauncherInstanceDetails(
                features,
                ignoredFiles,
                jvmArguments,
                null, null, null, null, null);

        if(modsCreator != null) {
            details.setModsComponent(modsCreator.getId());
            if (details.getModsComponent() == null) {
                LOGGER.warn("Failed to create instance component: failed to create mods component");
                attemptCleanup();
                return null;
            }
        }

        details.setOptionsComponent(optionsCreator.getId());
        if(details.getOptionsComponent() == null) {
            LOGGER.warn("Failed to create instance component: failed to create options component");
            attemptCleanup();
            return null;
        }

        details.setResourcepacksComponent(resourcepackCreator.getId());
        if(details.getResourcepacksComponent() == null) {
            LOGGER.warn("Failed to create instance component: failed to create resourcepacks component");
            attemptCleanup();
            return null;
        }

        details.setSavesComponent(savesCreator.getId());
        if(details.getSavesComponent() == null) {
            LOGGER.warn("Failed to create instance component: failed to create saves component");
            attemptCleanup();
            return null;
        }

        details.setVersionComponent(versionCreator.getId());
        if(details.getVersionComponent() == null) {
            LOGGER.warn("Failed to create instance component: failed to create version component");
            attemptCleanup();
            return null;
        }

        if(!details.writeToFile(getNewManifest().getDirectory() + getNewManifest().getDetails())) {
            LOGGER.warn("Failed to create instance component: failed to write details to file");
            attemptCleanup();
            return null;
        }

        LOGGER.debug("Created instance component: id={}", getNewManifest().getId());
        return result;
    }

    @Override
    public String inheritComponent() {
        LOGGER.warn("Unable to inherit instance component: unsupported");
        attemptCleanup();
        return null;
    }

    @Override
    public String useComponent() {
        LOGGER.warn("Unable to use instance component: unsupported");
        attemptCleanup();
        return null;
    }

    @Override
    protected boolean attemptCleanup() {
        boolean success = super.attemptCleanup();
        success &= optionsCreator.attemptCleanup();
        success &= modsCreator == null || modsCreator.attemptCleanup();
        success &= savesCreator.attemptCleanup();
        success &= versionCreator.attemptCleanup();
        success &= resourcepackCreator.attemptCleanup();
        LOGGER.debug("Attempted cleanup of instance component: success={}", success);
        return success;
    }
}
