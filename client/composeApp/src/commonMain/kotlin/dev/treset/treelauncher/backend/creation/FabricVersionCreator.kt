package dev.treset.treelauncher.backend.creation

import dev.treset.mcdl.exception.FileDownloadException
import dev.treset.mcdl.fabric.FabricLibrary
import dev.treset.mcdl.fabric.FabricProfile
import dev.treset.mcdl.fabric.FabricVersion
import dev.treset.mcdl.minecraft.MinecraftProfile
import dev.treset.mcdl.minecraft.MinecraftVersion
import dev.treset.treelauncher.backend.config.appConfig
import dev.treset.treelauncher.backend.data.LauncherFiles
import dev.treset.treelauncher.backend.data.manifest.VersionComponent
import dev.treset.treelauncher.backend.util.FormatStringProvider
import dev.treset.treelauncher.backend.util.Status
import dev.treset.treelauncher.backend.util.StatusProvider
import dev.treset.treelauncher.backend.util.assignFrom
import dev.treset.treelauncher.backend.util.string.PatternString
import dev.treset.treelauncher.localization.Strings
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.io.IOException

class FabricVersionCreator(
    data: FabricCreationData,
    statusProvider: StatusProvider
) : VersionCreator<FabricCreationData>(data, statusProvider) {
    constructor(
        data: FabricCreationData,
        onStatus: (Status) -> Unit
    ) : this(data, StatusProvider(null, 0, onStatus))

    @Throws(IOException::class)
    override fun createNew(statusProvider: StatusProvider): VersionComponent {
        LOGGER.debug { "Creating new fabric version: id=${data.profile.id}..." }
        statusProvider.next(Strings.creator.status.message.vanillaVersion())

        if(data.profile.inheritsFrom == null) {
            throw IOException("Unable to create fabric version: no valid fabric profile")
        }

        LOGGER.debug { "Creating minecraft version..." }
        val inheritVersion = MinecraftVersion.get(data.profile.inheritsFrom)?: throw IOException("Unable to create fabric version: failed to find mc version: versionId=${data.profile.inheritsFrom}")
        val versionDetails = try {
            MinecraftProfile.get(inheritVersion.url)
        } catch (e: FileDownloadException) {
            throw IOException("Unable to create fabric version: failed to download mc version details: versionId=${data.profile.inheritsFrom}", e)
        }

        val creator = VanillaVersionCreator(
            VanillaCreationData(versionDetails, data.files),
            statusProvider
        )
        val mc = try {
            creator.create()
        } catch (e: IOException) {
            throw IOException("Unable to create fabric version: failed to create mc version: versionId=${data.profile.inheritsFrom}", e)
        }

        LOGGER.debug { "Created minecraft version: id=${mc.id.value}" }

        val version = VersionComponent(
            id = id,
            name = data.name,
            versionNumber = data.profile.inheritsFrom,
            versionType = "fabric",
            loaderVersion = data.version.loader.version,
            assets = null,
            virtualAssets = null,
            natives = null,
            depends = mc.id.value,
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
            versionId = data.versionId,
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

    @Throws(IOException::class)
    private fun addLibraries(data: FabricCreationData, version: VersionComponent, statusProvider: StatusProvider) {
        LOGGER.debug { "Adding fabric libraries..." }
        val libraryProvider = statusProvider.subStep(CreationStep.VERSION_FABRIC_LIBRARIES, 3)
        libraryProvider.next()
        if (!data.files.librariesDir.isDirectory()) {
            try {
                data.files.librariesDir.createDir()
            } catch (e: IOException) {
                throw IOException("Unable to add fabric libraries: failed to create libraries directory: path=${data.files.librariesDir}", e)
            }
        }
        val loaderPattern = PatternString(":fabric-loader:", true)
        val clientLibs = data.profile.libraries.filter { !loaderPattern.matches(it.name) }
        val libs: List<String> = try {
            FabricLibrary.downloadAll(
                clientLibs,
                data.files.librariesDir
            ) {
                libraryProvider.download(it, 1, 1)
            }
        } catch (e: FileDownloadException) {
            throw IOException("Unable to add fabric libraries: failed to download libraries", e)
        }
        version.libraries.assignFrom(libs)
        libraryProvider.finish()
        LOGGER.debug { "Added fabric libraries" }
    }

    @Throws(IOException::class)
    private fun addClient(data: FabricCreationData, version: VersionComponent, statusProvider: StatusProvider) {
        LOGGER.debug { "Adding fabric client..." }
        val clientProvider = statusProvider.subStep(CreationStep.VERSION_FABRIC_FILE, 3)
        clientProvider.next()
        if (!directory.isDirectory()) {
            try {
                directory.createDir()
            } catch (e: IOException) {
                throw IOException("Unable to add fabric client: base dir is not a directory")
            }
        }
        try {
            data.version.downloadClient(File(directory, appConfig().fabricDefaultClientFileName))
        } catch (e: FileDownloadException) {
            throw IOException("Unable to add fabric client: failed to download fabric loader", e)
        }
        version.mainFile.value = appConfig().fabricDefaultClientFileName
        LOGGER.debug { "Added fabric client: mainFile=${version.mainFile}" }
    }

    override val step = CreationStep.VERSION_FABRIC
    override val total = 1

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}

class FabricCreationData(
    val version: FabricVersion,
    val profile: FabricProfile,
    files: LauncherFiles
): VersionCreationData(profile.id, profile.id, files)

val CreationStep.VERSION_FABRIC: FormatStringProvider
    get() = FormatStringProvider { Strings.creator.status.version.fabric() }
val CreationStep.VERSION_FABRIC_LIBRARIES: FormatStringProvider
    get() = FormatStringProvider { Strings.creator.status.version.fabricLibraries() }
val CreationStep.VERSION_FABRIC_FILE: FormatStringProvider
    get() = FormatStringProvider { Strings.creator.status.version.fabricFile() }