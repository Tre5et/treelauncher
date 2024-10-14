package net.treset.treelauncher.backend.creation

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mc_version_loader.exception.FileDownloadException
import net.treset.mc_version_loader.forge.ForgeVersion
import net.treset.mc_version_loader.forge.MinecraftForge
import net.treset.mc_version_loader.json.SerializationException
import net.treset.mc_version_loader.minecraft.MinecraftGame
import net.treset.mc_version_loader.minecraft.MinecraftVersionDetails
import net.treset.mc_version_loader.util.FileUtil
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.data.LauncherFiles
import net.treset.treelauncher.backend.data.LauncherVersionDetails
import net.treset.treelauncher.backend.data.manifest.ComponentManifest
import net.treset.treelauncher.backend.data.manifest.LauncherManifestType
import net.treset.treelauncher.backend.data.manifest.ParentManifest
import net.treset.treelauncher.backend.util.CreationStatus
import net.treset.treelauncher.backend.util.exception.ComponentCreationException
import net.treset.treelauncher.backend.util.file.LauncherFile
import java.io.File
import java.io.IOException

class ForgeVersionCreator(
    typeConversion: Map<String, LauncherManifestType>,
    componentsManifest: ParentManifest,
    var versionId: String,
    files: LauncherFiles,
    var librariesDir: LauncherFile
) : VersionCreator(
    "forge-$versionId",
    typeConversion,
    componentsManifest,
    files
) {
    var forgeVersion: ForgeVersion? = null

    init {
        defaultStatus = CreationStatus(CreationStatus.DownloadStep.VERSION_FORGE, null)
    }

    override fun matchesVersion(id: String): Boolean {
        return id == versionId
    }

    @Throws(ComponentCreationException::class)
    override fun makeVersion() {
        setStatus(CreationStatus(CreationStatus.DownloadStep.VERSION_FORGE, null))
        LOGGER.debug { "Creating forge version..." }

        LOGGER.debug { "Fetching forge version..."}
        forgeVersion = try {
            MinecraftForge.getForgeVersion(versionId)
        } catch (e: Exception) {
            throw ComponentCreationException("Unable to create forge version: failed to get forge version", e)
        }
        LOGGER.debug { "Fetched forge version" }

        forgeVersion?.let { forgeVersion ->
            if(forgeVersion.inheritsFrom == null) {
                throw ComponentCreationException("Unable to create forge version: no valid forge version")
            }

            LOGGER.debug { "Creating minecraft version..." }
            val versions = try {
                MinecraftGame.getVersions()
            } catch (e: FileDownloadException) {
                throw ComponentCreationException("Unable to create forge version: failed to get mc versions", e)
            }

            for (m in versions) {
                if (forgeVersion.inheritsFrom == m.id) {
                    val mcJson = try {
                        FileUtil.getStringFromUrl(m.url)
                    } catch (e: FileDownloadException) {
                        throw ComponentCreationException("Unable to create forge version: failed to download mc version details: versionId=${forgeVersion.inheritsFrom}", e)
                    }

                    val mcCreator: VersionCreator = try {
                        VanillaVersionCreator(
                            typeConversion!!,
                            componentsManifest!!,
                            MinecraftVersionDetails.fromJson(mcJson),
                            files,
                            librariesDir,
                        )
                    } catch (e: SerializationException) {
                        throw ComponentCreationException("Unable to create forge version: failed to parse mc version details: versionId=${forgeVersion.inheritsFrom}", e)
                    }

                    mcCreator.statusCallback = statusCallback

                    val dependsId: String = try {
                        mcCreator.id
                    } catch (e: ComponentCreationException) {
                        throw ComponentCreationException("Unable to create forge version: failed to create mc version: versionId=${forgeVersion.inheritsFrom}", e)
                    }
                    LOGGER.debug { "Created minecraft version: id=$dependsId" }

                    val details = LauncherVersionDetails(
                        forgeVersion.inheritsFrom,
                        "forge",
                        versionId,
                        null,
                        null,
                        null,
                        dependsId,
                        listOf(),
                        listOf(),
                        "",
                        listOf(),
                        forgeVersion.mainClass,
                        "",
                        versionId
                    )

                    try {
                        addArguments(details)
                        addLibraries(details)
                        createClient(details, mcCreator.newManifest!!)
                    } catch (e: ComponentCreationException) {
                        mcCreator.attemptCleanup()
                        attemptCleanup()
                        throw ComponentCreationException("Unable to create forge version: versionId=${forgeVersion.inheritsFrom}", e)
                    }

                    try {
                        LauncherFile.of(newManifest!!.directory, newManifest!!.details).write(details)
                    } catch (e: IOException) {
                        throw ComponentCreationException("Unable to create fabric version: failed to write version details: versionId=${forgeVersion.inheritsFrom}", e)
                    }

                    LOGGER.debug { "Created forge version: id=${newManifest?.id}" }
                    return
                }
            }
        }?: throw ComponentCreationException("Unable to create forge version: invalid data")
    }

    @Throws(ComponentCreationException::class)
    private fun addArguments(details: LauncherVersionDetails) {
        LOGGER.debug { "Adding forge arguments..." }
        forgeVersion?.let {forgeVersion ->
            details.jvmArguments = translateArguments(
                forgeVersion.arguments.jvm,
                appConfig().forgeDefaultJvmArguments
            ).map {
                // Hack because forge uses ${version_name} in a questionable way in some versions
                if(it.argument.contains("\${version_name}")) {
                    it.argument = it.argument.replace("\${version_name}", appConfig().minecraftDefaultFileName.let { it.substring(0, it.length - 4) })
                }
                it
            }

            details.gameArguments = translateArguments(
                forgeVersion.arguments.game,
                appConfig().forgeDefaultGameArguments
            )
        }?: throw ComponentCreationException("Unable to create forge version: invalid data")
        LOGGER.debug { "Added forge arguments" }
    }

    @Throws(ComponentCreationException::class)
    private fun addLibraries(details: LauncherVersionDetails) {
        setStatus(CreationStatus(CreationStatus.DownloadStep.VERSION_FORGE_LIBRARIES, null))
        LOGGER.debug { "Adding forge libraries..." }
        forgeVersion?.libraries?.let {libraries ->
            if (!librariesDir.isDirectory()) {
                try {
                    librariesDir.createDir()
                } catch (e: IOException) {
                    throw ComponentCreationException("Unable to add fabric libraries: failed to create libraries directory: path=$librariesDir", e)
                }
            }

            val libs = try {
                MinecraftForge.downloadForgeLibraries(
                    librariesDir,
                    versionId,
                    libraries,
                    LauncherFile.of(newManifest!!.directory, appConfig().nativesDirName)
                ) {
                    setStatus(CreationStatus(CreationStatus.DownloadStep.VERSION_FORGE_LIBRARIES, it))
                }
            } catch (e: FileDownloadException) {
                throw ComponentCreationException("Unable to add forge libraries: failed to download libraries", e)
            }
            details.libraries = libs
            LOGGER.debug { "Added forge libraries" }
        }?: throw ComponentCreationException("Unable to create forge version: invalid data")
    }

    @Throws(ComponentCreationException::class)
    private fun createClient(details: LauncherVersionDetails, vanillaManifest: ComponentManifest) {
        setStatus(CreationStatus(CreationStatus.DownloadStep.VERSION_FORGE_FILE, null))
        LOGGER.debug { "Creating forge client..." }
        newManifest?.let { newManifest ->
            val baseDir = File(newManifest.directory)
            if (!baseDir.isDirectory()) {
                throw ComponentCreationException("Unable to add forge main file: base dir is not a directory")
            }

            LOGGER.debug { "Fetching installer profile..."}
            val profile = try {
                MinecraftForge.getForgeInstallProfile(versionId)
            } catch (e: FileDownloadException) {
                throw ComponentCreationException("Unable to create forge version: failed to get forge install profile", e)
            }
            LOGGER.debug { "Fetched installer profile" }

            LOGGER.debug { "Finding minecraft jar..." }
            val minecraftBaseDir = vanillaManifest.directory
            val minecraftFileName = files.versionComponents.firstOrNull { it.second.versionId == details.depends }?.second?.mainFile
                ?: appConfig().minecraftDefaultFileName
            val minecraftFile = LauncherFile.of(minecraftBaseDir, minecraftFileName)
            if(!minecraftFile.isFile) {
                throw ComponentCreationException("Unable to create forge version: failed to find mc version client file: versionId=${details.depends}")
            }
            LOGGER.debug { "Found minecraft jar" }

            LOGGER.debug { "Finding minecraft java..." }
            val vanillaDetailsFile = LauncherFile.of(vanillaManifest.directory, vanillaManifest.details)
            val vanillaDetails = try {
                LauncherVersionDetails.fromJson(vanillaDetailsFile.readString())
            } catch (e: Exception) {
                throw ComponentCreationException("Unable to create forge version: failed to read mc version details: versionId=${details.depends}", e)
            }
            val javaFile = LauncherFile.ofData(files.launcherDetails.javasDir, files.javaManifest.prefix + "_" + vanillaDetails.java, "bin", "java")
            LOGGER.debug { "Found minecraft java" }

            LOGGER.debug { "Patching forge client..."}
            try {
                MinecraftForge.createForgeClient(
                    versionId,
                    LauncherFile.ofData(files.launcherDetails.librariesDir),
                    profile,
                    minecraftFile,
                    javaFile
                ) {
                    setStatus(CreationStatus(CreationStatus.DownloadStep.VERSION_FORGE_FILE, it))
                }
            } catch (e: FileDownloadException) {
                throw ComponentCreationException("Unable to create forge version: failed to create forge client", e)
            }
            LOGGER.debug { "Created forge client" }
        }?: throw ComponentCreationException("Unable to create forge version: invalid data")
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}