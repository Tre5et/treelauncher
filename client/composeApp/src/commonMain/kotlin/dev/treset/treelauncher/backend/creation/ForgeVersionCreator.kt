package dev.treset.treelauncher.backend.creation

import dev.treset.mcdl.exception.FileDownloadException
import dev.treset.mcdl.forge.ForgeInstaller
import dev.treset.mcdl.minecraft.MinecraftVersion
import dev.treset.mcdl.minecraft.MinecraftVersionDetails
import dev.treset.treelauncher.backend.config.appConfig
import dev.treset.treelauncher.backend.data.LauncherFiles
import dev.treset.treelauncher.backend.data.manifest.VersionComponent
import dev.treset.treelauncher.backend.util.FormatStringProvider
import dev.treset.treelauncher.backend.util.Status
import dev.treset.treelauncher.backend.util.StatusProvider
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.localization.strings
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.IOException

class ForgeVersionCreator(
    data: ForgeCreationData,
    statusProvider: StatusProvider
) : VersionCreator<ForgeCreationData>(data, statusProvider) {
    constructor(
        data: ForgeCreationData,
        onStatus: (Status) -> Unit
    ) : this(data, StatusProvider(null, 0, onStatus))

    @Throws(IOException::class)
    override fun createNew(statusProvider: StatusProvider): VersionComponent {
        LOGGER.debug { "Creating new forge version: id=${data.installer.version}..." }
        statusProvider.next(strings().creator.status.message.vanillaVersion())

        if(data.installer.installData.inheritsFrom == null) {
            throw IOException("Unable to create forge version: no valid forge version")
        }

        LOGGER.debug { "Creating minecraft version..." }

        val inheritVersion = MinecraftVersion.get(data.installer.installData.inheritsFrom)?: throw IOException("Unable to create forge version: failed to find mc version: versionId=${data.installer.installData.inheritsFrom}")
        val versionDetails = try {
            MinecraftVersionDetails.get(inheritVersion.url)
        } catch (e: FileDownloadException) {
            throw IOException("Unable to create fabric version: failed to download mc version details: versionId=${data.installer.installData.inheritsFrom}", e)
        }

        val creator = VanillaVersionCreator(
            VanillaCreationData(versionDetails, data.files),
            statusProvider
        )
        val mc = try {
            creator.create()
        } catch (e: IOException) {
            throw IOException("Unable to create forge version: failed to create mc version: versionId=${data.installer.installData.inheritsFrom}", e)
        }

        LOGGER.debug { "Created minecraft version: id=${mc.id}" }

        val version = VersionComponent(
            id = id,
            name = data.name,
            versionNumber = data.installer.installData.inheritsFrom,
            versionType = "forge",
            loaderVersion = data.installer.installData.id,
            assets = null,
            virtualAssets = null,
            natives = null,
            depends = mc.id,
            gameArguments = translateArguments(
                data.installer.installData.arguments.game,
                appConfig().forgeDefaultGameArguments
            ).map {
                // Hack because forge uses ${version_name} in a questionable way in some versions
                if(it.argument.contains("\${version_name}")) {
                    it.argument = it.argument.replace("\${version_name}", appConfig().minecraftDefaultFileName.let { it.substring(0, it.length - 4) })
                }
                it
            },
            jvmArguments = translateArguments(
                data.installer.installData.arguments.jvm,
                appConfig().forgeDefaultJvmArguments
            ),
            java = null,
            libraries = listOf(),
            mainClass = data.installer.installData.mainClass,
            mainFile = null,
            versionId = data.versionId,
            file = file,
        )

        try {
            addLibraries(data, version, statusProvider)
            createClient(data, version, mc, statusProvider)
        } catch (e: IOException) {
            throw IOException("Unable to create forge version: versionId=${data.installer.installData.inheritsFrom}", e)
        }

        LOGGER.debug { "Created forge version: id=${version.id}" }
        return version
    }

    @Throws(IOException::class)
    private fun addLibraries(data: ForgeCreationData, version: VersionComponent, statusProvider: StatusProvider) {
        LOGGER.debug { "Adding forge libraries..." }
        val libraryProvider = statusProvider.subStep(VERSION_FORGE_LIBRARIES, 3)
        libraryProvider.next()
        if (!data.files.librariesDir.isDirectory()) {
            try {
                data.files.librariesDir.createDir()
            } catch (e: IOException) {
                throw IOException("Unable to add fabric libraries: failed to create libraries directory: path=${data.files.librariesDir}", e)
            }
        }

        val libs = try {
            data.installer.downloadLibraries(
                data.files.librariesDir,
                LauncherFile.of(version.directory, appConfig().nativesDirName)
            ) {
                LOGGER.debug { "Downloading forge library: ${it.currentFile}" }
                libraryProvider.download(it, 1, 1)
            }
        } catch (e: FileDownloadException) {
            throw IOException("Unable to add forge libraries: failed to download libraries", e)
        }
        version.libraries = libs
        libraryProvider.finish()
        LOGGER.debug { "Added forge libraries" }
    }

    @Throws(IOException::class)
    private fun createClient(data: ForgeCreationData, version: VersionComponent, vanillaVersion: VersionComponent, statusProvider: StatusProvider) {
        LOGGER.debug { "Creating forge client..." }
        val clientProvider = statusProvider.subStep(VERSION_FORGE_FILE, 3)
        clientProvider.next()

        if (!version.directory.isDirectory()) {
            try {
                version.directory.createDir()
            } catch (e: IOException) {
                throw IOException("Unable to add forge main file: failed to create base dir: dir=${version.directory}", e)
            }
        }

        LOGGER.debug { "Finding minecraft jar..." }
        val minecraftBaseDir = vanillaVersion.directory
        val minecraftFileName = vanillaVersion.mainFile ?: appConfig().minecraftDefaultFileName
        val minecraftFile = LauncherFile.of(minecraftBaseDir, minecraftFileName)
        if(!minecraftFile.isFile) {
            throw IOException("Unable to create forge version: failed to find mc version client file: versionId=${data.installer.installData.inheritsFrom}")
        }
        LOGGER.debug { "Found minecraft jar" }

        LOGGER.debug { "Finding minecraft java..." }
        val javaFile = LauncherFile.ofData(data.files.mainManifest.javasDir, data.files.javaManifest.prefix + "_" + vanillaVersion.java, "bin", "java")
        LOGGER.debug { "Found minecraft java" }

        LOGGER.debug { "Patching forge client..."}
        try {
            data.installer.createClient(
                LauncherFile.ofData(data.files.mainManifest.librariesDir),
                minecraftFile,
                javaFile
            ) {
                LOGGER.debug { "Patching forge client: ${it.currentFile}" }
                clientProvider.download(it, 1, 1)
            }
        } catch (e: FileDownloadException) {
            throw IOException("Unable to create forge version: failed to create forge client", e)
        }
        clientProvider.finish()
        LOGGER.debug { "Created forge client" }
    }

    override val step = VERSION_FORGE
    override val total = 1

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}

class ForgeCreationData(
    val installer: ForgeInstaller,
    files: LauncherFiles
): VersionCreationData(installer.installData.id, installer.version, files)

val VERSION_FORGE = FormatStringProvider { strings().creator.status.version.forge() }
val VERSION_FORGE_LIBRARIES = FormatStringProvider { strings().creator.status.version.forgeLibraries() }
val VERSION_FORGE_FILE = FormatStringProvider { strings().creator.status.version.forgeFile() }