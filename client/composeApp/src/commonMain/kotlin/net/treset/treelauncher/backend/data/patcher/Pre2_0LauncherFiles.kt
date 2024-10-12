package net.treset.treelauncher.backend.data.patcher

import io.github.oshai.kotlinlogging.KotlinLogging
import dev.treset.mcdl.json.GenericJsonParsable
import dev.treset.mcdl.json.SerializationException
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.data.manifest.*
import net.treset.treelauncher.backend.util.exception.FileLoadException
import net.treset.treelauncher.backend.util.file.LauncherFile
import java.io.IOException
import java.util.*

open class Pre2_0LauncherFiles {
    private var _mainManifest: Pre2_0MainManifest? = null
    val mainManifest: Pre2_0MainManifest
        get() = _mainManifest!!
    private var _launcherDetails: Pre2_0LauncherDetails? = null
    val launcherDetails: Pre2_0LauncherDetails
        get() = _launcherDetails!!
    protected var _modsManifest: Pre2_0ParentManifest? = null
    val modsManifest: Pre2_0ParentManifest
        get() = _modsManifest!!
    protected var _modsComponents: Array<Pair<Pre2_0ComponentManifest, Pre2_0LauncherModsDetails>>? = null
    val modsComponents: Array<Pair<Pre2_0ComponentManifest, Pre2_0LauncherModsDetails>>
        get() = _modsComponents!!
    protected var _savesManifest: Pre2_0ParentManifest? = null
    val savesManifest: Pre2_0ParentManifest
        get() = _savesManifest!!
    protected var _savesComponents: Array<Pre2_0ComponentManifest>? = null
    val savesComponents: Array<Pre2_0ComponentManifest>
        get() = _savesComponents!!
    private var _instanceManifest: Pre2_0ParentManifest? = null
    val instanceManifest: Pre2_0ParentManifest
        get() = _instanceManifest!!
    private var _instanceComponents: Array<Pair<Pre2_0ComponentManifest, Pre2_0LauncherInstanceDetails>>? = null
    val instanceComponents: Array<Pair<Pre2_0ComponentManifest, Pre2_0LauncherInstanceDetails>>
        get() = _instanceComponents!!
    private var _javaManifest: Pre2_0ParentManifest? = null
    val javaManifest: Pre2_0ParentManifest
        get() = _javaManifest!!
    private var _javaComponents: Array<Pre2_0ComponentManifest>? = null
    val javaComponents: Array<Pre2_0ComponentManifest>
        get() = _javaComponents!!
    private var _optionsManifest: Pre2_0ParentManifest? = null
    val optionsManifest: Pre2_0ParentManifest
        get() = _optionsManifest!!
    private var _optionsComponents: Array<Pre2_0ComponentManifest>? = null
    val optionsComponents: Array<Pre2_0ComponentManifest>
        get() = _optionsComponents!!
    private var _resourcepackManifest: Pre2_0ParentManifest? = null
    val resourcepackManifest: Pre2_0ParentManifest
        get() = _resourcepackManifest!!
    private var _resourcepackComponents: Array<Pre2_0ComponentManifest>? = null
    val resourcepackComponents: Array<Pre2_0ComponentManifest>
        get() = _resourcepackComponents!!
    private var _versionManifest: Pre2_0ParentManifest? = null
    val versionManifest: Pre2_0ParentManifest
        get() = _versionManifest!!
    private var _versionComponents: Array<Pair<Pre2_0ComponentManifest, Pre2_0LauncherVersionDetails>>? = null
    val versionComponents: Array<Pair<Pre2_0ComponentManifest, Pre2_0LauncherVersionDetails>>
        get() = _versionComponents!!

    init {
        reloadMainManifest()
        reloadLauncherDetails()
        LOGGER.debug { "Loaded launcher details" }
    }

