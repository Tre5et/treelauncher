package net.treset.treelauncher.backend.launching.resources

import net.treset.mcdl.saves.Save
import net.treset.mcdl.saves.Server
import net.treset.treelauncher.backend.util.file.LauncherFile
import java.io.IOException

data class SavesDisplayData(
    var saves: List<Save>,
    var server: List<Server>,
    private val onAdd: SavesDisplayData.(source: List<LauncherFile>) -> Unit,
) {
    @Throws(IOException::class)
    fun addSave(vararg source: LauncherFile) {
        onAdd(source.toList())
    }
}