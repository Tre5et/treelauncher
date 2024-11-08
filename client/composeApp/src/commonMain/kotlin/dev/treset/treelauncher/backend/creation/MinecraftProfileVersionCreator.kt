package dev.treset.treelauncher.backend.creation

import dev.treset.mcdl.exception.FileDownloadException
import dev.treset.mcdl.minecraft.MinecraftProfile
import dev.treset.mcdl.minecraft.MinecraftVersion
import dev.treset.treelauncher.backend.config.appConfig
import dev.treset.treelauncher.backend.data.LauncherFiles
import dev.treset.treelauncher.backend.data.LauncherLaunchArgument
import dev.treset.treelauncher.backend.data.manifest.VersionComponent
import dev.treset.treelauncher.backend.util.FormatStringProvider
import dev.treset.treelauncher.backend.util.StatusProvider
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.localization.Strings
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.IOException

abstract class MinecraftProfileVersionCreator<T: MinecraftProfileCreationData>(
    data: T,
    statusProvider: StatusProvider
): VersionCreator<T>(data, statusProvider) {
    abstract val versionType: String
    abstract val versionCreationStep: FormatStringProvider

    open val defaultGameArguments: List<LauncherLaunchArgument> = listOf()
    open val defaultJvmArguments: List<LauncherLaunchArgument> = listOf()

    @Throws(IOException::class)
    abstract fun createVersion(javaFile: LauncherFile, statusProvider: StatusProvider): MinecraftProfile

    @Throws(IOException::class)
    override fun createNew(statusProvider: StatusProvider): VersionComponent {
        LOGGER.debug { "Creating new modloader version: id=${data.versionId}..." }

        val minecraftVersion = createInheritedVersion(statusProvider)

        val javaFile = getJavaFile(minecraftVersion)

        val creationProvider = statusProvider.subStep(versionCreationStep, -1)
        val profile = createVersion(javaFile, statusProvider)
        creationProvider.finish()

        val version = parseMinecraftProfile(profile, minecraftVersion)

        LOGGER.debug { "Created modloader version: id=${version.id.value}" }
        return version
    }

     @Throws(IOException::class)
    fun createInheritedVersion(statusProvider: StatusProvider): VersionComponent {
        statusProvider.next(Strings.creator.status.message.vanillaVersion())
        LOGGER.debug { "Creating minecraft version ${data.minecraftVersion}..." }

        val inheritVersion = MinecraftVersion.get(data.minecraftVersion)?: throw IOException("Unable to create modloader version: failed to find mc version: versionId=${data.minecraftVersion}")
        val vanillaProfile = try {
            MinecraftProfile.get(inheritVersion.url)
        } catch (e: FileDownloadException) {
            throw IOException("Unable to create modloader version: failed to download mc version details: versionId=${data.minecraftVersion}", e)
        }

        val creator = VanillaVersionCreator(
            VanillaCreationData(vanillaProfile, data.files),
            statusProvider
        )
        val mc = try {
            creator.create()
        } catch (e: IOException) {
            throw IOException("Unable to create modloader version: failed to create mc version: versionId=${data.minecraftVersion}", e)
        }

        LOGGER.debug { "Created minecraft version: id=${mc.id.value}" }
        return mc
    }

    @Throws(IOException::class)
    fun createLibrariesDirectory() {
        LOGGER.debug { "Creating libraries directory..." }
        if (!data.files.librariesDir.isDirectory()) {
            try {
                data.files.librariesDir.createDir()
            } catch (e: IOException) {
                throw IOException("Unable to add fabric libraries: failed to create libraries directory: path=${data.files.librariesDir}", e)
            }
        }
        LOGGER.debug { "Created libraries directory" }
    }

    fun parseMinecraftProfile(profile: MinecraftProfile, minecraftVersion: VersionComponent): VersionComponent {
        return VersionComponent(
            id = id,
            name = profile.id,
            versionNumber = data.minecraftVersion,
            versionType = versionType,
            loaderVersion = data.versionId,
            assets = profile.assets,
            virtualAssets = null,
            natives = null,
            depends = minecraftVersion.id.value,
            gameArguments = translateArguments(
                profile.launchArguments.game,
                defaultGameArguments
            ).map {
                // Hack because forge uses ${version_name} in a questionable way in some versions
                if(it.argument.contains("\${version_name}")) {
                    it.argument = it.argument.replace("\${version_name}", appConfig().minecraftDefaultFileName.dropLast(4))
                }
                it
            },
            jvmArguments = translateArguments(
                profile.launchArguments.jvm,
                defaultJvmArguments
            ).map {
                // Hack because forge uses ${version_name} in a questionable way in some versions
                if(it.argument.contains("\${version_name}")) {
                    it.argument = it.argument.replace("\${version_name}", appConfig().minecraftDefaultFileName.dropLast(4))
                }
                it
            },
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
    }

    fun getJavaFile(minecraftVersion: VersionComponent): LauncherFile {
        val path = System.getProperty("java.home")
        if(path == null) {
            LOGGER.warn { "Unable to get current jre path, using vanilla version jre" }
            return getVersionJava(minecraftVersion)
        }
        val installation = LauncherFile.of(path, "bin")
        val files = installation.listFiles()
        val file = files.find { it.name == "java" || it.name == "java.exe" }
        if(file == null) {
            LOGGER.warn { "Couldn't find java file for current installation, using vanilla java" }
            return getVersionJava(minecraftVersion)
        }

        return LauncherFile.of(file)
    }

    fun getVersionJava(minecraftVersion: VersionComponent): LauncherFile {
        return LauncherFile.ofData(data.files.mainManifest.javasDir.value, data.files.javaManifest.prefix.value + "_" + minecraftVersion.java.value, "bin", "java")
    }

    companion object {
        val LOGGER = KotlinLogging.logger {}
    }
}

open class MinecraftProfileCreationData(
    val minecraftVersion: String,
    version: String,
    files: LauncherFiles
): VersionCreationData(minecraftVersion, version, files)