package net.treset.treelauncher.backend.creation

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mc_version_loader.assets.AssetIndex
import net.treset.mc_version_loader.assets.MinecraftAssets
import net.treset.mc_version_loader.exception.FileDownloadException
import net.treset.mc_version_loader.minecraft.MinecraftGame
import net.treset.mc_version_loader.minecraft.MinecraftVersionDetails
import net.treset.mc_version_loader.util.DownloadStatus
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.data.LauncherFiles
import net.treset.treelauncher.backend.data.LauncherVersionDetails
import net.treset.treelauncher.backend.data.manifest.LauncherManifestType
import net.treset.treelauncher.backend.data.manifest.ParentManifest
import net.treset.treelauncher.backend.util.CreationStatus
import net.treset.treelauncher.backend.util.exception.ComponentCreationException
import net.treset.treelauncher.backend.util.file.LauncherFile
import java.io.File
import java.io.IOException

class VanillaVersionCreator(
    typeConversion: Map<String, LauncherManifestType>,
    componentsManifest: ParentManifest,
    var mcVersion: MinecraftVersionDetails,
    files: LauncherFiles,
    var librariesDir: LauncherFile
) : VersionCreator(
    mcVersion.id,
    typeConversion,
    componentsManifest,
    files
) {
    init {
        defaultStatus = CreationStatus(CreationStatus.DownloadStep.VERSION, null)
    }

    override fun matchesVersion(id: String): Boolean {
        return id == mcVersion.id
    }

    @Throws(ComponentCreationException::class)
    override fun makeVersion() {
        setStatus(CreationStatus(CreationStatus.DownloadStep.VERSION_VANILLA, null))
        LOGGER.debug { "Creating minecraft version..." }
        val details = LauncherVersionDetails(
            mcVersion.id,
            "vanilla",
            null,
            mcVersion.assets,
            null,
            null,
            null,
            listOf(),
            listOf(),
            "",
            listOf(),
            mcVersion.mainClass,
            null,
            mcVersion.id
        )
        try {
            createAssets(details)
            addArguments(details)
            addJava(details)
            addLibraries(details)
            addClient(details)
        } catch (e: ComponentCreationException) {
            attemptCleanup()
            throw ComponentCreationException("Unable to create minecraft version", e)
        }
        newManifest?.let {newManifest ->
            try {
                LauncherFile.of(newManifest.directory, newManifest.details!!).write(details)
            } catch (e: IOException) {
                attemptCleanup()
                throw ComponentCreationException("Unable to write version details to file", e)
            }
            LOGGER.debug { "${"Created minecraft version: id={}"} ${newManifest.id}" }
        }?: run{
            attemptCleanup()
            throw ComponentCreationException("Unable to create minecraft version: invalid data")
        }
    }

    @Throws(ComponentCreationException::class)
    private fun createAssets(details: LauncherVersionDetails) {
        LOGGER.debug { "Downloading assets..." }
        setStatus(CreationStatus(CreationStatus.DownloadStep.VERSION_ASSETS, null))
        val assetIndexUrl: String = mcVersion.assetIndex.url
        val index: AssetIndex = try {
            MinecraftAssets.getAssetIndex(assetIndexUrl)
        } catch (e: FileDownloadException) {
            throw ComponentCreationException("Unable to create assets: failed to download asset index", e)
        }
        if (index.objects == null || index.objects.isEmpty()) {
            throw ComponentCreationException("Unable to create assets: invalid index contents")
        }
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
            throw ComponentCreationException("Unable to create assets: failed to download assets", e)
        }
        if(index.isMapToResources) {
            val virtualDir = try {
                MinecraftAssets.createVirtualAssets(mcVersion.assets, baseDir)
            } catch (e: IOException) {
                throw ComponentCreationException("Unable to create assets: failed to extract virtual assets", e)
            }

            details.virtualAssets = virtualDir.relativeTo(baseDir).path
        }

        LOGGER.debug { "Downloaded assets" }
    }

    @Throws(ComponentCreationException::class)
    private fun addArguments(details: LauncherVersionDetails) {
        details.gameArguments = translateArguments(
            mcVersion.launchArguments.game,
            appConfig().minecraftDefaultGameArguments
        )
        details.jvmArguments = translateArguments(
            mcVersion.launchArguments.jvm,
            appConfig().minecraftDefaultJvmArguments
        )
        LOGGER.debug { "Added arguments" }
    }

    @Throws(ComponentCreationException::class)
    private fun addJava(details: LauncherVersionDetails) {
        LOGGER.debug { "Adding java component..." }
        val javaName: String = mcVersion.javaVersion.getComponent() ?: throw ComponentCreationException("Unable to add java component: java name is null")
        for (j in files.javaComponents) {
            if (javaName == j.name) {
                details.java = j.id
                LOGGER.debug { "Using existing java component: id=§{j.id}" }
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
        LOGGER.debug { "Added java component: id=${details.java}" }
    }

    @Throws(ComponentCreationException::class)
    private fun addLibraries(details: LauncherVersionDetails) {
        setStatus(CreationStatus(CreationStatus.DownloadStep.VERSION_LIBRARIES, null))
        LOGGER.debug { "Adding libraries..." }
        if (mcVersion.libraries == null) {
            throw ComponentCreationException("Unable to add libraries: libraries is null")
        }
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

        val nativesDir = LauncherFile.of(newManifest!!.directory, appConfig().nativesDirName)
        val result: List<String> = try {
            MinecraftGame.downloadVersionLibraries(
                mcVersion.libraries,
                librariesDir,
                listOf<String>(),
                nativesDir
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
        if(nativesDir.isDirectory()) {
            details.natives = appConfig().nativesDirName
        }
        LOGGER.debug { "Added libraries: $result" }
    }

    @Throws(ComponentCreationException::class)
    private fun addClient(details: LauncherVersionDetails) {
        setStatus(CreationStatus(CreationStatus.DownloadStep.VERSION_FILE, null))
        LOGGER.debug { "Adding client..." }
        newManifest?.let { newManifest ->
            val baseDir = File(newManifest.directory)
            if (!baseDir.isDirectory()) {
                throw ComponentCreationException("Unable to add client: base dir is not a directory: dir=${newManifest.directory}")
            }
            try {
                MinecraftGame.downloadVersionDownload(mcVersion.downloads.client, File(baseDir, appConfig().minecraftDefaultFileName))
            } catch (e: FileDownloadException) {
                throw ComponentCreationException("Unable to add client: Failed to download client: url=${mcVersion.downloads.client.url}", e)
            }
            val urlParts: Array<String> = mcVersion.downloads.client.url.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            details.mainFile = urlParts[urlParts.size - 1]
            LOGGER.debug { "Added client: mainFile=${details.mainFile}" }
        }?: throw ComponentCreationException("Unable to add client: newManifest is null")
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}