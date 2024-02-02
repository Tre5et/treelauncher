package net.treset.treelauncher.backend.creation

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mc_version_loader.assets.AssetIndex
import net.treset.mc_version_loader.assets.MinecraftAssets
import net.treset.mc_version_loader.exception.FileDownloadException
import net.treset.mc_version_loader.fabric.FabricLibrary
import net.treset.mc_version_loader.fabric.FabricLoader
import net.treset.mc_version_loader.fabric.FabricProfile
import net.treset.mc_version_loader.fabric.FabricVersionDetails
import net.treset.mc_version_loader.json.SerializationException
import net.treset.mc_version_loader.launcher.LauncherLaunchArgument
import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.mc_version_loader.launcher.LauncherManifestType
import net.treset.mc_version_loader.launcher.LauncherVersionDetails
import net.treset.mc_version_loader.minecraft.MinecraftGame
import net.treset.mc_version_loader.minecraft.MinecraftLaunchArgument
import net.treset.mc_version_loader.minecraft.MinecraftVersion
import net.treset.mc_version_loader.minecraft.MinecraftVersionDetails
import net.treset.mc_version_loader.util.DownloadStatus
import net.treset.mc_version_loader.util.FileUtil
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.data.LauncherFiles
import net.treset.treelauncher.backend.util.CreationStatus
import net.treset.treelauncher.backend.util.exception.ComponentCreationException
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.backend.util.string.PatternString
import java.io.File
import java.io.IOException

class VersionCreator : GenericComponentCreator {
    var mcVersion: MinecraftVersionDetails? = null
    var fabricVersion: FabricVersionDetails? = null
    var fabricProfile: FabricProfile? = null
    var files: LauncherFiles? = null
    var librariesDir: LauncherFile? = null

    constructor(
        typeConversion: Map<String, LauncherManifestType>?,
        componentsManifest: LauncherManifest?,
        mcVersion: MinecraftVersionDetails,
        files: LauncherFiles?,
        librariesDir: LauncherFile?
    ) : super(
        LauncherManifestType.VERSION_COMPONENT,
        null,
        null,
        mcVersion.id,
        typeConversion,
        null,
        appConfig().versionsDefaultDetails,
        componentsManifest
    ) {
        this.mcVersion = mcVersion
        this.files = files
        this.librariesDir = librariesDir
        defaultStatus = CreationStatus(CreationStatus.DownloadStep.VERSION, null)
    }

    constructor(
        typeConversion: Map<String, LauncherManifestType>?,
        componentsManifest: LauncherManifest?,
        fabricVersion: FabricVersionDetails?,
        fabricProfile: FabricProfile,
        files: LauncherFiles?,
        librariesDir: LauncherFile?
    ) : super(
        LauncherManifestType.VERSION_COMPONENT,
        null,
        null,
        fabricProfile.id,
        typeConversion,
        null,
        appConfig().versionsDefaultDetails,
        componentsManifest
    ) {
        this.fabricVersion = fabricVersion
        this.fabricProfile = fabricProfile
        this.files = files
        this.librariesDir = librariesDir
        defaultStatus = CreationStatus(CreationStatus.DownloadStep.VERSION, null)
    }

    constructor(uses: Pair<LauncherManifest, LauncherVersionDetails>) : super(
        LauncherManifestType.VERSION_COMPONENT,
        uses.first,
        null,
        null,
        null,
        null,
        null,
        null
    ) {
        defaultStatus = CreationStatus(CreationStatus.DownloadStep.VERSION, null)
    }

    @Throws(ComponentCreationException::class)
    override fun createComponent(): String {
        files?.let{files ->
            for (v in files.versionComponents) {
                if (v.second.versionId != null && (mcVersion != null && v.second.versionId.equals(mcVersion?.id)
                            || fabricProfile != null && v.second.versionId.equals(fabricProfile?.id))
                ) {
                    LOGGER.debug { "Matching version already exists, using instead: versionId=${v.second.versionId}, usingId=${v.first.id}" }
                    uses = v.first
                    return useComponent()
                }
            }
            val result = super.createComponent()
            if (newManifest == null || mcVersion == null && fabricVersion == null) {
                attemptCleanup()
                throw ComponentCreationException("Failed to create version component: invalid data")
            }
            makeVersion()
            LOGGER.debug { "Created version component: id=${newManifest!!.id}" }
            return result
        }
        attemptCleanup()
        throw ComponentCreationException("Failed to create version component: invalid data")
    }

    @Throws(ComponentCreationException::class)
    override fun inheritComponent(): String {
        throw ComponentCreationException("Unable to inherit version: not supported")
    }

    @Throws(ComponentCreationException::class)
    private fun makeVersion() {
        if (fabricVersion != null) {
            makeFabricVersion()
            return
        }
        makeMinecraftVersion()
    }

