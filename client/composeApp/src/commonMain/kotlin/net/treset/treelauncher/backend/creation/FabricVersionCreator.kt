package net.treset.treelauncher.backend.creation

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mcdl.exception.FileDownloadException
import net.treset.mcdl.fabric.FabricLibrary
import net.treset.mcdl.fabric.FabricProfile
import net.treset.mcdl.fabric.FabricVersion
import net.treset.mcdl.minecraft.MinecraftVersion
import net.treset.mcdl.minecraft.MinecraftVersionDetails
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.data.LauncherFiles
import net.treset.treelauncher.backend.data.manifest.ParentManifest
import net.treset.treelauncher.backend.data.manifest.VersionComponent
import net.treset.treelauncher.backend.util.FormatStringProvider
import net.treset.treelauncher.backend.util.Status
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.backend.util.string.PatternString
import java.io.File
import java.io.IOException

class FabricVersionCreator(
    parent: ParentManifest,
    onStatus: (Status) -> Unit
) : VersionCreator<FabricCreationData>(parent, onStatus) {
    @Throws(IOException::class)
    override fun createNew(data: FabricCreationData, statusProvider: CreationProvider): VersionComponent {
        LOGGER.debug { "Creating new fabric version: id=${data.profile.id}..." }
        statusProvider.next("Creating parent version") // TODO: make localized

        if(data.profile.inheritsFrom == null) {
            throw IOException("Unable to create fabric version: no valid fabric profile")
        }

        LOGGER.debug { "Creating minecraft version..." }
        val versions = try {
            MinecraftVersion.getAll()
        } catch (e: FileDownloadException) {
            throw IOException("Unable to create fabric version: failed to get mc versions", e)
        }
        val inheritVersion = versions.firstOrNull { it.id == data.profile.inheritsFrom }?: throw IOException("Unable to create fabric version: failed to find mc version: versionId=${data.profile.inheritsFrom}")
        val versionDetails = try {
            MinecraftVersionDetails.get(inheritVersion.url)
        } catch (e: FileDownloadException) {
            throw IOException("Unable to create fabric version: failed to download mc version details: versionId=${data.profile.inheritsFrom}", e)
        }

        val creator = VanillaVersionCreator(parent, onStatus)
        val mcId = try {
            creator.new(VanillaCreationData(versionDetails, data.librariesDir, data.assetsDir, data.files))
        } catch (e: IOException) {
            throw IOException("Unable to create fabric version: failed to create mc version: versionId=${data.profile.inheritsFrom}", e)
        }

        val version = VersionComponent(
            id = id,
            name = data.name,
            versionNumber = data.profile.inheritsFrom,
            versionType = "fabric",
            loaderVersion = data.version.loader.version,
            assets = null,
            virtualAssets = null,
            natives = null,
            depends = mcId,
            gameArguments = translateArguments(
                data.profile.launchArguments.game,
                appConfig().fabricDefaultGameArguments
            ),
            jvmArguments = translateArguments(
                data.profile.launchArguments.jvm,
                appConfig().fabricDefaultJvmArguments
            ),
            java = null,
            libraries = listOf(),
            mainClass = data.profile.mainClass,
            mainFile = null,
            versionId = data.profile.id,
            file = file,
        )

        try {
            addLibraries(data, version, statusProvider)
            addClient(data, version, statusProvider)
        } catch (e: IOException) {
            throw IOException("Unable to create fabric version: versionId=${data.profile.id}", e)
        }

        return version
    }

    override val step = VERSION_FABRIC
    override val newTotal = 1

    @Throws(IOException::class)
    private fun addLibraries(data: FabricCreationData, version: VersionComponent, statusProvider: CreationProvider) {
        LOGGER.debug { "Adding fabric libraries..." }
        val libraryProvider = statusProvider.subStep(VERSION_FABRIC_LIBRARIES, 3)
        libraryProvider.next("Downloading fabric libraries") // TODO: make localized
        if (!data.librariesDir.isDirectory()) {
            try {
                data.librariesDir.createDir()
            } catch (e: IOException) {
                throw IOException("Unable to add fabric libraries: failed to create libraries directory: path=${data.librariesDir}", e)
            }
        }
        val loaderPattern = PatternString(":fabric-loader:", true)
        val clientLibs = data.profile.libraries.filter { !loaderPattern.matches(it.name) }
        val libs: List<String> = try {
            FabricLibrary.downloadAll(
                clientLibs,
                data.librariesDir
            ) {
                libraryProvider.download(it, 1, 1)
            }
        } catch (e: FileDownloadException) {
            throw IOException("Unable to add fabric libraries: failed to download libraries", e)
        }
        version.libraries = libs
        libraryProvider.finish("Done") // TODO: make localized
        LOGGER.debug { "Added fabric libraries" }
    }

    @Throws(IOException::class)
    private fun addClient(data: FabricCreationData, version: VersionComponent, statusProvider: CreationProvider) {
        LOGGER.debug { "Adding fabric client..." }
        val clientProvider = statusProvider.subStep(VERSION_FABRIC_FILE, 3)
        clientProvider.next("Downloading fabric client") // TODO: make localized
        if (!directory.isDirectory()) {
            throw IOException("Unable to add fabric client: base dir is not a directory")
        }
        try {
            data.version.downloadClient(File(directory, appConfig().fabricDefaultClientFileName))
        } catch (e: FileDownloadException) {
            throw IOException("Unable to add fabric client: failed to download fabric loader", e)
        }
        version.mainFile = appConfig().fabricDefaultClientFileName
        LOGGER.debug { "Added fabric client: mainFile=${version.mainFile}" }
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}

class FabricCreationData(
    val version: FabricVersion,
    val profile: FabricProfile,
    val librariesDir: LauncherFile,
    val assetsDir: LauncherFile,
    val files: LauncherFiles
): VersionCreationData(profile.id, version.loader.version, files.versionComponents)

val VERSION_FABRIC = FormatStringProvider { net.treset.treelauncher.localization.strings().creator.status.version.fabric() }
val VERSION_FABRIC_LIBRARIES = FormatStringProvider { net.treset.treelauncher.localization.strings().creator.status.version.fabricLibraries() }
val VERSION_FABRIC_FILE = FormatStringProvider { net.treset.treelauncher.localization.strings().creator.status.version.fabricFile() }