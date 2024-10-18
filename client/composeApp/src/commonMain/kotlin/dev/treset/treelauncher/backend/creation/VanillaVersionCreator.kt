package dev.treset.treelauncher.backend.creation

import dev.treset.mcdl.assets.AssetIndex
import dev.treset.mcdl.exception.FileDownloadException
import dev.treset.mcdl.minecraft.MinecraftLibrary
import dev.treset.mcdl.minecraft.MinecraftVersionDetails
import dev.treset.treelauncher.backend.config.appConfig
import dev.treset.treelauncher.backend.data.LauncherFiles
import dev.treset.treelauncher.backend.data.manifest.VersionComponent
import dev.treset.treelauncher.backend.util.FormatStringProvider
import dev.treset.treelauncher.backend.util.Status
import dev.treset.treelauncher.backend.util.StatusProvider
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.localization.Strings
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.IOException

class VanillaVersionCreator(
    data: VanillaCreationData,
    statusProvider: StatusProvider
) : VersionCreator<VanillaCreationData>(data, statusProvider) {
    constructor(
        data: VanillaCreationData,
        onStatus: (Status) -> Unit
    ) : this(data, StatusProvider(null, 0, onStatus))

    @Throws(IOException::class)
    override fun createNew(statusProvider: StatusProvider): VersionComponent {
        LOGGER.debug { "Creating new vanilla version: id=${data.versionId}..." }
        statusProvider.next(Strings.creator.status.message.vanillaVersion())
        val version = VersionComponent(
            id = id,
            name = data.name,
            versionNumber = data.version.id,
            versionType = "vanilla",
            loaderVersion = null,
            assets = data.version.assets,
            virtualAssets = null,
            natives = null,
            depends = null,
            gameArguments = translateArguments(
                data.version.launchArguments.game,
                appConfig().minecraftDefaultGameArguments
            ),
            jvmArguments = translateArguments(
                data.version.launchArguments.jvm,
                appConfig().minecraftDefaultJvmArguments
            ),
            java = null,
            libraries = listOf(),
            mainClass = data.version.mainClass,
            mainFile = null,
            versionId = data.version.id,
            file = file,
        )

        try {
            createAssets(data, version, statusProvider)
            addJava(data, version)
            addLibraries(data, version, statusProvider)
            addClient(data, version, statusProvider)
        } catch (e: IOException) {
            throw IOException("Unable to create vanilla version", e)
        }

        return version
    }

    override val step = VERSION_VANILLA
    override val total = 1

    @Throws(IOException::class)
    private fun createAssets(data: VanillaCreationData, version: VersionComponent, statusProvider: StatusProvider) {
        LOGGER.debug { "Downloading assets..." }
        val assetsProvider = statusProvider.subStep(VERSION_ASSETS, 3)
        assetsProvider.next()
        val assetIndexUrl = data.version.assetIndex.url
        val index = try {
            AssetIndex.get(assetIndexUrl)
        } catch (e: FileDownloadException) {
            throw IOException("Unable to create assets: failed to download asset index", e)
        }
        if (index.objects == null || index.objects.isEmpty()) {
            throw IOException("Unable to create assets: invalid index contents")
        }
        try {
            index.downloadAll(
                data.files.assetsDir,
                false
            ) {
                assetsProvider.download(it, 1, 2)
            }
        } catch (e: FileDownloadException) {
            throw IOException("Unable to create assets: failed to download assets", e)
        }
        if(index.isMapToResources) {
            assetsProvider.next()
            val virtualDir = try {
                index.resolveAll(data.files.assetsDir)
            } catch (e: IOException) {
                throw IOException("Unable to create assets: failed to extract virtual assets", e)
            }

            version.virtualAssets = virtualDir.relativeTo(data.files.assetsDir).path
        }

        assetsProvider.finish()
        LOGGER.debug { "Downloaded assets" }
    }

    @Throws(IOException::class)
    private fun addJava(data: VanillaCreationData, version: VersionComponent) {
        LOGGER.debug { "Adding java component..." }
        val javaName: String = data.version.javaVersion.getComponent()

        val javaCreator = JavaComponentCreator(
            JavaCreationData(javaName, data.files.javaComponents, data.files.javaManifest),
            statusProvider
        )

        version.java = javaCreator.create().id
        LOGGER.debug { "Added java component: id=${version.java}" }
    }

    @Throws(IOException::class)
    private fun addLibraries(data: VanillaCreationData, version: VersionComponent, statusProvider: StatusProvider) {
        LOGGER.debug { "Adding libraries..." }
        val librariesProvider = statusProvider.subStep(VERSION_LIBRARIES, 2)
        librariesProvider.next()

        if (!data.files.librariesDir.isDirectory()) {
            try {
                data.files.librariesDir.createDir()
            } catch (e: IOException) {
                throw IOException("Unable to add libraries: failed to create libraries directory: path=${data.files.librariesDir}", e)
            }
        }

        val nativesDir = LauncherFile.of(directory, appConfig().nativesDirName)
        val result: List<String> = try {
            MinecraftLibrary.downloadAll(
                data.version.libraries,
                data.files.librariesDir,
                null,
                listOf<String>(),
                nativesDir
            ) {
                librariesProvider.download(it, 1, 1)
            }
        } catch (e: FileDownloadException) {
            throw IOException("Unable to add libraries: failed to download libraries", e)
        }
        version.libraries = result
        if(nativesDir.isDirectory()) {
            version.natives = appConfig().nativesDirName
        }
        librariesProvider.finish()
        LOGGER.debug { "Added libraries: $result" }
    }

    @Throws(IOException::class)
    private fun addClient(data: VanillaCreationData, version: VersionComponent, statusProvider: StatusProvider) {
        LOGGER.debug { "Adding client..." }
        val clientProvider = statusProvider.subStep(VERSION_FILE, 2)
        clientProvider.next()

        val baseDir = directory
        if (!baseDir.isDirectory()) {
            try {
                baseDir.createDir()
            } catch (e: IOException) {
                throw IOException("Unable to add client: base dir is not a directory: dir=${baseDir}")
            }
        }
        try {
            data.version.downloads.client.download(
                LauncherFile.of(baseDir, appConfig().minecraftDefaultFileName)
            )
        } catch (e: FileDownloadException) {
            throw IOException("Unable to add client: failed to download client: url=${data.version.downloads.client.url}", e)
        }
        val urlParts = data.version.downloads.client.url.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        version.mainFile = urlParts[urlParts.size - 1]
        clientProvider.finish()
        LOGGER.debug { "Added client: mainFile=${version.mainFile}" }
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}

class VanillaCreationData(
    val version: MinecraftVersionDetails,
    files: LauncherFiles
): VersionCreationData(version.id, version.id, files)

val VERSION_VANILLA = FormatStringProvider { Strings.creator.status.version.vanilla() }
val VERSION_ASSETS = FormatStringProvider { dev.treset.treelauncher.localization.Strings.creator.status.version.assets() }
val VERSION_LIBRARIES = FormatStringProvider { dev.treset.treelauncher.localization.Strings.creator.status.version.libraries() }
val VERSION_FILE = FormatStringProvider { dev.treset.treelauncher.localization.Strings.creator.status.version.file() }