    @Throws(FileLoadException::class)
    fun reloadAll() {
        reloadMainManifest()
        reloadLauncherDetails()
        reloadModsManifest()
        reloadModsComponents()
        reloadSavesManifest()
        reloadSavesComponents()
        reloadInstanceManifest()
        reloadInstanceComponents()
        reloadJavaManifest()
        reloadJavaComponents()
        reloadOptionsManifest()
        reloadOptionsComponents()
        reloadResourcepackManifest()
        reloadResourcepackComponents()
        reloadVersionManifest()
        reloadVersionComponents()
    }

    @Throws(FileLoadException::class)
    fun reloadMainManifest() {
        val versionFile: String = try {
            LauncherFile.of(appConfig().baseDir, appConfig().manifestFileName).readString()
        } catch (e: IOException) {
            throw FileLoadException("Unable to load launcher manifest: file error", e)
        }

        _mainManifest = try {
            Pre2_0MainManifest.fromJson(versionFile)
        } catch (e: SerializationException) {
            throw FileLoadException("Unable to load launcher manifest: json error", e)
        }.also {
            if (it.type != LauncherManifestType.LAUNCHER) {
                throw FileLoadException("Unable to load launcher manifest: incorrect contents")
            }
            it.directory = appConfig().baseDir.absolutePath
            LOGGER.debug { "Loaded launcher manifest" }
        }
    }

    @Throws(FileLoadException::class)
    fun reloadLauncherDetails() {
        _mainManifest?.let {
            it.details

            val detailsFile: String = try {
                LauncherFile.of(appConfig().baseDir, it.details).readString()
            } catch (e: IOException) {
                throw FileLoadException("Unable to load launcher details: file error", e)
            }

            _launcherDetails = try {
                Pre2_0LauncherDetails.fromJson(detailsFile)
            } catch (e: SerializationException) {
                throw FileLoadException("Unable to load launcher details: json error", e)
            }
            LOGGER.debug { "Loaded launcher details" }

        }?: throw FileLoadException("Unable to load launcher details: invalid main file")
    }


    @Throws(FileLoadException::class)
    open fun reloadModsManifest() {
        _modsManifest = reloadParentManifest(
            LauncherFile.ofData(_launcherDetails?.modsDir ?: throw FileLoadException("Unable to load mods manifest: invalid configuration")),
            LauncherManifestType.MODS
        )
    }

    @Throws(FileLoadException::class)
    open fun reloadModsComponents() {
        _modsComponents = reloadComponents(
            _modsManifest?: throw FileLoadException("Unable to load mods components: invalid configuration"),
            LauncherFile.ofData(_launcherDetails?.modsDir ?: throw FileLoadException("Unable to load mods components: invalid configuration")),
            LauncherManifestType.MODS_COMPONENT,
            Pre2_0LauncherModsDetails::fromJson,
            {
                it.types = types
                it.versions = versions
                it.mods = mods
            },
            null,
            _modsComponents?: emptyArray()
        )
    }

    @Throws(FileLoadException::class)
    open fun reloadSavesManifest() {
        _savesManifest = reloadParentManifest(
            LauncherFile.ofData(_launcherDetails?.savesDir ?: throw FileLoadException("Unable to load saves manifest: invalid configuration")),
            LauncherManifestType.SAVES
        )
    }

    @Throws(FileLoadException::class)
    open fun reloadSavesComponents() {
        _savesComponents = reloadComponents(
            _savesManifest?: throw FileLoadException("Unable to load saves components: invalid configuration"),
            LauncherFile.ofData(_launcherDetails?.savesDir ?: throw FileLoadException("Unable to load saves components: invalid configuration")),
            LauncherManifestType.SAVES_COMPONENT,
            LauncherFile.ofData(
                _launcherDetails?.gamedataDir ?: throw FileLoadException("Unable to load saves components: invalid configuration"),
                "saves"
            ),
            _savesComponents?: emptyArray()
        )
    }

    @Throws(FileLoadException::class)
    fun reloadInstanceManifest() {
        _instanceManifest = reloadParentManifest(
            LauncherFile.ofData(_launcherDetails?.instancesDir ?: throw FileLoadException("Unable to load instance manifest: invalid configuration")),
            LauncherManifestType.INSTANCES
        )
    }

