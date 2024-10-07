package net.treset.treelauncher.backend.data

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.data.manifest.*
import net.treset.treelauncher.backend.util.exception.FileLoadException
import net.treset.treelauncher.backend.util.file.LauncherFile
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*

open class LauncherFiles {
    private var _mainManifest: MainManifest? = null
    val mainManifest: MainManifest
        get() = _mainManifest!!
    protected var _modsManifest: ParentManifest? = null
    val modsManifest: ParentManifest
        get() = _modsManifest!!
    protected var _modsComponents: Array<ModsComponent>? = null
    val modsComponents: Array<ModsComponent>
        get() = _modsComponents!!
    protected var _savesManifest: ParentManifest? = null
    val savesManifest: ParentManifest
        get() = _savesManifest!!
    protected var _savesComponents: Array<SavesComponent>? = null
    val savesComponents: Array<SavesComponent>
        get() = _savesComponents!!
    private var _instanceManifest: ParentManifest? = null
    val instanceManifest: ParentManifest
        get() = _instanceManifest!!
    private var _instanceComponents: Array<InstanceComponent>? = null
    val instanceComponents: Array<InstanceComponent>
        get() = _instanceComponents!!
    private var _javaManifest: ParentManifest? = null
    val javaManifest: ParentManifest
        get() = _javaManifest!!
    private var _javaComponents: Array<JavaComponent>? = null
    val javaComponents: Array<JavaComponent>
        get() = _javaComponents!!
    private var _optionsManifest: ParentManifest? = null
    val optionsManifest: ParentManifest
        get() = _optionsManifest!!
    private var _optionsComponents: Array<OptionsComponent>? = null
    val optionsComponents: Array<OptionsComponent>
        get() = _optionsComponents!!
    private var _resourcepackManifest: ParentManifest? = null
    val resourcepackManifest: ParentManifest
        get() = _resourcepackManifest!!
    private var _resourcepackComponents: Array<ResourcepackComponent>? = null
    val resourcepackComponents: Array<ResourcepackComponent>
        get() = _resourcepackComponents!!
    private var _versionManifest: ParentManifest? = null
    val versionManifest: ParentManifest
        get() = _versionManifest!!
    private var _versionComponents: Array<VersionComponent>? = null
    val versionComponents: Array<VersionComponent>
        get() = _versionComponents!!

    private var _gameDataDir: LauncherFile? = null
    val gameDataDir: LauncherFile
        get() = _gameDataDir!!

