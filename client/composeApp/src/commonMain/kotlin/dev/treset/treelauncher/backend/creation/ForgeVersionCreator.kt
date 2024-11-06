package dev.treset.treelauncher.backend.creation

import dev.treset.mcdl.exception.FileDownloadException
import dev.treset.mcdl.forge.ForgeInstallerExecutor
import dev.treset.mcdl.minecraft.MinecraftProfile
import dev.treset.mcdl.minecraft.MinecraftVersion
import dev.treset.treelauncher.backend.config.appConfig
import dev.treset.treelauncher.backend.data.LauncherFiles
import dev.treset.treelauncher.backend.data.manifest.VersionComponent
import dev.treset.treelauncher.backend.util.FormatStringProvider
import dev.treset.treelauncher.backend.util.Status
import dev.treset.treelauncher.backend.util.StatusProvider
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.localization.Strings
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
        LOGGER.debug { "Creating new forge version: id=${data.versionId}..." }

        val installer = ForgeInstallerExecutor(data.versionId)

        statusProvider.next(Strings.creator.status.message.vanillaVersion())
        LOGGER.debug { "Creating minecraft version ${data.minecraftVersion}..." }

        val inheritVersion = MinecraftVersion.get(data.minecraftVersion)?: throw IOException("Unable to create forge version: failed to find mc version: versionId=${data.minecraftVersion}")
        val vanillaProfile = try {
            MinecraftProfile.get(inheritVersion.url)
        } catch (e: FileDownloadException) {
            throw IOException("Unable to create fabric version: failed to download mc version details: versionId=${data.minecraftVersion}", e)
        }

        val creator = VanillaVersionCreator(
            VanillaCreationData(vanillaProfile, data.files),
            statusProvider
        )
        val mc = try {
            creator.create()
        } catch (e: IOException) {
            throw IOException("Unable to create forge version: failed to create mc version: versionId=${data.minecraftVersion}", e)
        }

        LOGGER.debug { "Created minecraft version: id=${mc.id.value}" }

        LOGGER.debug { "Creating libraries directory..." }
        if (!data.files.librariesDir.isDirectory()) {
            try {
                data.files.librariesDir.createDir()
            } catch (e: IOException) {
                throw IOException("Unable to add fabric libraries: failed to create libraries directory: path=${data.files.librariesDir}", e)
            }
        }
        LOGGER.debug { "Created libraries directory" }

        val javaFile = LauncherFile.ofData(data.files.mainManifest.javasDir.value, data.files.javaManifest.prefix.value + "_" + mc.java.value, "bin", "java")

        LOGGER.debug { "Running forge installer..." }
        val installerStatus = statusProvider.subStep(VERSION_FORGE_INSTALLER, -1)

        val profile = try {
            installer.install(
                data.files.librariesDir,
                javaFile
            ) { installerStatus.download(it, 0, 0) }
        } catch (e: FileDownloadException) {
            throw IOException("Unable to create forge version: failed to execute insatller: versionId=${data.versionId}", e)
        }
        installerStatus.finish()
        LOGGER.debug { "Ran forge installer" }

        val version = VersionComponent(
            id = id,
            name = profile.id,
            versionNumber = data.minecraftVersion,
            versionType = "forge",
            loaderVersion = data.versionId,
            assets = profile.assets,
            virtualAssets = null,
            natives = null,
            depends = mc.id.value,
            gameArguments = translateArguments(
                profile.launchArguments.game,
                appConfig().forgeDefaultGameArguments
            ).map {
                // Hack because forge uses ${version_name} in a questionable way in some versions
                if(it.argument.contains("\${version_name}")) {
                    it.argument = it.argument.replace("\${version_name}", appConfig().minecraftDefaultFileName.let { it.substring(0, it.length - 4) })
                }
                it
            },
            jvmArguments = translateArguments(
                profile.launchArguments.jvm,
                appConfig().forgeDefaultJvmArguments
            ),
            java = null,
            libraries = profile.libraries.mapNotNull {
                it.downloads?.artifact?.path.let {
                    if(it.isNullOrBlank()) {
                        null
                    } else {
                        it
                    }
                }
            },
            mainClass = profile.mainClass,
            mainFile = null,
            versionId = data.versionId,
            file = file,
        )

        LOGGER.debug { "Created forge version: id=${version.id}" }
        return version
    }

    override val step = VERSION_FORGE
    override val total = 1

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}

class ForgeCreationData(
    val minecraftVersion: String,
    version: String,
    files: LauncherFiles
): VersionCreationData(version, version, files)

val VERSION_FORGE = FormatStringProvider { Strings.creator.status.version.forge() }
val VERSION_FORGE_INSTALLER = FormatStringProvider { Strings.creator.status.version.forgeInstaller() }