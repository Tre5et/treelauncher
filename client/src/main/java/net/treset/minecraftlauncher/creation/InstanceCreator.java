package net.treset.minecraftlauncher.creation;

import net.treset.mc_version_loader.launcher.*;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.util.CreationStatus;
import net.treset.minecraftlauncher.util.exception.ComponentCreationException;
import net.treset.minecraftlauncher.util.string.PatternString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class InstanceCreator extends GenericComponentCreator {
    private static final Logger LOGGER = LogManager.getLogger(InstanceCreator.class);

    private final List<PatternString> ignoredFiles;
    private final List<LauncherLaunchArgument> jvmArguments;
    private final List<LauncherFeature> features;
    private final ModsCreator modsCreator;
    private final OptionsCreator optionsCreator;
    private final ResourcepackCreator resourcepackCreator;
    private final SavesCreator savesCreator;
    private final VersionCreator versionCreator;

    public InstanceCreator(String name, Map<String, LauncherManifestType> typeConversion, LauncherManifest componentsManifest, List<PatternString> ignoredFiles, List<LauncherLaunchArgument> jvmArguments, List<LauncherFeature> features, ModsCreator modsCreator, OptionsCreator optionsCreator, ResourcepackCreator resourcepackCreator, SavesCreator savesCreator, VersionCreator versionCreator) {
        super(LauncherManifestType.INSTANCE_COMPONENT, null, null, name, typeConversion, LauncherApplication.config.INSTANCE_DEFAULT_INCLUDED_FILES, LauncherApplication.config.INSTANCE_DEFAULT_DETAILS, componentsManifest);
        this.ignoredFiles = ignoredFiles;
        this.jvmArguments = jvmArguments;
        this.features = features;
        this.modsCreator = modsCreator;
        this.optionsCreator = optionsCreator;
        this.resourcepackCreator = resourcepackCreator;
        this.savesCreator = savesCreator;
        this.versionCreator = versionCreator;
        setDefaultStatus(new CreationStatus(CreationStatus.DownloadStep.STARTING, null));
    }


    @Override
    public String createComponent() throws ComponentCreationException {
        String result = super.createComponent();

        if(result == null || getNewManifest() == null) {
            attemptCleanup();
            throw new ComponentCreationException("Failed to create instance component: invalid data");
        }

        ArrayList<LauncherFeature> features = new ArrayList<>(this.features);
        features.addAll(LauncherApplication.config.INSTANCE_DEFAULT_FEATURES);

        ArrayList<PatternString> ignoredFiles = new ArrayList<>(this.ignoredFiles);
        ignoredFiles.addAll(LauncherApplication.config.INSTANCE_DEFAULT_IGNORED_FILES);

        ArrayList<LauncherLaunchArgument> jvmArguments = new ArrayList<>(this.jvmArguments);
        jvmArguments.addAll(LauncherApplication.config.INSTANCE_DEFAULT_JVM_ARGUMENTS);

        LauncherInstanceDetails details = new LauncherInstanceDetails(
                features,
                ignoredFiles.stream().map(PatternString::get).toList(),
                jvmArguments,
                null, null, null, null, null);

        try {
            if (modsCreator != null) {
                details.setModsComponent(modsCreator.getId());
            }
            details.setOptionsComponent(optionsCreator.getId());
            details.setResourcepacksComponent(resourcepackCreator.getId());
            details.setSavesComponent(savesCreator.getId());
            details.setVersionComponent(versionCreator.getId());
        } catch (ComponentCreationException e) {
            attemptCleanup();
            throw new ComponentCreationException("Failed to create instance: Error creating components", e);
        }

        setStatus(new CreationStatus(CreationStatus.DownloadStep.FINISHING, null));

        try {
            details.writeToFile(getNewManifest().getDirectory() + getNewManifest().getDetails());
        } catch (IOException e) {
            attemptCleanup();
            throw new ComponentCreationException("Failed to create instance component: failed to write details to file", e);
        }

        LOGGER.debug("Created instance component: id={}", getNewManifest().getId());
        return result;
    }

    @Override
    public String inheritComponent() throws ComponentCreationException {
        attemptCleanup();
        throw new ComponentCreationException("Unable to inherit instance component: unsupported");
    }

    @Override
    public String useComponent() throws ComponentCreationException {
        attemptCleanup();
        throw new ComponentCreationException("Unable to use instance component: unsupported");
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

    @Override
    public void setStatusCallback(Consumer<CreationStatus> statusCallback) {
        super.setStatusCallback(statusCallback);
        optionsCreator.setStatusCallback(statusCallback);
        if(modsCreator != null) {
            modsCreator.setStatusCallback(statusCallback);
        }
        savesCreator.setStatusCallback(statusCallback);
        versionCreator.setStatusCallback(statusCallback);
        resourcepackCreator.setStatusCallback(statusCallback);
    }
}
