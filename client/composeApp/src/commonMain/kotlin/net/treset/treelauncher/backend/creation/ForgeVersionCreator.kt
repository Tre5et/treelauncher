package net.treset.treelauncher.backend.creation

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mcdl.exception.FileDownloadException
import net.treset.mcdl.forge.ForgeInstaller
import net.treset.mcdl.minecraft.MinecraftVersion
import net.treset.mcdl.minecraft.MinecraftVersionDetails
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.data.LauncherFiles
import net.treset.treelauncher.backend.data.manifest.ParentManifest
import net.treset.treelauncher.backend.data.manifest.VersionComponent
import net.treset.treelauncher.backend.util.FormatStringProvider
import net.treset.treelauncher.backend.util.Status
import net.treset.treelauncher.backend.util.file.LauncherFile
import java.io.IOException

class ForgeVersionCreator(
    parent: ParentManifest,
    onStatus: (Status) -> Unit
) : VersionCreator<ForgeCreationData>(parent, onStatus) {
    @Throws(IOException::class)
    override fun createNew(data: ForgeCreationData, statusProvider: CreationProvider): VersionComponent {
        LOGGER.debug { "Creating new forge version: id=${data.installer.version}..." }
        statusProvider.next("Creating parent version") // TODO: make localized

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

        val creator = VanillaVersionCreator(parent, onStatus)
        val mc = try {
            creator.new(VanillaCreationData(versionDetails, data.librariesDir, data.assetsDir, data.files))
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
    private fun addLibraries(data: ForgeCreationData, version: VersionComponent, statusProvider: CreationProvider) {
        LOGGER.debug { "Adding forge libraries..." }
        val libraryProvider = statusProvider.subStep(VERSION_FORGE_LIBRARIES, 3)
        libraryProvider.next("Downloading forge libraries") // TODO: make localized
        if (!data.librariesDir.isDirectory()) {
            try {
                data.librariesDir.createDir()
            } catch (e: IOException) {
                throw IOException("Unable to add fabric libraries: failed to create libraries directory: path=${data.librariesDir}", e)
            }
        }

        val libs = try {
            data.installer.downloadLibraries(
                data.librariesDir,
                LauncherFile.of(version.directory, appConfig().nativesDirName)
            ) {
                LOGGER.debug { "Downloading forge library: ${it.currentFile}" }
                libraryProvider.download(it, 1, 1)
            }
        } catch (e: FileDownloadException) {
            throw IOException("Unable to add forge libraries: failed to download libraries", e)
        }
        version.libraries = libs
        libraryProvider.finish("Done") // TODO: make localized
        LOGGER.debug { "Added forge libraries" }
    }

    @Throws(IOException::class)
    private fun createClient(data: ForgeCreationData, version: VersionComponent, vanillaVersion: VersionComponent, statusProvider: CreationProvider) {
        LOGGER.debug { "Creating forge client..." }
        val clientProvider = statusProvider.subStep(VERSION_FORGE_FILE, 3)
        clientProvider.next("Creating forge client") // TODO: make localized

        if (!version.directory.isDirectory()) {
            throw IOException("Unable to add forge main file: base dir is not a directory")
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
        clientProvider.finish("Done") // TODO: make localized
        LOGGER.debug { "Created forge client" }
    }

    override val step = VERSION_FORGE
    override val newTotal = 1

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}

class ForgeCreationData(
    val installer: ForgeInstaller,
    val librariesDir: LauncherFile,
    val assetsDir: LauncherFile,
    val files: LauncherFiles
): VersionCreationData(installer.version, installer.version, files.versionComponents)

val VERSION_FORGE = FormatStringProvider { net.treset.treelauncher.localization.strings().creator.status.version.forge() }
val VERSION_FORGE_LIBRARIES = FormatStringProvider { net.treset.treelauncher.localization.strings().creator.status.version.forgeLibraries() }
val VERSION_FORGE_FILE = FormatStringProvider { net.treset.treelauncher.localization.strings().creator.status.version.forgeFile() }