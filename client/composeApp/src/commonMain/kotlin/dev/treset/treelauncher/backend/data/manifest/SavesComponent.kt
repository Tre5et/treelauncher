package dev.treset.treelauncher.backend.data.manifest

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import dev.treset.mcdl.saves.Save
import dev.treset.mcdl.saves.Server
import dev.treset.treelauncher.backend.config.appConfig
import dev.treset.treelauncher.backend.launching.resources.SavesDisplayData
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.backend.util.serialization.MutableDataState
import dev.treset.treelauncher.backend.util.serialization.MutableDataStateList
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.io.IOException

@Serializable
class SavesComponent(
    override val id: MutableDataState<String>,
    override val name: MutableDataState<String>,
    @Transient override val file: MutableDataState<LauncherFile> = mutableStateOf(LauncherFile.of("")),
    override val includedFiles: MutableDataStateList<String> = appConfig().savesDefaultIncludedFiles.toMutableStateList(),
    override val lastUsed: MutableDataState<String> = mutableStateOf(""),
    override val active: MutableDataState<Boolean> = mutableStateOf(false)
): Component() {
    override val type= LauncherManifestType.SAVES_COMPONENT
    @Transient override var expectedType = LauncherManifestType.SAVES_COMPONENT

    constructor(
        id: String,
        name: String,
        file: LauncherFile,
        active: Boolean = false,
        lastUsed: String = "",
        includedFiles: List<String> = appConfig().savesDefaultIncludedFiles
    ): this(
        mutableStateOf(id),
        mutableStateOf(name),
        mutableStateOf(file),
        includedFiles.toMutableStateList(),
        mutableStateOf(lastUsed),
        mutableStateOf(active)
    )

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
}