    @Throws(ComponentCreationException::class)
    private fun makeFabricVersion() {
        setStatus(CreationStatus(CreationStatus.DownloadStep.VERSION_FABRIC, null))
        if (fabricProfile == null || fabricProfile!!.inheritsFrom == null || fabricVersion == null) {
            throw ComponentCreationException("Unable to create fabric version: no valid fabric profile")
        }
        fabricProfile!!.let { fabricProfile ->
            val versions: List<MinecraftVersion> = try {
                MinecraftGame.getVersions()
            } catch (e: FileDownloadException) {
                throw ComponentCreationException("Unable to create fabric version: failed to get mc versions", e)
            }
            for (m in versions) {
                if (fabricProfile.inheritsFrom == m.id) {
                    val mcJson: String = try {
                        FileUtil.getStringFromUrl(m.url)
                    } catch (e: FileDownloadException) {
                        throw ComponentCreationException("Unable to create fabric version: failed to download mc version details: versionId=${fabricProfile.inheritsFrom}", e)
                    }
                    val mcCreator: VersionCreator = try {
                        VersionCreator(
                            typeConversion,
                            componentsManifest,
                            MinecraftVersionDetails.fromJson(mcJson),
                            files,
                            librariesDir
                        )
                    } catch (e: SerializationException) {
                        throw ComponentCreationException("Unable to create fabric version: failed to parse mc version details: versionId=${fabricProfile.inheritsFrom}", e)
                    }
                    mcCreator.statusCallback = statusCallback
                    val dependsId: String = try {
                        mcCreator.id
                    } catch (e: ComponentCreationException) {
                        throw ComponentCreationException("Unable to create fabric version: failed to create mc version: versionId=${fabricProfile.inheritsFrom}", e)
                    }
                    val details = LauncherVersionDetails(
                        fabricProfile.inheritsFrom,
                        "fabric",
                        fabricVersion!!.loader.version,
                        null,
                        dependsId,
                        null,
                        null,
                        null,
                        null,
                        fabricProfile.mainClass,
                        null,
                        fabricProfile.id
                    )
                    try {
                        addFabricArguments(details)
                        addFabricLibraries(details)
                        addFabricFile(details)
                    } catch (e: ComponentCreationException) {
                        mcCreator.attemptCleanup()
                        attemptCleanup()
                        throw ComponentCreationException("Unable to create fabric version: versionId=${fabricProfile.inheritsFrom}", e)
                    }
                    try {
                        LauncherFile.of(newManifest!!.directory, newManifest!!.details).write(details)
                    } catch (e: IOException) {
                        throw ComponentCreationException("Unable to create fabric version: failed to write version details: versionId=${fabricProfile.inheritsFrom}", e)
                    }
                    LOGGER.debug { "Created fabric version: id=${newManifest?.id}" }
                    return
                }
            }
            throw ComponentCreationException("Unable to create fabric version: failed to find mc version: versionId=${fabricProfile.inheritsFrom}")
        }
    }

    @Throws(ComponentCreationException::class)
    private fun addFabricFile(details: LauncherVersionDetails) {
        newManifest?.let { newManifest ->
            val baseDir = File(newManifest.directory)
            if (!baseDir.isDirectory()) {
                throw ComponentCreationException("Unable to add fabric file: base dir is not a directory")
            }
            fabricVersion?.let { fabricVersion ->

                try {
                    FabricLoader.downloadFabricLoader(baseDir, fabricVersion.loader)
                } catch (e: FileDownloadException) {
                    throw ComponentCreationException("Unable to add fabric file: failed to download fabric loader", e)
                }
                details.mainFile = appConfig().fabricDefaultClientFileName
                LOGGER.debug { "Added fabric file: mainFile=${details.mainFile}" }
            }?: {
                throw ComponentCreationException("Unable to add fabric file: fabricVersion is null")
            }
        }?: {
            throw ComponentCreationException("Unable to add fabric file: newManifest is null")
        }
    }

