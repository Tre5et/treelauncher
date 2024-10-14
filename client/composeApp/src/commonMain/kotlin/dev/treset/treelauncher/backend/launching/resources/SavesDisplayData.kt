package dev.treset.treelauncher.backend.launching.resources

import dev.treset.mcdl.saves.Save
import dev.treset.mcdl.saves.Server
import dev.treset.treelauncher.backend.util.file.LauncherFile
import java.io.IOException

data class SavesDisplayData(
    var saves: Map<Save, LauncherFile>,
    var servers: List<Server>,
    private val onAdd: SavesDisplayData.(source: List<LauncherFile>) -> Unit,
) {
    @Throws(IOException::class)
    fun addSaves(source: List<LauncherFile>) {
        onAdd(source)
    }
}