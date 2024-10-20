package dev.treset.treelauncher.backend.creation

import dev.treset.mcdl.exception.FileDownloadException
import dev.treset.mcdl.minecraft.MinecraftVersion
import dev.treset.mcdl.minecraft.MinecraftVersionDetails
import dev.treset.mcdl.quiltmc.QuiltLibrary
import dev.treset.mcdl.quiltmc.QuiltProfile
import dev.treset.mcdl.quiltmc.QuiltVersion
import dev.treset.treelauncher.backend.config.appConfig
import dev.treset.treelauncher.backend.data.LauncherFiles
import dev.treset.treelauncher.backend.data.manifest.VersionComponent
import dev.treset.treelauncher.backend.util.FormatStringProvider
import dev.treset.treelauncher.backend.util.Status
import dev.treset.treelauncher.backend.util.StatusProvider
import dev.treset.treelauncher.backend.util.copyTo
import dev.treset.treelauncher.localization.Strings
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.IOException

class QuiltVersionCreator(
    data: QuiltCreationData,
    statusProvider: StatusProvider
) : VersionCreator<QuiltCreationData>(data, statusProvider) {
    constructor(
        data: QuiltCreationData,
        onStatus: (Status) -> Unit
    ) : this(data, StatusProvider(null, 0, onStatus))

    @Throws(IOException::class)
    override fun createNew(statusProvider: StatusProvider): VersionComponent {
        LOGGER.debug { "Creating quilt version..." }
        statusProvider.next(Strings.creator.status.message.vanillaVersion())

        if (data.profile.inheritsFrom == null) {
            throw IOException("Unable to create quilt version: no valid quilt profile")
        }

        LOGGER.debug { "Creating minecraft version..." }

        val inheritVersion = MinecraftVersion.get(data.profile.inheritsFrom)?: throw IOException("Unable to create quilt version: failed to find mc version: versionId=${data.profile.inheritsFrom}")
        val versionDetails = try {
            MinecraftVersionDetails.get(inheritVersion.url)
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
            throw IOException("Unable to create forge version: failed to create mc version: versionId=${data.profile.inheritsFrom}", e)
        }

        LOGGER.debug { "Created minecraft version: id=${mc.id.value}" }

        val version = VersionComponent(
            id = id,
            name = data.name,
            versionNumber = data.profile.inheritsFrom,
            versionType = "quilt",
            loaderVersion = data.version.loader.version,
            assets = null,
            virtualAssets = null,
            natives = null,
            depends = mc.id.value,
            gameArguments = translateArguments(
                data.profile.arguments.game,
                appConfig().quiltDefaultGameArguments
            ),
            jvmArguments = translateArguments(
                data.profile.arguments.jvm,
                appConfig().quiltDefaultJvmArguments
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
        } catch (e: IOException) {
            throw IOException("Unable to create quilt version: versionId=${data.profile.id}", e)
        }
        LOGGER.debug { "Created quilt version: id=${version.id.value}" }
        return version
    }

    @Throws(IOException::class)
    private fun addLibraries(data: QuiltCreationData, version: VersionComponent, statusProvider: StatusProvider) {
        LOGGER.debug { "Adding quilt libraries..." }
        val librariesProvider = statusProvider.subStep(CreationStep.VERSION_QUILT_LIBRARIES, 3)
        librariesProvider.next()
        data.profile.libraries?.let { libraries: List<QuiltLibrary> ->
            if (!data.files.librariesDir.isDirectory()) {
                try {
                    data.files.librariesDir.createDir()
                } catch (e: IOException) {
                    throw IOException("Unable to add quilt libraries: failed to create libraries directory: path=${data.files.librariesDir}", e)
                }
            }
            val libs: List<String> = try {
                QuiltLibrary.downloadAll(
                    libraries,
                    data.files.librariesDir
                ) {
                    LOGGER.debug { "Downloading quilt library: ${it.currentFile}" }
                    librariesProvider.download(it, 1, 1)
                }
            } catch (e: FileDownloadException) {
                throw IOException("Unable to add quilt libraries: failed to download libraries", e)
            }
            libs.copyTo(version.libraries)
            librariesProvider.finish()
            LOGGER.debug { "Added quilt libraries" }
        }?: throw IOException("Unable to add quilt libraries: libraries invalid")
    }

    override val step = CreationStep.VERSION_QUILT
    override val total = 1

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}

class QuiltCreationData(
    val version: QuiltVersion,
    val profile: QuiltProfile,
    files: LauncherFiles
): VersionCreationData(profile.id, profile.id, files)

val CreationStep.VERSION_QUILT: FormatStringProvider
    get() = FormatStringProvider { Strings.creator.status.version.quilt() }
val CreationStep.VERSION_QUILT_LIBRARIES: FormatStringProvider
    get() = FormatStringProvider { Strings.creator.status.version.quiltLibraries() }