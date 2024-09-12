package net.treset.treelauncher.backend.creation

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mcdl.exception.FileDownloadException
import net.treset.mcdl.forge.ForgeInstaller
import net.treset.mcdl.json.SerializationException
import net.treset.mcdl.minecraft.MinecraftVersion
import net.treset.mcdl.minecraft.MinecraftVersionDetails
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
    var forgeInstaller: ForgeInstaller? = null

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
        forgeInstaller = try {
            ForgeInstaller.getForVersion(versionId)
        } catch (e: Exception) {
            throw ComponentCreationException("Unable to create forge version: failed to get forge version", e)
        }
        LOGGER.debug { "Fetched forge version" }

        forgeInstaller?.let { forgeInstaller ->
            if(forgeInstaller.installData.inheritsFrom == null) {
                throw ComponentCreationException("Unable to create forge version: no valid forge version")
            }

            LOGGER.debug { "Creating minecraft version..." }
            val versions = try {
                MinecraftVersion.getAll()
            } catch (e: FileDownloadException) {
                throw ComponentCreationException("Unable to create forge version: failed to get mc versions", e)
            }

            for (m in versions) {
                if (forgeInstaller.installData.inheritsFrom == m.id) {
                    val versionDetails = try {
                        MinecraftVersionDetails.get(m.url)
                    } catch (e: FileDownloadException) {
                        throw ComponentCreationException("Unable to create forge version: failed to download mc version details: versionId=${forgeInstaller.installData.inheritsFrom}", e)
                    }

                    val mcCreator: VersionCreator = try {
                        VanillaVersionCreator(
                            typeConversion!!,
                            componentsManifest!!,
                            versionDetails,
                            files,
                            librariesDir,
                        )
                    } catch (e: SerializationException) {
                        throw ComponentCreationException("Unable to create forge version: failed to parse mc version details: versionId=${forgeInstaller.installData.inheritsFrom}", e)
                    }

                    mcCreator.statusCallback = statusCallback

                    val dependsId: String = try {
                        mcCreator.id
                    } catch (e: ComponentCreationException) {
                        throw ComponentCreationException("Unable to create forge version: failed to create mc version: versionId=${forgeInstaller.installData.inheritsFrom}", e)
                    }
                    LOGGER.debug { "Created minecraft version: id=$dependsId" }

                    val details = LauncherVersionDetails(
                        forgeInstaller.installData.inheritsFrom,
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
                        forgeInstaller.installData.mainClass,
                        "",
                        versionId
                    )

                    try {
                        addArguments(details)
                        addLibraries(details)
                        createClient(mcCreator.newManifest!!)
                    } catch (e: ComponentCreationException) {
                        mcCreator.attemptCleanup()
                        attemptCleanup()
                        throw ComponentCreationException("Unable to create forge version: versionId=${forgeInstaller.installData.inheritsFrom}", e)
                    }

                    try {
                        LauncherFile.of(newManifest!!.directory, newManifest!!.details).write(details)
                    } catch (e: IOException) {
                        throw ComponentCreationException("Unable to create fabric version: failed to write version details: versionId=${forgeInstaller.installData.inheritsFrom}", e)
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
        forgeInstaller?.let { forgeInstaller ->
            details.jvmArguments = translateArguments(
                forgeInstaller.installData.arguments.jvm,
                appConfig().forgeDefaultJvmArguments
            ).map {
                // Hack because forge uses ${version_name} in a questionable way in some versions
                if(it.argument.contains("\${version_name}")) {
                    it.argument = it.argument.replace("\${version_name}", appConfig().minecraftDefaultFileName.let { it.substring(0, it.length - 4) })
                }
                it
            }

            details.gameArguments = translateArguments(
                forgeInstaller.installData.arguments.game,
                appConfig().forgeDefaultGameArguments
            )
        }?: throw ComponentCreationException("Unable to create forge version: invalid data")
        LOGGER.debug { "Added forge arguments" }
    }

    @Throws(ComponentCreationException::class)
    private fun addLibraries(details: LauncherVersionDetails) {
        setStatus(CreationStatus(CreationStatus.DownloadStep.VERSION_FORGE_LIBRARIES, null))
        LOGGER.debug { "Adding forge libraries..." }
        forgeInstaller?.let { installer ->
            if (!librariesDir.isDirectory()) {
                try {
                    librariesDir.createDir()
                } catch (e: IOException) {
                    throw ComponentCreationException("Unable to add fabric libraries: failed to create libraries directory: path=$librariesDir", e)
                }
            }

            val libs = try {
                installer.downloadLibraries(
                    librariesDir,
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
    private fun createClient(vanillaManifest: ComponentManifest) {
        setStatus(CreationStatus(CreationStatus.DownloadStep.VERSION_FORGE_FILE, null))
        LOGGER.debug { "Creating forge client..." }
        newManifest?.let { newManifest ->
            forgeInstaller?.let { installer ->
                val baseDir = File(newManifest.directory)
                if (!baseDir.isDirectory()) {
                    throw ComponentCreationException("Unable to add forge main file: base dir is not a directory")
                }

                LOGGER.debug { "Finding minecraft jar..." }
                val minecraftBaseDir = vanillaManifest.directory
                val minecraftFileName = files.versionComponents.firstOrNull { it.second.versionId == installer.installData.inheritsFrom }?.second?.mainFile
                    ?: appConfig().minecraftDefaultFileName
                val minecraftFile = LauncherFile.of(minecraftBaseDir, minecraftFileName)
                if(!minecraftFile.isFile) {
                    throw ComponentCreationException("Unable to create forge version: failed to find mc version client file: versionId=${installer.installData.inheritsFrom}")
                }
                LOGGER.debug { "Found minecraft jar" }

                LOGGER.debug { "Finding minecraft java..." }
                val vanillaDetailsFile = LauncherFile.of(vanillaManifest.directory, vanillaManifest.details)
                val vanillaDetails = try {
                    LauncherVersionDetails.fromJson(vanillaDetailsFile.readString())
                } catch (e: SerializationException) {
                    throw ComponentCreationException("Unable to create forge version: failed to read mc version details: versionId=${installer.installData.inheritsFrom}", e)
                }
                val javaFile = LauncherFile.ofData(files.launcherDetails.javasDir, files.javaManifest.prefix + "_" + vanillaDetails.java, "bin", "java")
                LOGGER.debug { "Found minecraft java" }

                LOGGER.debug { "Patching forge client..."}
                try {
                    installer.createClient(
                        LauncherFile.ofData(files.launcherDetails.librariesDir),
                        minecraftFile,
                        javaFile
                    ) {
                        setStatus(CreationStatus(CreationStatus.DownloadStep.VERSION_FORGE_FILE, it))
                    }
                } catch (e: FileDownloadException) {
                    throw ComponentCreationException("Unable to create forge version: failed to create forge client", e)
                }
                LOGGER.debug { "Created forge client" }
            }
        }?: throw ComponentCreationException("Unable to create forge version: invalid data")
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}