package net.treset.treelauncher.backend.creation

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mc_version_loader.exception.FileDownloadException
import net.treset.mc_version_loader.fabric.FabricLibrary
import net.treset.mc_version_loader.fabric.FabricLoader
import net.treset.mc_version_loader.fabric.FabricProfile
import net.treset.mc_version_loader.fabric.FabricVersionDetails
import net.treset.mc_version_loader.json.SerializationException
import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.mc_version_loader.launcher.LauncherManifestType
import net.treset.mc_version_loader.launcher.LauncherVersionDetails
import net.treset.mc_version_loader.minecraft.MinecraftGame
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

class FabricVersionCreator(
    typeConversion: Map<String, LauncherManifestType>,
    componentsManifest: LauncherManifest,
    var fabricVersion: FabricVersionDetails,
    var fabricProfile: FabricProfile,
    var files: LauncherFiles,
    var librariesDir: LauncherFile
) : VersionCreator(
    fabricProfile.id,
    typeConversion,
    componentsManifest
) {
    init {
        defaultStatus = CreationStatus(CreationStatus.DownloadStep.VERSION, null)
    }

    @Throws(ComponentCreationException::class)
    override fun createComponent(): String {
        for (v in files.versionComponents) {
            if (v.second.versionId != null && v.second.versionId == fabricProfile.id) {
                LOGGER.debug { "Matching version already exists, using instead: versionId=${v.second.versionId}, usingId=${v.first.id}" }
                uses = v.first
                return useComponent()
            }
        }
        val result = super.createComponent()
        if (newManifest == null) {
            attemptCleanup()
            throw ComponentCreationException("Failed to create version component: invalid data")
        }
        makeVersion()
        LOGGER.debug { "Created version component: id=${newManifest!!.id}" }
        return result
    }

    @Throws(ComponentCreationException::class)
    private fun makeVersion() {
        setStatus(CreationStatus(CreationStatus.DownloadStep.VERSION_FABRIC, null))
        if (fabricProfile.inheritsFrom == null) {
            throw ComponentCreationException("Unable to create fabric version: no valid fabric profile")
        }
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
                val details = LauncherVersionDetails(
                    fabricProfile.inheritsFrom,
                    "fabric",
                    fabricVersion.loader.version,
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

    @Throws(ComponentCreationException::class)
    private fun addFabricArguments(details: LauncherVersionDetails) {
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
    private fun addFabricLibraries(details: LauncherVersionDetails) {
        setStatus(CreationStatus(CreationStatus.DownloadStep.VERSION_LIBRARIES, null))
        fabricProfile.libraries?.let { libraries: List<FabricLibrary> ->
            if (!librariesDir.isDirectory()) {
                try {
                    librariesDir.createDir()
                } catch (e: IOException) {
                    throw ComponentCreationException("Unable to add fabric libraries: failed to create libraries directory: path=$librariesDir", e)
                }
            }
            val loaderPattern = PatternString(":fabric-loader:", true)
            val clientLibs = libraries.filter { !loaderPattern.matches(it.name) }.toList()
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
        }?: throw ComponentCreationException("Unable to add fabric libraries: libraries invalid")
    }

    @Throws(ComponentCreationException::class)
    private fun addFabricFile(details: LauncherVersionDetails) {
        newManifest?.let { newManifest ->
            val baseDir = File(newManifest.directory)
            if (!baseDir.isDirectory()) {
                throw ComponentCreationException("Unable to add fabric file: base dir is not a directory")
            }
            try {
                FabricLoader.downloadFabricClient(File(baseDir, appConfig().fabricDefaultClientFileName), fabricVersion.loader)
            } catch (e: FileDownloadException) {
                throw ComponentCreationException("Unable to add fabric file: failed to download fabric loader", e)
            }
            details.mainFile = appConfig().fabricDefaultClientFileName
            LOGGER.debug { "Added fabric file: mainFile=${details.mainFile}" }
        }?: throw ComponentCreationException("Unable to add fabric file: newManifest is null")
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}