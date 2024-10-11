package net.treset.treelauncher.backend.launching.resources

import net.treset.mcdl.saves.Save
import net.treset.mcdl.saves.Server
import net.treset.treelauncher.backend.util.file.LauncherFile
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