    init {
        reloadMain()
        LOGGER.debug { "Loaded Main manifest" }
    }

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
        _mainManifest = MainManifest.readFile(file)
        _gameDataDir = LauncherFile.ofData(_mainManifest?.gameDataDir ?: throw FileLoadException("Unable to load main manifest: invalid configuration"))
        LOGGER.debug { "Finished reloading main manifest" }
    }


    @Throws(IOException::class)
    open fun reloadMods() {
        LOGGER.debug { "Reloading mods..." }
        val file = LauncherFile.ofData(_mainManifest?.modsDir ?: throw IOException("Unable to load mods manifest: invalid configuration"))
        _modsManifest = ParentManifest.readFile(file, LauncherManifestType.MODS)

        _modsComponents = reloadComponents(
            _modsManifest ?: throw FileNotFoundException("Unable to load mods components: parent manifest not loaded"),
            ModsComponent::readFile,
            _modsComponents ?: emptyArray()
        )
        LOGGER.debug { "Finished reloading mods" }
    }

    @Throws(IOException::class)
    open fun reloadSaves() {
        LOGGER.debug { "Reloading saves..." }
        val file = LauncherFile.ofData(_mainManifest?.savesDir ?: throw FileLoadException("Unable to load saves manifest: invalid configuration"))
        _savesManifest = ParentManifest.readFile(file, LauncherManifestType.SAVES)

        _savesComponents = reloadComponents(
            _savesManifest ?: throw FileNotFoundException("Unable to load saves components: parent manifest not loaded"),
            SavesComponent::readFile,
            _savesComponents?: emptyArray()
        )
        LOGGER.debug { "Finished reloading saves" }
    }

    @Throws(IOException::class)
    fun reloadInstances() {
        LOGGER.debug { "Reloading instances..." }
        val file = LauncherFile.ofData(_mainManifest?.instancesDir ?: throw FileLoadException("Unable to load instance manifest: invalid configuration"))
        _instanceManifest = ParentManifest.readFile(file, LauncherManifestType.INSTANCES)

        _instanceComponents = reloadComponents(
            _instanceManifest ?: throw FileNotFoundException("Unable to load instance components: parent manifest not loaded"),
            InstanceComponent::readFile,
            _instanceComponents?: emptyArray()
        )
        LOGGER.debug { "Finished reloading instances" }
    }

    @Throws(IOException::class)
    fun reloadJavas() {
        LOGGER.debug { "Reloading javas..." }
        val file = LauncherFile.ofData(_mainManifest?.javasDir ?: throw FileLoadException("Unable to load java manifest: invalid configuration"))
        _javaManifest = ParentManifest.readFile(file, LauncherManifestType.JAVAS)

        _javaComponents = reloadComponents(
            _javaManifest ?: throw FileNotFoundException("Unable to load java components: parent manifest not loaded"),
            JavaComponent::readFile,
            _javaComponents?: emptyArray()
        )
        LOGGER.debug { "Finished reloading javas" }
    }

    @Throws(IOException::class)
    fun reloadOptions() {
        LOGGER.debug { "Reloading options..." }
        val file = LauncherFile.ofData(_mainManifest?.optionsDir ?: throw FileLoadException("Unable to load options manifest: invalid configuration"))
        _optionsManifest = ParentManifest.readFile(file, LauncherManifestType.OPTIONS)

        _optionsComponents = reloadComponents(
            _optionsManifest ?: throw FileNotFoundException("Unable to load options components: parent manifest not loaded"),
            OptionsComponent::readFile,
            _optionsComponents?: emptyArray()
        )
        LOGGER.debug { "Finished reloading options" }
    }

    @Throws(IOException::class)
    fun reloadResourcepacks() {
        LOGGER.debug { "Reloading resourcepacks..." }
        val file = LauncherFile.ofData(_mainManifest?.resourcepacksDir ?: throw FileLoadException("Unable to load resourcepack manifest: invalid configuration"))
        _resourcepackManifest = ParentManifest.readFile(file, LauncherManifestType.RESOURCEPACKS)

        _resourcepackComponents = reloadComponents(
            _resourcepackManifest ?: throw FileNotFoundException("Unable to load resourcepack components: parent manifest not loaded"),
            ResourcepackComponent::readFile,
            _resourcepackComponents?: emptyArray()
        )
        LOGGER.debug { "Finished reloading resourcepacks" }
    }
    @Throws(IOException::class)
    fun reloadVersions() {
        LOGGER.debug { "Reloading versions..." }
        val file = LauncherFile.ofData(_mainManifest?.versionDir ?: throw FileLoadException("Unable to load version manifest: invalid configuration"))
        _versionManifest = ParentManifest.readFile(file, LauncherManifestType.VERSIONS)

        _versionComponents = reloadComponents(
            _versionManifest ?: throw FileNotFoundException("Unable to load version components: parent manifest not loaded"),
            VersionComponent::readFile,
            _versionComponents?: emptyArray()
        )
        LOGGER.debug { "Finished reloading versions" }
    }

    @Throws(IOException::class)
    inline fun <reified T: Component> reloadComponents(
        parentManifest: ParentManifest,
        load: (LauncherFile) -> T,
        currentComponents: Array<T>
    ): Array<T> {
        val out = mutableListOf<T>()

        for(id in parentManifest.components) {
            val file = LauncherFile.of(parentManifest.directory, "${parentManifest.prefix}_$id", appConfig().manifestFileName)
            val component = load(file)
            currentComponents.filter { it.id == component.id }.firstOrNull() ?.let {
                component.copyTo(it)
                out.add(it)
            } ?: out.add(component)
        }

        return out.toTypedArray()
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
            usedVersions.add(instance.versionComponent)
            LOGGER.debug { "Used version: ${instance.versionComponent}" }
        }

        var newFound: Boolean
        do {
            LOGGER.debug { "Checking for dependencies..." }
            newFound = false
            for (version in versionComponents) {
                if (usedVersions.contains(version.id)) {
                    version.depends?.let {
                        if (!usedVersions.contains(it)) {
                            usedVersions.add(it)
                            newFound = true
                            LOGGER.debug { "Used version: ${version.id}" }
                        }
                    }
                }
            }
        } while(newFound)
        LOGGER.debug { "Finished checking for dependencies" }
        LOGGER.debug { "Finished checking for used versions" }

        LOGGER.debug { "Deleting unused versions..."}
        for (version in versionComponents) {
            if (!usedVersions.contains(version.id)) {
                LOGGER.debug { "Deleting unused version: ${version.id}" }
                versionManifest.components.remove(version.id)
                try {
                    LauncherFile.of(version.directory).remove()
                } catch(e: IOException) {
                    throw FileLoadException("Unable to remove unused version: file error", e)
                }
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
            if(!usedVersions.contains(version.id)) {
                unusedLibraries.addAll(version.libraries)
                LOGGER.debug { "Unused libraries: ${version.libraries}" }
            }
        }
        LOGGER.debug { "Finished collecting libraries in unused versions" }

        LOGGER.debug { "Checking unused libraries against used versions..." }
        for(version in versionComponents) {
            if(usedVersions.contains(version.id)) {
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

@Throws(IOException::class)
fun Array<out Component>.getId(id: String): Component {
    return this.firstOrNull { it.id == id } ?: throw FileNotFoundException("Unable to find component: id=$id")
}
