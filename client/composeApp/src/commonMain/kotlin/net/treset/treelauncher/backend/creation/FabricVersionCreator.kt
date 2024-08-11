package net.treset.treelauncher.backend.creation

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mc_version_loader.exception.FileDownloadException
import net.treset.mc_version_loader.fabric.FabricLibrary
import net.treset.mc_version_loader.fabric.FabricLoader
import net.treset.mc_version_loader.fabric.FabricProfile
import net.treset.mc_version_loader.fabric.FabricVersionDetails
import net.treset.mc_version_loader.json.SerializationException
import net.treset.mc_version_loader.minecraft.MinecraftGame
import net.treset.mc_version_loader.minecraft.MinecraftVersion
import net.treset.mc_version_loader.minecraft.MinecraftVersionDetails
import net.treset.mc_version_loader.util.FileUtil
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.data.LauncherFiles
import net.treset.treelauncher.backend.data.LauncherVersionDetails
import net.treset.treelauncher.backend.data.manifest.LauncherManifestType
import net.treset.treelauncher.backend.data.manifest.ParentManifest
import net.treset.treelauncher.backend.util.CreationStatus
import net.treset.treelauncher.backend.util.exception.ComponentCreationException
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.backend.util.string.PatternString
import java.io.File
import java.io.IOException

class FabricVersionCreator(
    typeConversion: Map<String, LauncherManifestType>,
    componentsManifest: ParentManifest,
    var fabricVersion: FabricVersionDetails,
    var fabricProfile: FabricProfile,
    files: LauncherFiles,
    var librariesDir: LauncherFile
) : VersionCreator(
    fabricProfile.id,
    typeConversion,
    componentsManifest,
    files
) {
    init {
        defaultStatus = CreationStatus(CreationStatus.DownloadStep.VERSION_FABRIC, null)
    }

    override fun matchesVersion(id: String): Boolean {
        return id == fabricProfile.id
    }

    @Throws(ComponentCreationException::class)
    override fun makeVersion() {
        setStatus(CreationStatus(CreationStatus.DownloadStep.VERSION_FABRIC, null))
        LOGGER.debug { "Creating fabric version..." }

        if (fabricProfile.inheritsFrom == null) {
            throw ComponentCreationException("Unable to create fabric version: no valid fabric profile")
        }

        LOGGER.debug { "Creating minecraft version..." }
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
                    VanillaVersionCreator(
                        typeConversion!!,
                        componentsManifest!!,
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
                LOGGER.debug { "Created minecraft version: id=$dependsId" }

                val details = LauncherVersionDetails(
                    fabricProfile.inheritsFrom,
                    "fabric",
                    fabricVersion.loader.version,
                    "",
                    null,
                    null,
                    dependsId,
                    listOf(),
                    listOf(),
                    "",
                    listOf(),
                    fabricProfile.mainClass,
                    "",
                    fabricProfile.id
                )
                try {
                    addArguments(details)
                    addLibraries(details)
                    addClient(details)
                } catch (e: ComponentCreationException) {
                    mcCreator.attemptCleanup()
                    attemptCleanup()
                    throw ComponentCreationException("Unable to create fabric version: versionId=${fabricProfile.inheritsFrom}", e)
                }
                try {
                    LauncherFile.of(newManifest!!.directory, newManifest!!.details!!).write(details)
                } catch (e: IOException) {
                    throw ComponentCreationException("Unable to create fabric version: failed to write version details: versionId=${fabricProfile.inheritsFrom}", e)
                }
                LOGGER.debug { "Created fabric version: id=${newManifest?.id}" }
                return
            }
        }
        throw ComponentCreationException("Unable to create fabric version: failed to find mc version: versionId=${fabricProfile.inheritsFrom}")
    }

    @Throws(ComponentCreationException::class)
    private fun addArguments(details: LauncherVersionDetails) {
        LOGGER.debug { "Adding fabric arguments..." }
        details.jvmArguments = translateArguments(
            fabricProfile.launchArguments.jvm,
            appConfig().fabricDefaultJvmArguments
        )
        details.gameArguments = translateArguments(
            fabricProfile.launchArguments.game,
            appConfig().fabricDefaultGameArguments
        )
        LOGGER.debug { "Added fabric arguments" }
    }

    @Throws(ComponentCreationException::class)
    private fun addLibraries(details: LauncherVersionDetails) {
        setStatus(CreationStatus(CreationStatus.DownloadStep.VERSION_FABRIC_LIBRARIES, null))
        LOGGER.debug { "Adding fabric libraries..." }
        fabricProfile.libraries?.let { libraries: List<FabricLibrary> ->
            if (!librariesDir.isDirectory()) {
                try {
                    librariesDir.createDir()
                } catch (e: IOException) {
                    throw ComponentCreationException("Unable to add fabric libraries: failed to create libraries directory: path=$librariesDir", e)
                }
            }
            val loaderPattern = PatternString(":fabric-loader:", true)
            val clientLibs = libraries.filter { !loaderPattern.matches(it.name) }
            val libs: List<String> = try {
                FabricLoader.downloadFabricLibraries(
                    librariesDir,
                    clientLibs
                ) {
                    setStatus(CreationStatus(CreationStatus.DownloadStep.VERSION_FABRIC_LIBRARIES, it))
                }
            } catch (e: FileDownloadException) {
                throw ComponentCreationException("Unable to add fabric libraries: failed to download libraries", e)
            }
            details.libraries = libs
            LOGGER.debug { "Added fabric libraries" }
        }?: throw ComponentCreationException("Unable to add fabric libraries: libraries invalid")
    }

    @Throws(ComponentCreationException::class)
    private fun addClient(details: LauncherVersionDetails) {
        setStatus(CreationStatus(CreationStatus.DownloadStep.VERSION_FABRIC_FILE, null))
        LOGGER.debug { "Adding fabric client..." }
        newManifest?.let { newManifest ->
            val baseDir = File(newManifest.directory)
            if (!baseDir.isDirectory()) {
                throw ComponentCreationException("Unable to add fabric client: base dir is not a directory")
            }
            try {
                FabricLoader.downloadFabricClient(File(baseDir, appConfig().fabricDefaultClientFileName), fabricVersion.loader)
            } catch (e: FileDownloadException) {
                throw ComponentCreationException("Unable to add fabric client: failed to download fabric loader", e)
            }
            details.mainFile = appConfig().fabricDefaultClientFileName
            LOGGER.debug { "Added fabric client: mainFile=${details.mainFile}" }
        }?: throw ComponentCreationException("Unable to add fabric client: newManifest is null")
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}