    @Throws(FileLoadException::class)
    fun reloadInstanceComponents() {
        _instanceComponents = reloadComponents(
            _instanceManifest?: throw FileLoadException("Unable to load instance components: invalid configuration"),
            LauncherFile.ofData(_launcherDetails?.instancesDir ?: throw FileLoadException("Unable to load instance components: invalid configuration")),
            LauncherManifestType.INSTANCE_COMPONENT,
            Pre2_0LauncherInstanceDetails::fromJson,
            {
                it.features = features
                it.ignoredFiles = ignoredFiles
                it.jvmArguments = jvmArguments
                it.lastPlayed = lastPlayed
                it.totalTime = totalTime
                it.modsComponent = modsComponent
                it.optionsComponent = optionsComponent
                it.resourcepacksComponent = resourcepacksComponent
                it.savesComponent = savesComponent
                it.versionComponent = versionComponent
            },
            null,
            _instanceComponents?: emptyArray()
        )
    }

    @Throws(FileLoadException::class)
    fun reloadJavaManifest() {
        _javaManifest =
            reloadParentManifest(
                LauncherFile.ofData(_launcherDetails?.javasDir ?: throw FileLoadException("Unable to load java manifest: invalid configuration")),
                LauncherManifestType.JAVAS
            )
    }

    @Throws(FileLoadException::class)
    fun reloadJavaComponents() {
        _javaComponents = reloadComponents(
            _javaManifest?: throw FileLoadException("Unable to load java components: invalid configuration"),
            LauncherFile.ofData(_launcherDetails?.javasDir ?: throw FileLoadException("Unable to load java components: invalid configuration")),
            LauncherManifestType.JAVA_COMPONENT,
            null,
            _javaComponents?: emptyArray()
        )
    }

    @Throws(FileLoadException::class)
    fun reloadOptionsManifest() {
        _optionsManifest = reloadParentManifest(
            LauncherFile.ofData(_launcherDetails?.optionsDir ?: throw FileLoadException("Unable to load options manifest: invalid configuration")),
            LauncherManifestType.OPTIONS
        )
    }

    @Throws(FileLoadException::class)
    fun reloadOptionsComponents() {
        _optionsComponents = reloadComponents(
            _optionsManifest?: throw FileLoadException("Unable to load options components: invalid configuration"),
            LauncherFile.ofData(_launcherDetails?.optionsDir ?: throw FileLoadException("Unable to load options components: invalid configuration")),
            LauncherManifestType.OPTIONS_COMPONENT,
            null,
            _optionsComponents?: emptyArray()
        )
    }

    @Throws(FileLoadException::class)
    fun reloadResourcepackManifest() {
        _resourcepackManifest = reloadParentManifest(
            LauncherFile.ofData(_launcherDetails?.resourcepacksDir ?: throw FileLoadException("Unable to load resourcepack manifest: invalid configuration")),
            LauncherManifestType.RESOURCEPACKS
        )
    }

    @Throws(FileLoadException::class)
    fun reloadResourcepackComponents() {
        _resourcepackComponents = reloadComponents(
            _resourcepackManifest?: throw FileLoadException("Unable to load resourcepack components: invalid configuration"),
            LauncherFile.ofData(_launcherDetails?.resourcepacksDir?: throw FileLoadException("Unable to load resourcepack components: invalid configuration")),
            LauncherManifestType.RESOURCEPACKS_COMPONENT,
            null,
            _resourcepackComponents?: emptyArray()
        )
    }
    @Throws(FileLoadException::class)
    fun reloadVersionManifest() {
        _versionManifest = reloadParentManifest(
            LauncherFile.ofData(_launcherDetails?.versionDir ?: throw FileLoadException("Unable to load version manifest: invalid configuration")),
            LauncherManifestType.VERSIONS
        )
    }

