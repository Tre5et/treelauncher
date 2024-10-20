package dev.treset.treelauncher.backend.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import dev.treset.treelauncher.backend.config.appConfig
import dev.treset.treelauncher.backend.data.manifest.*
import dev.treset.treelauncher.backend.util.exception.FileLoadException
import dev.treset.treelauncher.backend.util.file.LauncherFile
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*
import kotlin.reflect.KMutableProperty0

open class LauncherFiles {
    private var _mainManifest: MainManifest? = null
    val mainManifest: MainManifest
        get() = _mainManifest!!
    protected var _modsManifest: ParentManifest? = null
    val modsManifest: ParentManifest
        get() = _modsManifest!!
    val modsComponents = mutableStateListOf<ModsComponent>()
    protected var _savesManifest: ParentManifest? = null
    val savesManifest: ParentManifest
        get() = _savesManifest!!
    val savesComponents = mutableStateListOf<SavesComponent>()
    private var _instanceManifest: ParentManifest? = null
    val instanceManifest: ParentManifest
        get() = _instanceManifest!!
    val instanceComponents = mutableStateListOf<InstanceComponent>()
    private var _javaManifest: ParentManifest? = null
    val javaManifest: ParentManifest
        get() = _javaManifest!!
    val javaComponents = mutableStateListOf<JavaComponent>()
    private var _optionsManifest: ParentManifest? = null
    val optionsManifest: ParentManifest
        get() = _optionsManifest!!
    val optionsComponents = mutableStateListOf<OptionsComponent>()
    private var _resourcepackManifest: ParentManifest? = null
    val resourcepackManifest: ParentManifest
        get() = _resourcepackManifest!!
    val resourcepackComponents = mutableStateListOf<ResourcepackComponent>()
    private var _versionManifest: ParentManifest? = null
    val versionManifest: ParentManifest
        get() = _versionManifest!!
    val versionComponents = mutableStateListOf<VersionComponent>()

    val assetsDir: LauncherFile
        get() = LauncherFile.ofData(mainManifest.assetsDir.value)
    val librariesDir: LauncherFile
        get() = LauncherFile.ofData(mainManifest.librariesDir.value)
    val gameDataDir: LauncherFile
        get() = LauncherFile.ofData(mainManifest.gameDataDir.value)

    @Throws(IOException::class)
    fun reload() {
        LOGGER.debug { "Reloading all components..." }
        reloadMain()
        reloadMods()
        reloadSaves()
        reloadInstances()
        reloadJavas()
        reloadOptions()
        reloadResourcepacks()
        reloadVersions()
        LOGGER.debug { "Finished reloading all components" }
    }

    @Throws(IOException::class)
    fun reloadMain() {
        LOGGER.debug { "Reloading main manifest..." }
        val file = LauncherFile.ofData(appConfig().manifestFileName)
        val manifest = Manifest.readFile<MainManifest>(file)
        _mainManifest?.let {
            manifest.copyTo(it)
        } ?: run {
            _mainManifest = manifest
        }
        LOGGER.debug { "Finished reloading main manifest" }
    }


    @Throws(IOException::class)
    open fun reloadMods() {
        LOGGER.debug { "Reloading mods..." }
        reloadType(
            _mainManifest?.modsDir,
            LauncherManifestType.MODS,
            ::_modsManifest,
            modsComponents
        )
        LOGGER.debug { "Finished reloading mods" }
    }

    @Throws(IOException::class)
    open fun reloadSaves() {
        LOGGER.debug { "Reloading saves..." }
        reloadType(
            _mainManifest?.savesDir,
            LauncherManifestType.SAVES,
            ::_savesManifest,
            savesComponents
        )
        LOGGER.debug { "Finished reloading saves" }
    }

    @Throws(IOException::class)
    fun reloadInstances() {
        LOGGER.debug { "Reloading instances..." }
        reloadType(
            _mainManifest?.instancesDir,
            LauncherManifestType.INSTANCES,
            ::_instanceManifest,
            instanceComponents
        )
        LOGGER.debug { "Finished reloading instances" }
    }

    @Throws(IOException::class)
    fun reloadJavas() {
        LOGGER.debug { "Reloading javas..." }
        reloadType(
            _mainManifest?.javasDir,
            LauncherManifestType.JAVAS,
            ::_javaManifest,
            javaComponents
        )
        LOGGER.debug { "Finished reloading javas" }
    }

    @Throws(IOException::class)
    fun reloadOptions() {
        LOGGER.debug { "Reloading options..." }
        reloadType(
            _mainManifest?.optionsDir,
            LauncherManifestType.OPTIONS,
            ::_optionsManifest,
            optionsComponents
        )
        LOGGER.debug { "Finished reloading options" }
    }

    @Throws(IOException::class)
    fun reloadResourcepacks() {
        LOGGER.debug { "Reloading resourcepacks..." }
        reloadType(
            _mainManifest?.resourcepacksDir,
            LauncherManifestType.RESOURCEPACKS,
            ::_resourcepackManifest,
            resourcepackComponents
        )
        LOGGER.debug { "Finished reloading resourcepacks" }
    }
    @Throws(IOException::class)
    fun reloadVersions() {
        LOGGER.debug { "Reloading versions..." }
        reloadType(
            _mainManifest?.versionDir,
            LauncherManifestType.VERSIONS,
            ::_versionManifest,
            versionComponents
        )
        LOGGER.debug { "Finished reloading versions" }
    }

