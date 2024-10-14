package dev.treset.treelauncher.backend.data.manifest

import dev.treset.mcdl.saves.Save
import dev.treset.mcdl.saves.Server
import dev.treset.treelauncher.backend.config.appConfig
import dev.treset.treelauncher.backend.launching.resources.SavesDisplayData
import dev.treset.treelauncher.backend.util.file.LauncherFile
import java.io.IOException

class SavesComponent(
    id: String,
    name: String,
    file: LauncherFile,
    type: LauncherManifestType = LauncherManifestType.SAVES_COMPONENT,
    includedFiles: Array<String> = appConfig().savesDefaultIncludedFiles,
    lastUsed: String = "",
    active: Boolean = false
): Component(
    type,
    id,
    name,
    includedFiles,
    lastUsed,
    active,
    file
) {
    fun getDisplayData(gameDataDir: LauncherFile): SavesDisplayData {
        val displayData = SavesDisplayData(
            saves = mapOf(),
            servers = listOf(),
            onAdd = { f -> this.onAdd(f, gameDataDir) }
        )

        displayData.loadSaves(gameDataDir)
        displayData.loadServers(gameDataDir)
        return displayData
    }

    private fun SavesDisplayData.loadSaves(gameDataDir: LauncherFile) {
        val savesDirectory = LauncherFile.of(getContentDirectory(gameDataDir), "saves")

        saves = savesDirectory.listFiles().mapNotNull {
            try {
                Save.get(it) to it
            } catch (e: IOException) {
                null
            }
        }.toMap()
    }

    private fun SavesDisplayData.loadServers(gameDataDir: LauncherFile) {
        val serverFile = LauncherFile.of(getContentDirectory(gameDataDir), "servers.dat")

        servers = try {
            Server.getAll(serverFile)
        } catch (e: IOException) {
            emptyList()
        }
    }

    @Throws(IOException::class)
    private fun SavesDisplayData.onAdd(source: List<LauncherFile>, gameDataDir: LauncherFile) {
        val directory = LauncherFile.of(getContentDirectory(gameDataDir), "saves")
        for(file in source) {
            file.copyUniqueTo(directory)
        }
        this.loadSaves(gameDataDir)
    }

    companion object {
        @Throws(IOException::class)
        fun readFile(file: LauncherFile): SavesComponent {
            return readFile(
                file,
                LauncherManifestType.SAVES_COMPONENT,
            )
        }
    }
}