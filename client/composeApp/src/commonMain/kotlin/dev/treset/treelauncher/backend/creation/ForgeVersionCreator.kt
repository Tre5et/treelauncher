package dev.treset.treelauncher.backend.creation

import dev.treset.mcdl.exception.FileDownloadException
import dev.treset.mcdl.forge.ForgeInstallerExecutor
import dev.treset.mcdl.minecraft.MinecraftProfile
import dev.treset.treelauncher.backend.config.appConfig
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
) : MinecraftProfileVersionCreator<ForgeCreationData>(data, statusProvider) {
    constructor(
        data: ForgeCreationData,
        onStatus: (Status) -> Unit
    ) : this(data, StatusProvider(null, 0, onStatus))

    override val step = VERSION_FORGE
    override val total = 1

    override val versionType = "forge"
    override val versionCreationStep = VERSION_FORGE_INSTALLER

    override val defaultJvmArguments = appConfig().forgeDefaultJvmArguments
    override val defaultGameArguments = appConfig().forgeDefaultGameArguments

    @Throws(IOException::class)
    override fun createVersion(javaFile: LauncherFile, statusProvider: StatusProvider): MinecraftProfile {
        LOGGER.debug { "Running forge installer..." }
        val installer = ForgeInstallerExecutor(data.versionId)
        val profile = try {
            installer.install(
                data.files.librariesDir,
                javaFile
            ) {
                statusProvider.download(it, 0, 0)
                LOGGER.debug { "Forge installer: ${it.currentFile}" }
            }
        } catch (e: FileDownloadException) {
            throw IOException("Unable to create forge version: failed to execute insatller: versionId=${data.versionId}", e)
        }
        LOGGER.debug { "Ran forge installer" }
        return profile
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}

typealias ForgeCreationData = MinecraftProfileCreationData

val VERSION_FORGE = FormatStringProvider { Strings.creator.status.version.forge() }
val VERSION_FORGE_INSTALLER = FormatStringProvider { Strings.creator.status.version.forgeInstaller() }