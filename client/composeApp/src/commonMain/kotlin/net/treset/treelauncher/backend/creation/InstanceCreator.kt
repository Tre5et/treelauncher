package net.treset.treelauncher.backend.creation

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mc_version_loader.launcher.*
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.util.CreationStatus
import net.treset.treelauncher.backend.util.exception.ComponentCreationException
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.backend.util.string.PatternString
import java.io.IOException

class InstanceCreator(
    name: String?,
    typeConversion: Map<String, LauncherManifestType>?,
    componentsManifest: LauncherManifest?,
    ignoredFiles: List<PatternString>,
    jvmArguments: List<LauncherLaunchArgument>,
    features: List<LauncherFeature>,
    modsCreator: ModsCreator?,
    optionsCreator: OptionsCreator,
    resourcepackCreator: ResourcepackCreator,
    savesCreator: SavesCreator,
    versionCreator: VersionCreator
) : GenericComponentCreator(
    LauncherManifestType.INSTANCE_COMPONENT,
    null,
    null,
    name,
    typeConversion,
    appConfig().INSTANCE_DEFAULT_INCLUDED_FILES,
    appConfig().INSTANCE_DEFAULT_DETAILS,
    componentsManifest
) {
    private val ignoredFiles: List<PatternString>
    private val jvmArguments: List<LauncherLaunchArgument>
    private val features: List<LauncherFeature>
    private val modsCreator: ModsCreator?
    private val optionsCreator: OptionsCreator
    private val resourcepackCreator: ResourcepackCreator
    private val savesCreator: SavesCreator
    private val versionCreator: VersionCreator

    init {
        this.ignoredFiles = ignoredFiles
        this.jvmArguments = jvmArguments
        this.features = features
        this.modsCreator = modsCreator
        this.optionsCreator = optionsCreator
        this.resourcepackCreator = resourcepackCreator
        this.savesCreator = savesCreator
        this.versionCreator = versionCreator
        defaultStatus = CreationStatus(CreationStatus.DownloadStep.STARTING, null)
    }

    @Throws(ComponentCreationException::class)
    override fun createComponent(): String {
        val result = super.createComponent()
        newManifest?.let { newManifest ->
            val features: ArrayList<LauncherFeature> = ArrayList(features)
            features.addAll(appConfig().INSTANCE_DEFAULT_FEATURES)
            val ignoredFiles: ArrayList<PatternString> = ArrayList(ignoredFiles)
            ignoredFiles.addAll(appConfig().INSTANCE_DEFAULT_IGNORED_FILES)
            val jvmArguments: ArrayList<LauncherLaunchArgument> = ArrayList(
                jvmArguments
            )
            jvmArguments.addAll(appConfig().INSTANCE_DEFAULT_JVM_ARGUMENTS)
            val details = LauncherInstanceDetails(
                features,
                ignoredFiles.stream().map(PatternString::get).toList(),
                jvmArguments,
                null, null, null, null, null
            )
            try {
                if (modsCreator != null) {
                    details.modsComponent = modsCreator.id
                }
                details.optionsComponent = optionsCreator.id
                details.resourcepacksComponent = resourcepackCreator.id
                details.savesComponent = savesCreator.id
                details.versionComponent = versionCreator.id
            } catch (e: ComponentCreationException) {
                attemptCleanup()
                throw ComponentCreationException("Failed to create instance: Error creating components", e)
            }
            setStatus(CreationStatus(CreationStatus.DownloadStep.FINISHING, null))
            try {
                LauncherFile.of(newManifest.directory, newManifest.details).write(details)
            } catch (e: IOException) {
                attemptCleanup()
                throw ComponentCreationException(
                    "Failed to create instance component: failed to write details to file",
                    e
                )
            }
            LOGGER.debug("Created instance component: id={}", newManifest.id)
        }?: {
            attemptCleanup()
            throw ComponentCreationException("Failed to create instance component: invalid data")
        }
        return result
    }

    @Throws(ComponentCreationException::class)
    override fun inheritComponent(): String {
        attemptCleanup()
        throw ComponentCreationException("Unable to inherit instance component: unsupported")
    }

    @Throws(ComponentCreationException::class)
    override fun useComponent(): String {
        attemptCleanup()
        throw ComponentCreationException("Unable to use instance component: unsupported")
    }

    override fun attemptCleanup(): Boolean {
        var success = super.attemptCleanup()
        success = success and optionsCreator.attemptCleanup()
        success = success and (modsCreator == null || modsCreator.attemptCleanup())
        success = success and savesCreator.attemptCleanup()
        success = success and versionCreator.attemptCleanup()
        success = success and resourcepackCreator.attemptCleanup()
        LOGGER.debug("Attempted cleanup of instance component: success={}", success)
        return success
    }

    override var statusCallback: (CreationStatus) -> Unit
        get() = super.statusCallback
        set(statusCallback) {
            super.statusCallback = statusCallback
            optionsCreator.statusCallback = statusCallback
            modsCreator?.statusCallback = statusCallback
            savesCreator.statusCallback = statusCallback
            versionCreator.statusCallback = statusCallback
            resourcepackCreator.statusCallback = statusCallback
        }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}
