package dev.treset.treelauncher.backend.launching.resources

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import dev.treset.mcdl.saves.Save
import dev.treset.mcdl.saves.Server
import dev.treset.treelauncher.backend.util.MutableStateList
import dev.treset.treelauncher.backend.util.MutableStateMap
import dev.treset.treelauncher.backend.util.file.LauncherFile
import java.io.IOException

data class SavesDisplayData(
    val saves: MutableStateMap<Save, LauncherFile>,
    val servers: MutableStateList<Server>,
    val onAdd: MutableState<SavesDisplayData.(source: List<LauncherFile>) -> Unit>,
) {
    constructor() : this(
        mutableStateMapOf(),
        mutableStateListOf(),
        mutableStateOf({ })
    )

    @Throws(IOException::class)
    fun addSaves(source: List<LauncherFile>) {
        (this.onAdd.value)(source)
    }
}