    @Throws(ComponentCreationException::class)
    private fun addFabricLibraries(details: LauncherVersionDetails) {
        setStatus(CreationStatus(CreationStatus.DownloadStep.VERSION_LIBRARIES, null))
        fabricProfile?.libraries?.let {libraries: List<FabricLibrary> ->
            librariesDir?.let {dir ->
                if (!dir.isDirectory()) {
                    try {
                        dir.createDir()
                    } catch (e: IOException) {
                        throw ComponentCreationException("Unable to add fabric libraries: failed to create libraries directory: path=$librariesDir", e)
                    }
                }
                val loaderPattern = PatternString(":fabric-loader:")
                val clientLibs: List<FabricLibrary> = ArrayList<FabricLibrary>(libraries).stream().filter { !loaderPattern.matches(it.name) }.toList()
                val libs: List<String> = try {
                    FabricLoader.downloadFabricLibraries(
                        librariesDir,
                        clientLibs
                    ) { status: DownloadStatus? ->
                        setStatus(
                            CreationStatus(
                                CreationStatus.DownloadStep.VERSION_LIBRARIES,
                                status
                            )
                        )
                    }
                } catch (e: FileDownloadException) {
                    throw ComponentCreationException("Unable to add fabric libraries: failed to download libraries", e)
                }
                details.libraries = libs
                LOGGER.debug { "Added fabric libraries" }
            }?: {
                throw ComponentCreationException("Unable to add fabric libraries: librariesDir is null")
            }
        }?: {
            throw ComponentCreationException("Unable to add fabric libraries: libraries invalid")
        }
    }

    @Throws(ComponentCreationException::class)
    private fun addFabricArguments(details: LauncherVersionDetails) {
        fabricProfile?.let { fabricProfile ->
            details.jvmArguments = translateArguments(
                fabricProfile.launchArguments.jvm,
                appConfig().fabricDefaultJvmArguments
            )
            details.gameArguments = translateArguments(
                fabricProfile.launchArguments.game,
                appConfig().fabricDefaultGameArguments
            )
            LOGGER.debug { "Added fabric arguments" }
        }?: {
            throw ComponentCreationException("Unable to add fabric arguments: fabricProfile is null")
        }
    }

    @Throws(ComponentCreationException::class)
    private fun makeMinecraftVersion() {
        mcVersion?.let {mcVersion ->
            setStatus(CreationStatus(CreationStatus.DownloadStep.VERSION_VANILLA, null))
            val details = LauncherVersionDetails(
                mcVersion.id,
                "vanilla",
                null,
                mcVersion.assets,
                null,
                null,
                null,
                null,
                null,
                mcVersion.mainClass,
                null,
                mcVersion.id
            )
            try {
                downloadAssets()
                addArguments(details)
                addJava(details)
                addLibraries(details)
                addFile(details)
            } catch (e: ComponentCreationException) {
                attemptCleanup()
                throw ComponentCreationException("Unable to create minecraft version", e)
            }
            newManifest?.let {newManifest ->
                try {
                    LauncherFile.of(newManifest.directory, newManifest.details).write(details)
                } catch (e: IOException) {
                    attemptCleanup()
                    throw ComponentCreationException("Unable to write version details to file", e)
                }
                LOGGER.debug { "${"Created minecraft version: id={}"} ${newManifest.id}" }
            }?: {
                attemptCleanup()
                throw ComponentCreationException("Unable to create minecraft version: invalid data")
            }
        }?: {
            throw ComponentCreationException("Unable to create minecraft version: invalid data")
        }
    }

    @Throws(ComponentCreationException::class)
    private fun downloadAssets() {
        mcVersion?.let {mcVersion ->
            LOGGER.debug { "Downloading assets..." }
            setStatus(CreationStatus(CreationStatus.DownloadStep.VERSION_ASSETS, null))
            val assetIndexUrl: String = mcVersion.assetIndex.url
            val index: AssetIndex = try {
                MinecraftAssets.getAssetIndex(assetIndexUrl)
            } catch (e: FileDownloadException) {
                throw ComponentCreationException("Unable to download assets: failed to download asset index", e)
            }
            if (index.objects == null || index.objects.isEmpty()) {
                throw ComponentCreationException("Unable to download assets: invalid index contents")
            }
            files?.let {files ->
                val baseDir: LauncherFile = LauncherFile.ofData(files.launcherDetails.assetsDir)
                try {
                    MinecraftAssets.downloadAssets(
                        baseDir,
                        index,
                        assetIndexUrl,
                        false
                    ) { status: DownloadStatus? ->
                        setStatus(
                            CreationStatus(
                                CreationStatus.DownloadStep.VERSION_ASSETS,
                                status
                            )
                        )
                    }
                } catch (e: FileDownloadException) {
                    throw ComponentCreationException("Unable to download assets: failed to download assets", e)
                }
                LOGGER.debug { "Downloaded assets" }
            }?: {
                throw ComponentCreationException("Unable to download assets: files is null")
            }
        }?: {
            throw ComponentCreationException("Unable to download assets: invalid data")
        }
    }

