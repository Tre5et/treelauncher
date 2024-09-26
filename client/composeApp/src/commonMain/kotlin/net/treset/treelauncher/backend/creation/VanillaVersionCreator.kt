package net.treset.treelauncher.backend.creation

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mcdl.assets.AssetIndex
import net.treset.mcdl.exception.FileDownloadException
import net.treset.mcdl.minecraft.MinecraftLibrary
import net.treset.mcdl.minecraft.MinecraftVersionDetails
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.data.LauncherFiles
import net.treset.treelauncher.backend.data.manifest.ParentManifest
import net.treset.treelauncher.backend.data.manifest.VersionComponent
import net.treset.treelauncher.backend.util.FormatStringProvider
import net.treset.treelauncher.backend.util.Status
import net.treset.treelauncher.backend.util.file.LauncherFile
import java.io.IOException

class VanillaVersionCreator(
    parent: ParentManifest,
    onStatus: (Status) -> Unit
) : VersionCreator<VanillaCreationData>(parent, onStatus) {

    @Throws(IOException::class)
    override fun createNew(data: VanillaCreationData, statusProvider: CreationProvider): VersionComponent {
        LOGGER.debug { "Creating new vanilla version: id=${data.versionId}..." }
        statusProvider.next("Creating vanilla version") // TODO: make localized
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
    override val newTotal = 1

    @Throws(IOException::class)
    private fun createAssets(data: VanillaCreationData, version: VersionComponent, statusProvider: CreationProvider) {
        LOGGER.debug { "Downloading assets..." }
        val assetsProvider = statusProvider.subStep(VERSION_ASSETS, 3)
        assetsProvider.next("Downloading assets") // TODO: make localized
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
                data.assetsDir,
                false
            ) {
                assetsProvider.download(it, 1, 2)
            }
        } catch (e: FileDownloadException) {
            throw IOException("Unable to create assets: failed to download assets", e)
        }
        if(index.isMapToResources) {
            assetsProvider.next("Mapping virtual assets") // TODO: make localized
            val virtualDir = try {
                index.resolveAll(data.assetsDir)
            } catch (e: IOException) {
                throw IOException("Unable to create assets: failed to extract virtual assets", e)
            }

            version.virtualAssets = virtualDir.relativeTo(data.assetsDir).path
        }

        assetsProvider.finish("Done") // TODO: make localized
        LOGGER.debug { "Downloaded assets" }
    }

    @Throws(IOException::class)
    private fun addJava(data: VanillaCreationData, version: VersionComponent) {
        LOGGER.debug { "Adding java component..." }
        val javaName: String = data.version.javaVersion.getComponent()

        val javaCreator = JavaComponentCreator(data.files.javaManifest, onStatus)

        version.java = javaCreator.new(JavaCreationData(javaName, data.files.javaComponents))
        LOGGER.debug { "Added java component: id=${version.java}" }
    }

    @Throws(IOException::class)
    private fun addLibraries(data: VanillaCreationData, version: VersionComponent, statusProvider: CreationProvider) {
        LOGGER.debug { "Adding libraries..." }
        val librariesProvider = statusProvider.subStep(VERSION_LIBRARIES, 2)
        librariesProvider.next("Downloading libraries...") // TODO: make localized

        if (!data.librariesDir.isDirectory()) {
            try {
                data.librariesDir.createDir()
            } catch (e: IOException) {
                throw IOException("Unable to add libraries: failed to create libraries directory: path=${data.librariesDir}", e)
            }
        }

        val nativesDir = LauncherFile.of(directory, appConfig().nativesDirName)
        val result: List<String> = try {
            MinecraftLibrary.downloadAll(
                data.version.libraries,
                data.librariesDir,
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
        librariesProvider.finish("Done") // TODO: make localized
        LOGGER.debug { "Added libraries: $result" }
    }

    @Throws(IOException::class)
    private fun addClient(data: VanillaCreationData, version: VersionComponent, statusProvider: CreationProvider) {
        LOGGER.debug { "Adding client..." }
        val clientProvider = statusProvider.subStep(VERSION_FILE, 2)
        clientProvider.next("Downloading client...") // TODO: make localized

        val baseDir = directory
        if (!baseDir.isDirectory()) {
            throw IOException("Unable to add client: base dir is not a directory: dir=${baseDir}")
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
        clientProvider.finish("Done") // TODO: make localized
        LOGGER.debug { "Added client: mainFile=${version.mainFile}" }
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}

class VanillaCreationData(
    val version: MinecraftVersionDetails,
    val librariesDir: LauncherFile,
    val assetsDir: LauncherFile,
    val files: LauncherFiles
): VersionCreationData(version.id, version.id, files.versionComponents)

val VERSION_VANILLA = FormatStringProvider { net.treset.treelauncher.localization.strings().creator.status.version.vanilla() }
val VERSION_ASSETS = FormatStringProvider { net.treset.treelauncher.localization.strings().creator.status.version.assets() }
val VERSION_LIBRARIES = FormatStringProvider { net.treset.treelauncher.localization.strings().creator.status.version.libraries() }
val VERSION_FILE = FormatStringProvider { net.treset.treelauncher.localization.strings().creator.status.version.file() }