    @Throws(IOException::class)
    inline fun <reified T: Component> reloadType(
        dir: MutableState<String>?,
        expectedType: LauncherManifestType,
        manifest: KMutableProperty0<ParentManifest?>,
        components: SnapshotStateList<T>
    ) {
        reloadManifest(dir, expectedType, manifest)
        reloadComponents(
            manifest.get() ?: throw FileNotFoundException("Unable to load components: parent manifest not loaded"),
            components
        )
    }

    @Throws(IOException::class)
    fun reloadManifest(
        dir: MutableState<String>?,
        expectedType: LauncherManifestType,
        manifest: KMutableProperty0<ParentManifest?>
    ) {
        val file = LauncherFile.ofData(dir?.value ?: throw FileLoadException("Unable to load parent manifest: invalid configuration"), appConfig().manifestFileName)
        val newManifest: ParentManifest = Manifest.readFile(file, expectedType)
        manifest.get()?.let {
            newManifest.copyTo(it)
        } ?: run {
            manifest.set(newManifest)
        }
    }

    @Throws(IOException::class)
    inline fun <reified T: Component> reloadComponents(
        parentManifest: ParentManifest,
        currentComponents: SnapshotStateList<T>
    ) {
        val toRemove = currentComponents.filter { it.id.value !in parentManifest.components }
        currentComponents.removeAll(toRemove)

        for(id in parentManifest.components) {
            val file = LauncherFile.of(parentManifest.directory, "${parentManifest.prefix.value}_$id", appConfig().manifestFileName)
            val component = Manifest.readFile<T>(file)
            currentComponents.find { it.id.value == component.id.value }?.let {
                component.copyTo(it)
            } ?: currentComponents.add(component)
        }
    }

    @Throws(IOException::class)
    fun cleanupVersions(
        includeLibraries: Boolean
    ) {
        reload()

        LOGGER.debug { "Cleaning up versions..." }
        LOGGER.debug { "Checking for used versions..." }
        val usedVersions: MutableList<String> = mutableListOf()
        for (instance in instanceComponents) {
            usedVersions.add(instance.versionComponent.value)
            LOGGER.debug { "Used version: ${instance.versionComponent.value}" }
        }

        var newFound: Boolean
        do {
            LOGGER.debug { "Checking for dependencies..." }
            newFound = false
            for (version in versionComponents) {
                if (usedVersions.contains(version.id.value)) {
                    version.depends.value?.let {
                        if (!usedVersions.contains(it)) {
                            usedVersions.add(it)
                            newFound = true
                            LOGGER.debug { "Used version: ${version.id.value}" }
                        }
                    }
                }
            }
        } while(newFound)
        LOGGER.debug { "Finished checking for dependencies" }
        LOGGER.debug { "Finished checking for used versions" }

        LOGGER.debug { "Deleting unused versions..."}
        for (version in versionComponents) {
            if (!usedVersions.contains(version.id.value)) {
                LOGGER.debug { "Deleting unused version: ${version.id}" }
                version.delete(versionManifest)
            }
        }
        LOGGER.debug { "Finished deleting unused versions" }

        LOGGER.debug { "Saving instance manifest..." }
        try {
            LauncherFile.of(
                versionManifest.directory,
                appConfig().manifestFileName
            ).write(versionManifest)
        } catch(e: IOException) {
            throw FileLoadException("Unable to save instance manifest: file error", e)
        }
        LOGGER.debug { "Saved instance manifest" }

        if(includeLibraries) {
            cleanupLibraries(usedVersions)
        }

        LOGGER.debug { "Finished cleaning up versions" }
    }

    private fun cleanupLibraries(
        usedVersions: List<String>
    ) {
        LOGGER.debug { "Cleaning up libraries..." }

        LOGGER.debug { "Collecting libraries in unused versions..." }
        val unusedLibraries: MutableList<String> = mutableListOf()
        for(version in versionComponents) {
            if(!usedVersions.contains(version.id.value)) {
                unusedLibraries.addAll(version.libraries)
                LOGGER.debug { "Unused libraries: ${version.libraries}" }
            }
        }
        for(version in versionComponents) {
            if(usedVersions.contains(version.id.value)) {
                unusedLibraries.removeAll(version.libraries)
                LOGGER.debug { "Used libraries: ${version.libraries}" }
            }
        }
        LOGGER.debug { "Finished collecting libraries in unused versions" }

        LOGGER.debug { "Checking unused libraries against used versions..." }
        for(version in versionComponents) {
            if(usedVersions.contains(version.id.value)) {
                val libsToRemove = unusedLibraries.filter { version.libraries.contains(it) }
                if(libsToRemove.isNotEmpty()) {
                    unusedLibraries.removeAll(libsToRemove)
                    LOGGER.debug { "Removing used libraries from unused list: $libsToRemove" }
                }
            }
        }
        LOGGER.debug { "Finished checking unused libraries against used versions" }

        LOGGER.debug { "Deleting unused libraries..." }
        for(library in unusedLibraries) {
            val libFile = LauncherFile.of(versionManifest.directory, library)
            if(libFile.exists()) {
                try {
                    libFile.remove()
                } catch(e: IOException) {
                    LOGGER.warn(e) { "Unable to remove unused library, ignoring" }
                }
            }
        }
        LOGGER.debug { "Finished deleting unused libraries" }
        LOGGER.debug { "Finished cleaning up libraries" }
    }

    companion object {
        private val LOGGER = KotlinLogging.logger{}
    }
}
