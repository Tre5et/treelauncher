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
    private var mainManifest: LauncherManifest? = null
    private var launcherDetails: LauncherDetails? = null
    private var gameDetailsManifest: LauncherManifest? = null
    private var modsManifest: LauncherManifest? = null
    private var modsComponents: List<Pair<LauncherManifest, LauncherModsDetails>?>? = null
    private var savesManifest: LauncherManifest? = null
    private var savesComponents: List<LauncherManifest?>? = null
    private var instanceManifest: LauncherManifest? = null
    private var instanceComponents: List<Pair<LauncherManifest, LauncherInstanceDetails>?>? = null
    private var javaManifest: LauncherManifest? = null
    private var javaComponents: List<LauncherManifest?>? = null
    private var optionsManifest: LauncherManifest? = null
    private var optionsComponents: List<LauncherManifest?>? = null
    private var resourcepackManifest: LauncherManifest? = null
    private var resourcepackComponents: List<LauncherManifest?>? = null
    private var versionManifest: LauncherManifest? = null
    private var versionComponents: List<Pair<LauncherManifest, LauncherVersionDetails>?>? = null

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

    fun getMainManifest(): LauncherManifest? {
        return mainManifest
    }

    @Throws(FileLoadException::class)
    fun reloadMainManifest() {
        val versionFile: String = try {
            LauncherFile.of(appConfig().BASE_DIR, appConfig().MANIFEST_FILE_NAME).readString()
        } catch (e: IOException) {
            throw FileLoadException("Unable to load launcher manifest: file error", e)
        }

        mainManifest = try {
            LauncherManifest.fromJson(versionFile)
        } catch (e: SerializationException) {
            throw FileLoadException("Unable to load launcher manifest: json error", e)
        }.let {
            if (it == null || it.type != LauncherManifestType.LAUNCHER) {
                throw FileLoadException("Unable to load launcher manifest: incorrect contents")
            }
            it.directory = appConfig().BASE_DIR.absolutePath
            LOGGER.debug { "Loaded launcher manifest" }
            null
        }
    }

    fun getLauncherDetails(): LauncherDetails? {
        return launcherDetails
    }

    @Throws(FileLoadException::class)
    fun reloadLauncherDetails() {
        mainManifest?.let {
            it.details?: throw FileLoadException("Unable to load launcher details: invalid main file")

            val detailsFile: String = try {
                LauncherFile.of(appConfig().BASE_DIR, it.details).readString()
            } catch (e: IOException) {
                throw FileLoadException("Unable to load launcher details: file error", e)
            }

            launcherDetails = try {
                LauncherDetails.fromJson(detailsFile)
            } catch (e: SerializationException) {
                throw FileLoadException("Unable to load launcher details: json error", e)
            }.let { details ->
                if (details.versionDir == null || details.versionType == null || details.versionComponentType == null || details.savesType == null || details.savesComponentType == null || details.resourcepacksType == null || details.resourcepacksComponentType == null || details.resourcepacksDir == null || details.assetsDir == null || details.gamedataDir == null || details.gamedataType == null || details.instancesDir == null || details.instanceComponentType == null || details.instancesType == null || details.javaComponentType == null || details.javasDir == null || details.javasType == null || details.librariesDir == null || details.modsComponentType == null || details.modsType == null || details.optionsDir == null || details.optionsComponentType == null || details.optionsType == null || details.savesComponentType == null || details.savesType == null) {
                    throw FileLoadException("Unable to load launcher details: incorrect contents")
                }
                LOGGER.debug { "Loaded launcher details" }
                null
            }

        }?: throw FileLoadException("Unable to load launcher details: invalid main file")
    }

    fun getGameDetailsManifest(): LauncherManifest? {
        return gameDetailsManifest
    }

    @Throws(FileLoadException::class)
    fun reloadGameDetailsManifest() {
        gameDetailsManifest = reloadManifest(
            LauncherFile.ofRelative(launcherDetails?.gamedataDir ?: throw FileLoadException("Unable to load game details manifest: invalid configuration")),
            LauncherManifestType.GAME
        )
    }

    fun getModsManifest(): LauncherManifest? {
        return modsManifest
    }

    @Throws(FileLoadException::class)
    fun reloadModsManifest() {
        modsManifest = reloadManifest(
            LauncherFile.ofRelative(launcherDetails?.gamedataDir ?: throw FileLoadException("Unable to load mods manifest: invalid configuration")),
            gameDetailsManifest?.components?.get(0)?: throw FileLoadException("Unable to load mods manifest: invalid configuration"),
            LauncherManifestType.MODS
        )
    }

    fun getModsComponents(): List<Pair<LauncherManifest, LauncherModsDetails>?>? {
        return modsComponents
    }

    @Throws(FileLoadException::class)
    fun reloadModsComponents() {
        modsComponents = reloadComponents(
            modsManifest?: throw FileLoadException("Unable to load mods components: invalid configuration"),
            LauncherFile.ofRelative(launcherDetails?.gamedataDir ?: throw FileLoadException("Unable to load mods components: invalid configuration")),
            LauncherManifestType.MODS_COMPONENT,
            LauncherModsDetails::class.java,
            LauncherFile.ofRelative(
                launcherDetails?.gamedataDir ?: throw FileLoadException("Unable to load mods components: invalid configuration"),
                "mods"
            )
        )
    }

    fun getSavesManifest(): LauncherManifest? {
        return savesManifest
    }

    @Throws(FileLoadException::class)
    fun reloadSavesManifest() {
        savesManifest = reloadManifest(
            LauncherFile.ofRelative(launcherDetails?.gamedataDir ?: throw FileLoadException("Unable to load saves manifest: invalid configuration")),
            gameDetailsManifest?.components?.get(1)?: throw FileLoadException("Unable to load saves manifest: invalid configuration"),
            LauncherManifestType.SAVES
        )
    }

    fun getSavesComponents(): List<LauncherManifest?>? {
        return savesComponents
    }

    @Throws(FileLoadException::class)
    fun reloadSavesComponents() {
        savesComponents = reloadComponents(
            savesManifest?: throw FileLoadException("Unable to load saves components: invalid configuration"),
            LauncherFile.ofRelative(launcherDetails?.gamedataDir ?: throw FileLoadException("Unable to load saves components: invalid configuration")),
            LauncherManifestType.SAVES_COMPONENT,
            LauncherFile.ofRelative(
                launcherDetails?.gamedataDir ?: throw FileLoadException("Unable to load saves components: invalid configuration"),
                "saves"
            )
        )
    }

    fun getInstanceManifest(): LauncherManifest? {
        return instanceManifest
    }

    @Throws(FileLoadException::class)
    fun reloadInstanceManifest() {
        instanceManifest = reloadManifest(
            LauncherFile.ofRelative(launcherDetails?.instancesDir ?: throw FileLoadException("Unable to load instance manifest: invalid configuration")),
            LauncherManifestType.INSTANCES
        )
    }

    fun getInstanceComponents(): List<Pair<LauncherManifest, LauncherInstanceDetails>?>? {
        return instanceComponents
    }

    @Throws(FileLoadException::class)
    fun reloadInstanceComponents() {
        instanceComponents = reloadComponents(
            instanceManifest?: throw FileLoadException("Unable to load instance components: invalid configuration"),
            LauncherFile.ofRelative(launcherDetails?.instancesDir ?: throw FileLoadException("Unable to load instance components: invalid configuration")),
            LauncherManifestType.INSTANCE_COMPONENT,
            LauncherInstanceDetails::class.java,
            null
        )
    }

    fun getJavaManifest(): LauncherManifest? {
        return javaManifest
    }

    @Throws(FileLoadException::class)
    fun reloadJavaManifest() {
        javaManifest =
            reloadManifest(
                LauncherFile.ofRelative(launcherDetails?.javasDir ?: throw FileLoadException("Unable to load java manifest: invalid configuration")),
                LauncherManifestType.JAVAS
            )
    }

    fun getJavaComponents(): List<LauncherManifest?>? {
        return javaComponents
    }

    @Throws(FileLoadException::class)
    fun reloadJavaComponents() {
        javaComponents = reloadComponents(
            javaManifest?: throw FileLoadException("Unable to load java components: invalid configuration"),
            LauncherFile.ofRelative(launcherDetails?.javasDir ?: throw FileLoadException("Unable to load java components: invalid configuration")),
            LauncherManifestType.JAVA_COMPONENT,
            null
        )
    }

    fun getOptionsManifest(): LauncherManifest? {
        return optionsManifest
    }

    @Throws(FileLoadException::class)
    fun reloadOptionsManifest() {
        optionsManifest = reloadManifest(
            LauncherFile.ofRelative(launcherDetails?.optionsDir ?: throw FileLoadException("Unable to load options manifest: invalid configuration")),
            LauncherManifestType.OPTIONS
        )
    }

    fun getOptionsComponents(): List<LauncherManifest?>? {
        return optionsComponents
    }

    @Throws(FileLoadException::class)
    fun reloadOptionsComponents() {
        optionsComponents = reloadComponents(
            optionsManifest?: throw FileLoadException("Unable to load options components: invalid configuration"),
            LauncherFile.ofRelative(launcherDetails?.optionsDir ?: throw FileLoadException("Unable to load options components: invalid configuration")),
            LauncherManifestType.OPTIONS_COMPONENT,
            null
        )
    }

    fun getResourcepackManifest(): LauncherManifest? {
        return resourcepackManifest
    }

    @Throws(FileLoadException::class)
    fun reloadResourcepackManifest() {
        resourcepackManifest = reloadManifest(
            LauncherFile.ofRelative(launcherDetails?.resourcepacksDir ?: throw FileLoadException("Unable to load resourcepack manifest: invalid configuration")),
            LauncherManifestType.RESOURCEPACKS
        )
    }

    fun getResourcepackComponents(): List<LauncherManifest?>? {
        return resourcepackComponents
    }

    @Throws(FileLoadException::class)
    fun reloadResourcepackComponents() {
        resourcepackComponents = reloadComponents(
            resourcepackManifest?: throw FileLoadException("Unable to load resourcepack components: invalid configuration"),
            LauncherFile.ofRelative(launcherDetails?.resourcepacksDir?: throw FileLoadException("Unable to load resourcepack components: invalid configuration")),
            LauncherManifestType.RESOURCEPACKS_COMPONENT,
            null
        )
    }

    fun getVersionManifest(): LauncherManifest? {
        return versionManifest
    }

    @Throws(FileLoadException::class)
    fun reloadVersionManifest() {
        versionManifest = reloadManifest(
            LauncherFile.ofRelative(launcherDetails?.versionDir ?: throw FileLoadException("Unable to load version manifest: invalid configuration")),
            LauncherManifestType.VERSIONS
        )
    }

    fun getVersionComponents(): List<Pair<LauncherManifest, LauncherVersionDetails>?>? {
        return versionComponents
    }

    @Throws(FileLoadException::class)
    fun reloadVersionComponents() {
        versionComponents = reloadComponents(
            versionManifest?: throw FileLoadException("Unable to load version components: invalid configuration"),
            LauncherFile.ofRelative(launcherDetails?.versionDir ?: throw FileLoadException("Unable to load version components: invalid configuration")),
            LauncherManifestType.VERSION_COMPONENT,
            LauncherVersionDetails::class.java,
            null
        )
    }

    @Throws(FileLoadException::class)
    fun reloadManifest(path: LauncherFile, expectedType: LauncherManifestType): LauncherManifest {
        return reloadManifest(path, appConfig().MANIFEST_FILE_NAME, expectedType)
    }

    @Throws(FileLoadException::class)
    fun reloadManifest(path: LauncherFile, filename: String, expectedType: LauncherManifestType): LauncherManifest {
        val versionFile: String = try {
            LauncherFile.of(path, filename).readString()
        } catch (e: IOException) {
            throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} manifest: file error", e)
        }
        val out: LauncherManifest = try {
            LauncherManifest.fromJson(versionFile, getLauncherDetails()?.typeConversion ?: throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} manifest: invalid configuration"))
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
        fallbackPath: LauncherFile?
    ): List<LauncherManifest?> {
        return reloadComponents(
            parentManifest,
            parentPath,
            appConfig().MANIFEST_FILE_NAME,
            expectedType,
            fallbackPath
        )
    }

    @Throws(FileLoadException::class)
    fun reloadComponents(
        parentManifest: LauncherManifest,
        parentPath: LauncherFile,
        filename: String,
        expectedType: LauncherManifestType,
        fallbackPath: LauncherFile?
    ): List<LauncherManifest> {
        val out: MutableList<LauncherManifest> = mutableListOf()
        for (c in parentManifest.components) {
            try {
                addComponent(
                    out,
                    LauncherFile.of(parentPath, "${parentManifest.prefix}_$c"),
                    filename,
                    expectedType,
                    c,
                    fallbackPath
                )
            } catch (e: FileLoadException) {
                throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} components: component error: id=$c")
            }
        }
        LOGGER.debug { "Loaded ${expectedType.name.lowercase(Locale.getDefault())} components" }
        return out
    }

    @Throws(FileLoadException::class)
    private fun addComponent(
        list: MutableList<LauncherManifest>,
        path: LauncherFile,
        filename: String,
        expectedType: LauncherManifestType,
        expectedId: String,
        fallbackPath: LauncherFile?
    ) {
        val manifestFile: String = try {
            LauncherFile.of(path, filename).readString()
        } catch (e: IOException) {
            fallbackPath?: throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} component: file error: id=$expectedId", e)
            LOGGER.debug { "Falling back to fallback path loading " + expectedType.name.lowercase(Locale.getDefault()) + " component: file error: id=" + expectedId }
            addComponent(list, fallbackPath, filename, expectedType, expectedId, null)
            return
        }
        val manifest: LauncherManifest = try {
            LauncherManifest.fromJson(manifestFile, launcherDetails?.typeConversion?: throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} component: invalid configuration"))
        } catch (e: SerializationException) {
            throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} component: json error: id=$expectedId", e)
        }
        if (manifest.type == null || manifest.type != expectedType || manifest.id == null || manifest.id != expectedId) {
            fallbackPath?: throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} component: incorrect contents: id=$expectedId")
            LOGGER.debug { "Falling back to fallback path loading ${expectedType.name.lowercase(Locale.getDefault())} component id=$expectedId" }
            addComponent(list, fallbackPath, filename, expectedType, expectedId, null)
            return
        }
        manifest.directory = path.absolutePath
        list.add(manifest)
    }

    @Throws(FileLoadException::class)
    fun <T : GenericJsonParsable?> reloadComponents(
        parentManifest: LauncherManifest,
        parentDir: LauncherFile,
        expectedType: LauncherManifestType,
        targetClass: Class<T>,
        fallbackPath: LauncherFile?
    ): List<Pair<LauncherManifest, T>> {
        return reloadComponents(
            parentManifest,
            parentDir,
            appConfig().MANIFEST_FILE_NAME,
            expectedType,
            targetClass,
            fallbackPath
        )
    }

    @Throws(FileLoadException::class)
    fun <T : GenericJsonParsable?> reloadComponents(
        parentManifest: LauncherManifest,
        parentPath: LauncherFile,
        filename: String,
        expectedType: LauncherManifestType,
        targetClass: Class<T>,
        fallbackPath: LauncherFile?
    ): List<Pair<LauncherManifest, T>> {
        if (parentManifest.prefix == null || parentManifest.components == null) {
            throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} components: invalid configuration")
        }
        val out: MutableList<Pair<LauncherManifest, T>> = mutableListOf()
        val exceptionQueue: MutableList<FileLoadException> = mutableListOf()
        for (c in parentManifest.components) {
            try {
                addComponent(
                    out,
                    LauncherFile.of(parentPath, "${parentManifest.prefix}_$c"),
                    filename,
                    expectedType,
                    targetClass,
                    fallbackPath,
                    c
                )
            } catch (e: FileLoadException) {
                exceptionQueue.add(e)
            }
        }
        if (exceptionQueue.isNotEmpty()) {
            throw FileLoadException("Unable to load ${exceptionQueue.size} components: component error", exceptionQueue[0])
        }
        LOGGER.debug { "Loaded ${expectedType.name.lowercase(Locale.getDefault())} components" }
        return out
    }

    @Throws(FileLoadException::class)
    private fun <T : GenericJsonParsable?> addComponent(
        list: MutableList<Pair<LauncherManifest, T>>,
        path: LauncherFile,
        filename: String,
        expectedType: LauncherManifestType,
        targetClass: Class<T>,
        fallbackPath: LauncherFile?,
        expectedId: String
    ) {
        val manifestFile: String = try {
            LauncherFile.of(path, filename).readString()
        } catch (e: IOException) {
            fallbackPath?: throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} component: file error: id=$expectedId", e)
            LOGGER.debug { "Falling back to fallback path loading ${expectedType.name.lowercase(Locale.getDefault())} component: file error: id=$expectedId" }
            addComponent(list, fallbackPath, filename, expectedType, targetClass, null, expectedId)
            return
        }
        val manifest: LauncherManifest = try {
            LauncherManifest.fromJson(manifestFile, getLauncherDetails()?.typeConversion?: throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} component: invalid configuration"))
        } catch (e: SerializationException) {
            throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} component: json error: id=$expectedId, e")
        }
        if (manifest.type == null || manifest.type != expectedType || manifest.id == null || manifest.id != expectedId || manifest.details == null) {
            fallbackPath?: throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} component: incorrect contents: id=$expectedId")
            LOGGER.debug { "Falling back to fallback path loading ${expectedType.name.lowercase(Locale.getDefault())} component id=$expectedId" }
            addComponent(list, fallbackPath, filename, expectedType, targetClass, null, expectedId)
            return
        }

        manifest.directory = path.absolutePath
        val detailsFile = try {
            LauncherFile.of(path, manifest.details).readString()
        } catch (e: IOException) {
            fallbackPath?: throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} component details: file error: id=$expectedId", e)
            LOGGER.debug { "Falling back to fallback path loading ${expectedType.name.lowercase(Locale.getDefault())} component id=$expectedId" }
            addComponent(list, fallbackPath, filename, expectedType, targetClass, null, expectedId)
            return
        }

        val details: T = try {
            GenericJsonParsable.fromJson(detailsFile, targetClass)
        } catch (e: SerializationException) {
            throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} component details: json error: id=$expectedId", e)
        }
        if (details == null) {
            fallbackPath?: throw FileLoadException("Unable to load ${expectedType.name.lowercase(Locale.getDefault())} component details: incorrect contents: id=$expectedId")
            LOGGER.debug { "Falling back to fallback path loading ${expectedType.name.lowercase(Locale.getDefault())} component id=$expectedId" }
            addComponent(list, fallbackPath, filename, expectedType, targetClass, null, expectedId)
            return
        }
        list.add(Pair(manifest, details))
    }

    companion object {
        private val LOGGER = KotlinLogging.logger{}
    }
}