    @Throws(ComponentCreationException::class)
    private fun addJava(details: LauncherVersionDetails) {
        mcVersion?.let { mcVersion ->
            val javaName: String = mcVersion.javaVersion.getComponent() ?: throw ComponentCreationException("Unable to add java component: java name is null")
            files?.let { files ->
                for (j in files.javaComponents) {
                    if (javaName == j.name) {
                        details.java = j.id
                        LOGGER.debug { "${"Using existing java component: id={}"} ${j.id}" }
                        return
                    }
                }
                val javaCreator = JavaComponentCreator(javaName, typeConversion!!, files.javaManifest)
                javaCreator.statusCallback = statusCallback
                try {
                    details.java = javaCreator.id
                } catch (e: ComponentCreationException) {
                    throw ComponentCreationException("Unable to add java component: failed to create java component", e)
                }
            }?: {
                throw ComponentCreationException("Unable to add java component: files is null")
            }
        }?: {
            throw ComponentCreationException("Unable to add java component: invalid data")
        }
    }

    @Throws(ComponentCreationException::class)
    private fun addLibraries(details: LauncherVersionDetails) {
        mcVersion?.let { mcVersion ->
            setStatus(CreationStatus(CreationStatus.DownloadStep.VERSION_LIBRARIES, null))
            if (mcVersion.libraries == null) {
                throw ComponentCreationException("Unable to add libraries: libraries is null")
            }
            librariesDir?.let { librariesDir ->
                if (!librariesDir.isDirectory()) {
                    try {
                        librariesDir.createDir()
                    } catch (e: IOException) {
                        throw ComponentCreationException(
                            "Unable to add libraries: failed to create libraries directory: path=$librariesDir",
                            e
                        )
                    }
                }
                val result: List<String> = try {
                    MinecraftGame.downloadVersionLibraries(
                        mcVersion.libraries,
                        librariesDir,
                        listOf<String>()
                    ) { status: DownloadStatus? ->
                        setStatus(
                            CreationStatus(
                                CreationStatus.DownloadStep.VERSION_LIBRARIES,
                                status
                            )
                        )
                    }
                } catch (e: FileDownloadException) {
                    throw ComponentCreationException("Unable to add libraries: failed to download libraries", e)
                }
                details.libraries = result
                LOGGER.debug { "${"Added libraries: {}"} $result" }
            }?: {
                throw ComponentCreationException("Unable to add libraries: librariesDir is null")
            }
        }?: {
            throw ComponentCreationException("Unable to add libraries: invalid data")
        }
    }

    @Throws(ComponentCreationException::class)
    private fun addFile(details: LauncherVersionDetails) {
        newManifest?.let { newManifest ->
            val baseDir = File(newManifest.directory)
            if (!baseDir.isDirectory()) {
                throw ComponentCreationException("Unable to add file: base dir is not a directory: dir=${newManifest.directory}")
            }
            mcVersion?.let { mcVersion ->
                try {
                    MinecraftGame.downloadVersionDownload(mcVersion.downloads.client, baseDir)
                } catch (e: FileDownloadException) {
                    throw ComponentCreationException("Unable to add file: Failed to download client: url=${mcVersion.downloads.client.url}", e)
                }
                val urlParts: Array<String> = mcVersion.downloads.client.url.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                details.mainFile = urlParts[urlParts.size - 1]
                LOGGER.debug { "${"Added file: mainFile={}"} ${details.mainFile}" }
            }?: {
                throw ComponentCreationException("Unable to add file: mcVersion is null")
            }
        }?: {
            throw ComponentCreationException("Unable to add file: newManifest is null")
        }
    }

    @Throws(ComponentCreationException::class)
    private fun addArguments(details: LauncherVersionDetails) {
        mcVersion?.let { mcVersion ->
            details.gameArguments = translateArguments(
                mcVersion.launchArguments.game,
                appConfig().minecraftDefaultGameArguments
            )
            details.jvmArguments = translateArguments(
                mcVersion.launchArguments.jvm,
                appConfig().minecraftDefaultJvmArguments
            )
            LOGGER.debug { "Added arguments" }
        }?: {
            throw ComponentCreationException("Unable to add arguments: mcVersion is null")
        }
    }

    @Throws(ComponentCreationException::class)
    private fun translateArguments(
        args: List<MinecraftLaunchArgument>,
        defaultArgs: Array<LauncherLaunchArgument>
    ): List<LauncherLaunchArgument> {
        val result: MutableList<LauncherLaunchArgument> = mutableListOf()
        for (a in args) {
            var feature: String? = null
            var osName: String? = null
            var osVersion: String? = null
            var osArch: String? = null
            if (a.isGated) {
                for (r in a.rules) {
                    r.features?.let { features ->
                        if (features.isHasCustomResolution) {
                            feature = "resolution_x"
                        } else if (features.isDemoUser) {
                            feature = "is_demo_user"
                        }
                    }
                    r.os?.let { os ->
                        osName = os.name
                        osVersion = os.version
                        osArch = os.arch
                    }
                }
            }
            result.add(LauncherLaunchArgument(a.name, feature, osName, osVersion, osArch))
        }
        result.addAll(defaultArgs)
        LOGGER.debug { "Translated arguments: $result" }
        return result
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}
