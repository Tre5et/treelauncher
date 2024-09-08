package net.treset.treelauncher.backend.creation

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mcdl.exception.FileDownloadException
import net.treset.mcdl.json.SerializationException
import net.treset.mcdl.minecraft.MinecraftVersion
import net.treset.mcdl.minecraft.MinecraftVersionDetails
import net.treset.mcdl.quiltmc.QuiltLibrary
import net.treset.mcdl.quiltmc.QuiltProfile
import net.treset.mcdl.quiltmc.QuiltVersion
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.data.LauncherFiles
import net.treset.treelauncher.backend.data.LauncherVersionDetails
import net.treset.treelauncher.backend.data.manifest.LauncherManifestType
import net.treset.treelauncher.backend.data.manifest.ParentManifest
import net.treset.treelauncher.backend.util.CreationStatus
import net.treset.treelauncher.backend.util.exception.ComponentCreationException
import net.treset.treelauncher.backend.util.file.LauncherFile
import java.io.IOException

class QuiltVersionCreator(
    typeConversion: Map<String, LauncherManifestType>,
    componentsManifest: ParentManifest,
    var quiltVersion: QuiltVersion,
    var quiltProfile: QuiltProfile,
    files: LauncherFiles,
    var librariesDir: LauncherFile
) : VersionCreator(
    quiltProfile.id,
    typeConversion,
    componentsManifest,
    files
) {
    override fun matchesVersion(id: String): Boolean {
        return id == quiltProfile.id
    }

    @Throws(ComponentCreationException::class)
    override fun makeVersion() {
        setStatus(CreationStatus(CreationStatus.DownloadStep.VERSION_QUILT, null))
        LOGGER.debug { "Creating quilt version..." }

        if (quiltProfile.inheritsFrom == null) {
            throw ComponentCreationException("Unable to create quilt version: no valid quilt profile")
        }

        LOGGER.debug { "Creating minecraft version..." }
        val versions: List<MinecraftVersion> = try {
            MinecraftVersion.getAll()
        } catch (e: FileDownloadException) {
            throw ComponentCreationException("Unable to create quilt version: failed to get mc versions", e)
        }

        for (m in versions) {
            if (quiltProfile.inheritsFrom == m.id) {
                val versionDetails= try {
                    MinecraftVersionDetails.get(m.url)
                } catch (e: FileDownloadException) {
                    throw ComponentCreationException("Unable to create quilt version: failed to download mc version details: versionId=${quiltProfile.id}", e)
                }
                val mcCreator: VersionCreator = try {
                    VanillaVersionCreator(
                        typeConversion!!,
                        componentsManifest!!,
                        versionDetails,
                        files,
                        librariesDir
                    )
                } catch (e: SerializationException) {
                    throw ComponentCreationException("Unable to create quilt version: failed to parse mc version details: versionId=${quiltProfile.id}", e)
                }
                mcCreator.statusCallback = statusCallback
                val dependsId: String = try {
                    mcCreator.id
                } catch (e: ComponentCreationException) {
                    throw ComponentCreationException("Unable to create quilt version: failed to create mc version: versionId=${quiltProfile.id}", e)
                }
                LOGGER.debug { "Created minecraft version: id=$dependsId" }

                val details = LauncherVersionDetails(
                    quiltProfile.inheritsFrom,
                    "quilt",
                    quiltVersion.loader.version,
                    null,
                    null,
                    null,
                    dependsId,
                    listOf(),
                    listOf(),
                    "",
                    listOf(),
                    quiltProfile.mainClass,
                    null,
                    quiltProfile.id
                )
                try {
                    addArguments(details)
                    addLibraries(details)
                } catch (e: ComponentCreationException) {
                    mcCreator.attemptCleanup()
                    attemptCleanup()
                    throw ComponentCreationException("Unable to create quilt version: versionId=${quiltProfile.id}", e)
                }
                try {
                    LauncherFile.of(newManifest!!.directory, newManifest!!.details).write(details)
                } catch (e: IOException) {
                    throw ComponentCreationException("Unable to create quilt version: failed to write version details: versionId=${quiltProfile.id}", e)
                }
                LOGGER.debug { "Created fabric version: id=${newManifest?.id}" }
                return
            }
        }
        throw ComponentCreationException("Unable to create quilt version: failed to find mc version: versionId=${quiltProfile.inheritsFrom}")
    }

    @Throws(ComponentCreationException::class)
    private fun addArguments(details: LauncherVersionDetails) {
        LOGGER.debug { "Adding quilt arguments..." }
        details.jvmArguments = translateArguments(
            quiltProfile.arguments.jvm,
            appConfig().fabricDefaultJvmArguments
        )
        details.gameArguments = translateArguments(
            quiltProfile.arguments.game,
            appConfig().fabricDefaultGameArguments
        )
        LOGGER.debug { "Added quilt arguments" }
    }

    @Throws(ComponentCreationException::class)
    private fun addLibraries(details: LauncherVersionDetails) {
        setStatus(CreationStatus(CreationStatus.DownloadStep.VERSION_QUILT_LIBRARIES, null))
        LOGGER.debug { "Adding quilt libraries..." }
        quiltProfile.libraries?.let { libraries: List<QuiltLibrary> ->
            if (!librariesDir.isDirectory()) {
                try {
                    librariesDir.createDir()
                } catch (e: IOException) {
                    throw ComponentCreationException("Unable to add quilt libraries: failed to create libraries directory: path=$librariesDir", e)
                }
            }
            val libs: List<String> = try {
                QuiltLibrary.downloadAll(
                    libraries,
                    librariesDir
                ) {
                    setStatus(CreationStatus(CreationStatus.DownloadStep.VERSION_QUILT_LIBRARIES, it))
                }
            } catch (e: FileDownloadException) {
                throw ComponentCreationException("Unable to add quilt libraries: failed to download libraries", e)
            }
            details.libraries = libs
            LOGGER.debug { "Added quilt libraries" }
        }?: throw ComponentCreationException("Unable to add quilt libraries: libraries invalid")
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}