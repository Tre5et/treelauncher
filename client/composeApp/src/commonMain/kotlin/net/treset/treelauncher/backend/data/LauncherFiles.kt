package net.treset.treelauncher.backend.data

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mc_version_loader.json.GenericJsonParsable
import net.treset.mc_version_loader.json.SerializationException
import net.treset.mc_version_loader.launcher.*
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.util.exception.FileLoadException
import net.treset.treelauncher.backend.util.file.LauncherFile
import java.io.IOException
import java.util.*

class LauncherFiles {
    private var _mainManifest: LauncherManifest? = null
    val mainManifest: LauncherManifest
        get() = _mainManifest!!
    private var _launcherDetails: LauncherDetails? = null
    val launcherDetails: LauncherDetails
        get() = _launcherDetails!!
    private var _gameDetailsManifest: LauncherManifest? = null
    val gameDetailsManifest: LauncherManifest
        get() = _gameDetailsManifest!!
    private var _modsManifest: LauncherManifest? = null
    val modsManifest: LauncherManifest
        get() = _modsManifest!!
    private var _modsComponents: Array<Pair<LauncherManifest, LauncherModsDetails>>? = null
    val modsComponents: Array<Pair<LauncherManifest, LauncherModsDetails>>
        get() = _modsComponents!!
    private var _savesManifest: LauncherManifest? = null
    val savesManifest: LauncherManifest
        get() = _savesManifest!!
    private var _savesComponents: Array<LauncherManifest>? = null
    val savesComponents: Array<LauncherManifest>
        get() = _savesComponents!!
    private var _instanceManifest: LauncherManifest? = null
    val instanceManifest: LauncherManifest
        get() = _instanceManifest!!
    private var _instanceComponents: Array<Pair<LauncherManifest, LauncherInstanceDetails>>? = null
    val instanceComponents: Array<Pair<LauncherManifest, LauncherInstanceDetails>>
        get() = _instanceComponents!!
    private var _javaManifest: LauncherManifest? = null
    val javaManifest: LauncherManifest
        get() = _javaManifest!!
    private var _javaComponents: Array<LauncherManifest>? = null
    val javaComponents: Array<LauncherManifest>
        get() = _javaComponents!!
    private var _optionsManifest: LauncherManifest? = null
    val optionsManifest: LauncherManifest
        get() = _optionsManifest!!
    private var _optionsComponents: Array<LauncherManifest>? = null
    val optionsComponents: Array<LauncherManifest>
        get() = _optionsComponents!!
    private var _resourcepackManifest: LauncherManifest? = null
    val resourcepackManifest: LauncherManifest
        get() = _resourcepackManifest!!
    private var _resourcepackComponents: Array<LauncherManifest>? = null
    val resourcepackComponents: Array<LauncherManifest>
        get() = _resourcepackComponents!!
    private var _versionManifest: LauncherManifest? = null
    val versionManifest: LauncherManifest
        get() = _versionManifest!!
    private var _versionComponents: Array<Pair<LauncherManifest, LauncherVersionDetails>>? = null
    val versionComponents: Array<Pair<LauncherManifest, LauncherVersionDetails>>
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
        reloadGameDetailsManifest()
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
            LauncherManifest.fromJson(versionFile)
        } catch (e: SerializationException) {
            throw FileLoadException("Unable to load launcher manifest: json error", e)
        }.also {
            if (it == null || it.type != LauncherManifestType.LAUNCHER) {
                throw FileLoadException("Unable to load launcher manifest: incorrect contents")
            }
            it.directory = appConfig().baseDir.absolutePath
            LOGGER.debug { "Loaded launcher manifest" }
        }
    }

    @Throws(FileLoadException::class)
    fun reloadLauncherDetails() {
        _mainManifest?.let {
            it.details?: throw FileLoadException("Unable to load launcher details: invalid main file")

            val detailsFile: String = try {
                LauncherFile.of(appConfig().baseDir, it.details).readString()
            } catch (e: IOException) {
                throw FileLoadException("Unable to load launcher details: file error", e)
            }

            _launcherDetails = try {
                LauncherDetails.fromJson(detailsFile)
            } catch (e: SerializationException) {
                throw FileLoadException("Unable to load launcher details: json error", e)
            }.also { details ->
                if (details.versionDir == null || details.versionType == null || details.versionComponentType == null || details.savesType == null || details.savesComponentType == null || details.resourcepacksType == null || details.resourcepacksComponentType == null || details.resourcepacksDir == null || details.assetsDir == null || details.gamedataDir == null || details.gamedataType == null || details.instancesDir == null || details.instanceComponentType == null || details.instancesType == null || details.javaComponentType == null || details.javasDir == null || details.javasType == null || details.librariesDir == null || details.modsComponentType == null || details.modsType == null || details.optionsDir == null || details.optionsComponentType == null || details.optionsType == null || details.savesComponentType == null || details.savesType == null) {
                    throw FileLoadException("Unable to load launcher details: incorrect contents")
                }
                LOGGER.debug { "Loaded launcher details" }
            }

        }?: throw FileLoadException("Unable to load launcher details: invalid main file")
    }

    @Throws(FileLoadException::class)
    fun reloadGameDetailsManifest() {
        _gameDetailsManifest = reloadManifest(
            LauncherFile.ofData(_launcherDetails?.gamedataDir ?: throw FileLoadException("Unable to load game details manifest: invalid configuration")),
            LauncherManifestType.GAME
        )
    }

    @Throws(FileLoadException::class)
    fun reloadModsManifest() {
        _modsManifest = reloadManifest(
            LauncherFile.ofData(_launcherDetails?.gamedataDir ?: throw FileLoadException("Unable to load mods manifest: invalid configuration")),
            _gameDetailsManifest?.components?.get(0)?: throw FileLoadException("Unable to load mods manifest: invalid configuration"),
            LauncherManifestType.MODS
        )
    }

    @Throws(FileLoadException::class)
    fun reloadModsComponents() {
        _modsComponents = reloadComponents(
            _modsManifest?: throw FileLoadException("Unable to load mods components: invalid configuration"),
            LauncherFile.ofData(_launcherDetails?.gamedataDir ?: throw FileLoadException("Unable to load mods components: invalid configuration")),
            LauncherManifestType.MODS_COMPONENT,
            LauncherModsDetails::fromJson,
            {
                it.types = types
                it.versions = versions
                it.mods = mods
            },
            LauncherFile.ofData(
                _launcherDetails?.gamedataDir ?: throw FileLoadException("Unable to load mods components: invalid configuration"),
                "mods"
            ),
            _modsComponents?: emptyArray()
        )
    }

    @Throws(FileLoadException::class)
    fun reloadSavesManifest() {
        _savesManifest = reloadManifest(
            LauncherFile.ofData(_launcherDetails?.gamedataDir ?: throw FileLoadException("Unable to load saves manifest: invalid configuration")),
            _gameDetailsManifest?.components?.get(1)?: throw FileLoadException("Unable to load saves manifest: invalid configuration"),
            LauncherManifestType.SAVES
        )
    }

    @Throws(FileLoadException::class)
    fun reloadSavesComponents() {
        _savesComponents = reloadComponents(
            _savesManifest?: throw FileLoadException("Unable to load saves components: invalid configuration"),
            LauncherFile.ofData(_launcherDetails?.gamedataDir ?: throw FileLoadException("Unable to load saves components: invalid configuration")),
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
        _instanceManifest = reloadManifest(
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
            { LauncherInstanceDetails.fromJson(it) },
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
            reloadManifest(
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
        _optionsManifest = reloadManifest(
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
        _resourcepackManifest = reloadManifest(
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
        _versionManifest = reloadManifest(
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
            { LauncherVersionDetails.fromJson(it, LauncherVersionDetails::class.java) },
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
    fun reloadManifest(path: LauncherFile, expectedType: LauncherManifestType): LauncherManifest {
        return reloadManifest(path, appConfig().manifestFileName, expectedType)
    }

    @Throws(FileLoadException::class)
    fun reloadManifest(path: LauncherFile, filename: String, expectedType: LauncherManifestType): LauncherManifest {
        val versionFile: String = try {
            LauncherFile.of(path, filename).readString()
        } catch (e: IOException) {
            throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} manifest: file error", e)
        }
        val out: LauncherManifest = try {
            LauncherManifest.fromJson(versionFile, launcherDetails.typeConversion ?: throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} manifest: invalid configuration"))
        } catch (e: SerializationException) {
            throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} manifest: json error", e)
        }
        if (out.type != expectedType) {
            throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} manifest: incorrect contents")
        }
        out.directory = path.absolutePath
        LOGGER.debug { "Loaded " + expectedType.name.lowercase(Locale.getDefault()) + " manifest" }
        return out
    }

    @Throws(FileLoadException::class)
    fun reloadComponents(
        parentManifest: LauncherManifest,
        parentPath: LauncherFile,
        expectedType: LauncherManifestType,
        fallbackPath: LauncherFile?,
        currentComponents: Array<LauncherManifest>
    ): Array<LauncherManifest> {
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
        parentManifest: LauncherManifest,
        parentPath: LauncherFile,
        filename: String,
        expectedType: LauncherManifestType,
        fallbackPath: LauncherFile?,
        currentComponents: Array<LauncherManifest>
    ): Array<LauncherManifest> {
        val out: MutableList<LauncherManifest> = mutableListOf()
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
                    it.details = manifest.details
                    it.prefix = manifest.prefix
                    it.lastUsed = manifest.lastUsed
                    it.includedFiles = manifest.includedFiles
                    it.components = manifest.components
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
        list: MutableList<LauncherManifest>,
        path: LauncherFile,
        filename: String,
        expectedType: LauncherManifestType,
        expectedId: String,
        fallbackPath: LauncherFile?
    ): LauncherManifest {
        val manifestFile: String = try {
            LauncherFile.of(path, filename).readString()
        } catch (e: IOException) {
            fallbackPath?: throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} component: file error: id=$expectedId", e)
            LOGGER.debug { "Falling back to fallback path loading " + expectedType.name.lowercase(Locale.getDefault()) + " component: file error: id=" + expectedId }
            return getComponent(list, fallbackPath, filename, expectedType, expectedId, null)
        }
        val manifest: LauncherManifest = try {
            LauncherManifest.fromJson(manifestFile, _launcherDetails?.typeConversion?: throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} component: invalid configuration"))
        } catch (e: SerializationException) {
            throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} component: json error: id=$expectedId", e)
        }
        if (manifest.type == null || manifest.type != expectedType || manifest.id == null || manifest.id != expectedId) {
            fallbackPath?: throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} component: incorrect contents: id=$expectedId")
            LOGGER.debug { "Falling back to fallback path loading ${expectedType.name.lowercase(Locale.getDefault())} component id=$expectedId" }
            return getComponent(list, fallbackPath, filename, expectedType, expectedId, null)
        }
        manifest.directory = path.absolutePath
        return manifest
    }

    @Throws(FileLoadException::class)
    fun <T : GenericJsonParsable?> reloadComponents(
        parentManifest: LauncherManifest,
        parentDir: LauncherFile,
        expectedType: LauncherManifestType,
        toType: (String) -> T,
        copyTo: T.(T) -> Unit,
        fallbackPath: LauncherFile?,
        currentComponents: Array<Pair<LauncherManifest, T>>
    ): Array<Pair<LauncherManifest, T>> {
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
        parentManifest: LauncherManifest,
        parentPath: LauncherFile,
        filename: String,
        expectedType: LauncherManifestType,
        toType: (String) -> T,
        copyTo: T.(T) -> Unit,
        fallbackPath: LauncherFile?,
        currentComponents: Array<Pair<LauncherManifest, T>>
    ): Array<Pair<LauncherManifest, T>> {
        if (parentManifest.prefix == null || parentManifest.components == null) {
            throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} components: invalid configuration")
        }
        val out: MutableList<Pair<LauncherManifest, T>> = mutableListOf()
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
                    it.first.details = component.first.details
                    it.first.prefix = component.first.prefix
                    it.first.lastUsed = component.first.lastUsed
                    it.first.includedFiles = component.first.includedFiles
                    it.first.components = component.first.components
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
    ): Pair<LauncherManifest, T> {
        val manifestFile: String = try {
            LauncherFile.of(path, filename).readString()
        } catch (e: IOException) {
            fallbackPath?: throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} component: file error: id=$expectedId", e)
            LOGGER.debug { "Falling back to fallback path loading ${expectedType.name.lowercase(Locale.getDefault())} component: file error: id=$expectedId" }
            return getComponent(fallbackPath, filename, expectedType, toType, null, expectedId)
        }
        val manifest: LauncherManifest = try {
            LauncherManifest.fromJson(manifestFile, launcherDetails.typeConversion?: throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} component: invalid configuration"))
        } catch (e: SerializationException) {
            throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} component: json error: id=$expectedId, e")
        }
        if (manifest.type == null || manifest.type != expectedType || manifest.id == null || manifest.id != expectedId || manifest.details == null) {
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
        } catch (e: SerializationException) {
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