    @Throws(FileLoadException::class)
    fun reloadVersionComponents() {
        _versionComponents = reloadComponents(
            _versionManifest?: throw FileLoadException("Unable to load version components: invalid configuration"),
            LauncherFile.ofData(_launcherDetails?.versionDir ?: throw FileLoadException("Unable to load version components: invalid configuration")),
            LauncherManifestType.VERSION_COMPONENT,
            Pre2_0LauncherVersionDetails::fromJson,
            {
                it.versionNumber = versionNumber
                it.versionType = versionType
                it.loaderVersion = loaderVersion
                it.assets = assets
                it.depends = depends
                it.gameArguments = gameArguments
                it.jvmArguments = jvmArguments
                it.java = java
                it.libraries = libraries
                it.mainClass = mainClass
                it.mainFile = mainFile
                it.versionId = versionId
            },
            null,
            _versionComponents?: emptyArray()
        )
    }

    @Throws(FileLoadException::class)
    fun reloadParentManifest(path: LauncherFile, expectedType: LauncherManifestType): Pre2_0ParentManifest {
        return reloadParentManifest(path, appConfig().manifestFileName, expectedType)
    }

    @Throws(FileLoadException::class)
    fun reloadParentManifest(path: LauncherFile, filename: String, expectedType: LauncherManifestType): Pre2_0ParentManifest {
        val versionFile: String = try {
            LauncherFile.of(path, filename).readString()
        } catch (e: IOException) {
            throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} manifest: file error", e)
        }
        val out = try {
            Pre2_0ParentManifest.fromJson(versionFile, launcherDetails.typeConversion)
        } catch (e: SerializationException) {
            throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} manifest: json error", e)
        }
        if (out.type != expectedType) {
            throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} manifest: incorrect type: type=${out.type}")
        }
        out.directory = path.absolutePath
        LOGGER.debug { "Loaded " + expectedType.name.lowercase(Locale.getDefault()) + " manifest" }
        return out
    }

    @Throws(FileLoadException::class)
    fun reloadComponents(
        parentManifest: Pre2_0ParentManifest,
        parentPath: LauncherFile,
        expectedType: LauncherManifestType,
        fallbackPath: LauncherFile?,
        currentComponents: Array<Pre2_0ComponentManifest>
    ): Array<Pre2_0ComponentManifest> {
        return reloadComponents(
            parentManifest,
            parentPath,
            appConfig().manifestFileName,
            expectedType,
            fallbackPath,
            currentComponents
        )
    }

    @Throws(FileLoadException::class)
    fun reloadComponents(
        parentManifest: Pre2_0ParentManifest,
        parentPath: LauncherFile,
        filename: String,
        expectedType: LauncherManifestType,
        fallbackPath: LauncherFile?,
        currentComponents: Array<Pre2_0ComponentManifest>
    ): Array<Pre2_0ComponentManifest> {
        val out: MutableList<Pre2_0ComponentManifest> = mutableListOf()
        for (c in parentManifest.components) {
            try {
                val manifest = getComponent(
                    out,
                    LauncherFile.of(parentPath, "${parentManifest.prefix}_$c"),
                    filename,
                    expectedType,
                    c,
                    fallbackPath
                )
                currentComponents.filter { it.id == manifest.id }.firstOrNull() ?.let {
                    it.name = manifest.name
                    it.directory = manifest.directory
                    it.lastUsed = manifest.lastUsed
                    it.includedFiles = manifest.includedFiles
                    it.typeConversion = manifest.typeConversion
                    out.add(it)
                } ?: out.add(manifest)
            } catch (e: FileLoadException) {
                throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} components: component error: id=$c")
            }
        }
        LOGGER.debug { "Loaded ${expectedType.name.lowercase(Locale.getDefault())} components" }
        return out.toTypedArray()
    }

    @Throws(FileLoadException::class)
    private fun getComponent(
        list: MutableList<Pre2_0ComponentManifest>,
        path: LauncherFile,
        filename: String,
        expectedType: LauncherManifestType,
        expectedId: String,
        fallbackPath: LauncherFile?
    ): Pre2_0ComponentManifest {
        val manifestFile: String = try {
            LauncherFile.of(path, filename).readString()
        } catch (e: IOException) {
            fallbackPath?: throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} component: file error: id=$expectedId", e)
            LOGGER.debug { "Falling back to fallback path loading " + expectedType.name.lowercase(Locale.getDefault()) + " component: file error: id=" + expectedId }
            return getComponent(list, fallbackPath, filename, expectedType, expectedId, null)
        }
        val manifest = try {
            Pre2_0ComponentManifest.fromJson(manifestFile, _launcherDetails?.typeConversion?: throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} component: invalid configuration"))
        } catch (e: SerializationException) {
            throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} component: json error: id=$expectedId", e)
        }
        if (manifest.type != expectedType || manifest.id != expectedId) {
            fallbackPath?: throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} component: incorrect contents: id=$expectedId")
            LOGGER.debug { "Falling back to fallback path loading ${expectedType.name.lowercase(Locale.getDefault())} component id=$expectedId" }
            return getComponent(list, fallbackPath, filename, expectedType, expectedId, null)
        }
        manifest.directory = path.absolutePath
        return manifest
    }

    @Throws(FileLoadException::class)
    fun <T : GenericJsonParsable?> reloadComponents(
        parentManifest: Pre2_0ParentManifest,
        parentDir: LauncherFile,
        expectedType: LauncherManifestType,
        toType: (String) -> T,
        copyTo: T.(T) -> Unit,
        fallbackPath: LauncherFile?,
        currentComponents: Array<Pair<Pre2_0ComponentManifest, T>>
    ): Array<Pair<Pre2_0ComponentManifest, T>> {
        return reloadComponents(
            parentManifest,
            parentDir,
            appConfig().manifestFileName,
            expectedType,
            toType,
            copyTo,
            fallbackPath,
            currentComponents
        )
    }

    @Throws(FileLoadException::class)
    fun <T : GenericJsonParsable?> reloadComponents(
        parentManifest: Pre2_0ParentManifest,
        parentPath: LauncherFile,
        filename: String,
        expectedType: LauncherManifestType,
        toType: (String) -> T,
        copyTo: T.(T) -> Unit,
        fallbackPath: LauncherFile?,
        currentComponents: Array<Pair<Pre2_0ComponentManifest, T>>
    ): Array<Pair<Pre2_0ComponentManifest, T>> {
        val out: MutableList<Pair<Pre2_0ComponentManifest, T>> = mutableListOf()
        val exceptionQueue: MutableList<FileLoadException> = mutableListOf()
        for (c in parentManifest.components) {
            try {
                val component = getComponent(
                    LauncherFile.of(parentPath, "${parentManifest.prefix}_$c"),
                    filename,
                    expectedType,
                    toType,
                    fallbackPath,
                    c
                )
                currentComponents.filter { it.first.id == component.first.id }.firstOrNull() ?.let {
                    it.first.name = component.first.name
                    it.first.directory = component.first.directory
                    it.first.lastUsed = component.first.lastUsed
                    it.first.includedFiles = component.first.includedFiles
                    it.first.typeConversion = component.first.typeConversion

                    component.second.copyTo(it.second)
                    out.add(it)
                } ?: out.add(component)
            } catch (e: FileLoadException) {
                exceptionQueue.add(e)
            }
        }
        if (exceptionQueue.isNotEmpty()) {
            throw FileLoadException("Unable to load ${exceptionQueue.size} components: component error", exceptionQueue[0])
        }
        LOGGER.debug { "Loaded ${expectedType.name.lowercase(Locale.getDefault())} components" }
        return out.toTypedArray()
    }

    @Throws(FileLoadException::class)
    private fun <T : GenericJsonParsable?> getComponent(
        path: LauncherFile,
        filename: String,
        expectedType: LauncherManifestType,
        toType: (String) -> T,
        fallbackPath: LauncherFile?,
        expectedId: String
    ): Pair<Pre2_0ComponentManifest, T> {
        val manifestFile: String = try {
            LauncherFile.of(path, filename).readString()
        } catch (e: IOException) {
            fallbackPath?: throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} component: file error: id=$expectedId", e)
            LOGGER.debug { "Falling back to fallback path loading ${expectedType.name.lowercase(Locale.getDefault())} component: file error: id=$expectedId" }
            return getComponent(fallbackPath, filename, expectedType, toType, null, expectedId)
        }
        val manifest = try {
            Pre2_0ComponentManifest.fromJson(manifestFile, launcherDetails.typeConversion)
        } catch (e: SerializationException) {
            throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} component: json error: id=$expectedId, e")
        }
        if (manifest.type != expectedType || manifest.id != expectedId) {
            fallbackPath?: throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} component: incorrect contents: id=$expectedId")
            LOGGER.debug { "Falling back to fallback path loading ${expectedType.name.lowercase(Locale.getDefault())} component id=$expectedId" }
            return getComponent(fallbackPath, filename, expectedType, toType, null, expectedId)
        }

        manifest.directory = path.absolutePath
        val detailsFile = try {
            LauncherFile.of(path, manifest.details).readString()
        } catch (e: IOException) {
            fallbackPath?: throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} component details: file error: id=$expectedId", e)
            LOGGER.debug { "Falling back to fallback path loading ${expectedType.name.lowercase(Locale.getDefault())} component id=$expectedId" }
            return getComponent(fallbackPath, filename, expectedType, toType, null, expectedId)
        }

        val details: T = try {
            toType(detailsFile)
        } catch (e: Exception) {
            throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} component details: json error: id=$expectedId", e)
        }
        if (details == null) {
            fallbackPath?: throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} component details: incorrect contents: id=$expectedId")
            LOGGER.debug { "Falling back to fallback path loading ${expectedType.name.lowercase(Locale.getDefault())} component id=$expectedId" }
            return getComponent(fallbackPath, filename, expectedType, toType, null, expectedId)
        }
        return Pair(manifest, details)
    }

    @Throws(FileLoadException::class)
    fun cleanupVersions(
        includeLibraries: Boolean
    ) {
        reloadAll()

        LOGGER.debug { "Cleaning up versions..." }
        LOGGER.debug { "Checking for used versions..." }
        val usedVersions: MutableList<String> = mutableListOf()
        for (instance in instanceComponents) {
            usedVersions.add(instance.second.versionComponent)
            LOGGER.debug { "Used version: ${instance.second.versionComponent}" }
        }

        var newFound: Boolean
        do {
            LOGGER.debug { "Checking for dependencies..." }
            newFound = false
            for (version in versionComponents) {
                if (usedVersions.contains(version.first.id)) {
                    version.second.depends?.let {
                        if (!usedVersions.contains(it)) {
                            usedVersions.add(it)
                            newFound = true
                            LOGGER.debug { "Used version: ${version.first.id}" }
                        }
                    }
                }
            }
        } while(newFound)
        LOGGER.debug { "Finished checking for dependencies" }
        LOGGER.debug { "Finished checking for used versions" }

        LOGGER.debug { "Deleting unused versions..."}
        for (version in versionComponents) {
            if (!usedVersions.contains(version.first.id)) {
                LOGGER.debug { "Deleting unused version: ${version.first.id}" }
                versionManifest.components.remove(version.first.id)
                try {
                    LauncherFile.of(version.first.directory).remove()
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
            if(!usedVersions.contains(version.first.id)) {
                unusedLibraries.addAll(version.second.libraries)
                LOGGER.debug { "Unused libraries: ${version.second.libraries}" }
            }
        }
        LOGGER.debug { "Finished collecting libraries in unused versions" }

        LOGGER.debug { "Checking unused libraries against used versions..." }
        for(version in versionComponents) {
            if(usedVersions.contains(version.first.id)) {
                val libsToRemove = unusedLibraries.filter { version.second.libraries.contains(it) }
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