package dev.treset.treelauncher.backend.creation

import dev.treset.mcdl.exception.FileDownloadException
import dev.treset.mcdl.forge.ForgeInstallerExecutor
import dev.treset.mcdl.minecraft.MinecraftProfile
import dev.treset.mcdl.minecraft.MinecraftVersion
import dev.treset.mcdl.neoforge.NeoForgeInstallerExecutor
import dev.treset.treelauncher.backend.config.appConfig
import dev.treset.treelauncher.backend.data.LauncherFiles
import dev.treset.treelauncher.backend.data.LauncherLaunchArgument
import dev.treset.treelauncher.backend.data.manifest.VersionComponent
import dev.treset.treelauncher.backend.util.FormatStringProvider
import dev.treset.treelauncher.backend.util.Status
import dev.treset.treelauncher.backend.util.StatusProvider
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.localization.Strings
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.IOException

class NeoForgeVersionCreator(
    data: NeoForgeCreationData,
    statusProvider: StatusProvider
) : MinecraftProfileVersionCreator<NeoForgeCreationData>(data, statusProvider) {
    constructor(
        data: NeoForgeCreationData,
        onStatus: (Status) -> Unit
    ) : this(data, StatusProvider(null, 0, onStatus))

    override val step = VERSION_NEO_FORGE
    override val total = 1

    override val versionType = "neoforge"
    override val versionCreationStep = VERSION_NEO_FORGE_INSTALLER

    override val defaultJvmArguments = appConfig().neoForgeDefaultJvmArguments
    override val defaultGameArguments = appConfig().neoForgeDefaultGameArguments

    override fun createVersion(javaFile: LauncherFile, statusProvider: StatusProvider): MinecraftProfile {
        LOGGER.debug { "Running neoforge installer..." }
        val installer = NeoForgeInstallerExecutor(data.versionId)
        val profile = try {
            installer.install(
                data.files.librariesDir,
                javaFile
            ) { statusProvider.download(it, 0, 0) }
        } catch (e: FileDownloadException) {
            throw IOException("Unable to create neoforge version: failed to execute insatller: versionId=${data.versionId}", e)
        }
        LOGGER.debug { "Ran neoforge installer" }
        return profile
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}

typealias NeoForgeCreationData = MinecraftProfileCreationData

val VERSION_NEO_FORGE = FormatStringProvider { Strings.creator.status.version.neoForge() }
val VERSION_NEO_FORGE_INSTALLER = FormatStringProvider { Strings.creator.status.version.neoForgeInstaller() }