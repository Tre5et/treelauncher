package net.treset.treelauncher.backend.data.manifest

import net.treset.mcdl.resourcepacks.Resourcepack
import net.treset.mcdl.resourcepacks.Texturepack
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.launching.resources.ResourcepacksDisplayData
import net.treset.treelauncher.backend.util.file.LauncherFile
import java.io.IOException

class ResourcepackComponent(
    id: String,
    name: String,
    file: LauncherFile,
    includedFiles: Array<String> = appConfig().resourcepacksDefaultIncludedFiles,
    lastUsed: String = "",
    active: Boolean = false
): Component(
    LauncherManifestType.RESOURCEPACKS_COMPONENT,
    id,
    name,
    includedFiles,
    lastUsed,
    active,
    file
) {
    fun getDisplayData(gameDataDir: LauncherFile): ResourcepacksDisplayData {
        val displayData = ResourcepacksDisplayData(
            resourcepacks = mapOf(),
            texturepacks = mapOf(),
            onAddResourcepack = { f -> this.onAddResourcepack(f, gameDataDir) },
            onAddTexturepack = { f -> this.onAddTexturepack(f, gameDataDir) }
        )

        displayData.loadResourcepacks(gameDataDir)
        displayData.loadTexturepacks(gameDataDir)
        return displayData
    }


    private fun ResourcepacksDisplayData.loadResourcepacks(gameDataDir: LauncherFile) {
        val resourcepacksDirectory = LauncherFile.of(getContentDirectory(gameDataDir), "resourcepacks")

        resourcepacks = resourcepacksDirectory.listFiles()
            .mapNotNull {
                try {
                    Resourcepack.get(file)
                } catch (e: Exception) {
                    null
                }?.let {rp -> rp to it}
            }.toMap()
    }

    private fun ResourcepacksDisplayData.loadTexturepacks(gameDataDir: LauncherFile) {
        val texturepacksDirectory = LauncherFile.of(getContentDirectory(gameDataDir), "texturepacks")

        texturepacks = texturepacksDirectory.listFiles()
            .mapNotNull {
                try {
                    Texturepack.get(it)
                } catch (e: Exception) {
                    null
                }?.let {tp -> tp to it}
            }.toMap()
    }

    @Throws(IOException::class)
    private fun ResourcepacksDisplayData.onAddResourcepack(source: List<LauncherFile>, gameDataDir: LauncherFile) {
        val directory = LauncherFile.of(getContentDirectory(gameDataDir), "resourcepacks")
        for(file in source) {
            file.copyUniqueTo(directory)
        }
        this.loadResourcepacks(gameDataDir)
    }

    @Throws(IOException::class)
    private fun ResourcepacksDisplayData.onAddTexturepack(source: List<LauncherFile>, gameDataDir: LauncherFile) {
        val directory = LauncherFile.of(getContentDirectory(gameDataDir), "texturepacks")
        for (file in source) {
            file.copyUniqueTo(directory)
        }
        this.loadTexturepacks(gameDataDir)
    }

    companion object {
        @Throws(IOException::class)
        fun readFile(file: LauncherFile): ResourcepackComponent {
            return readFile(
                file,
                LauncherManifestType.RESOURCEPACKS_COMPONENT,
            )
        }
    }
}