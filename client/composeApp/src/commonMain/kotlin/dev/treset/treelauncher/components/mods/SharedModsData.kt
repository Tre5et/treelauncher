package dev.treset.treelauncher.components.mods

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import dev.treset.treelauncher.backend.data.manifest.LauncherMod
import dev.treset.treelauncher.backend.data.manifest.ModsComponent
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.components.SharedComponentData

class SharedModsData(
    component: ModsComponent,
    reload: () -> Unit,
    val showSearch: MutableState<Boolean> = mutableStateOf(false),
    val checkUpdates: MutableState<Int> = mutableStateOf(0),
    val editingMod: MutableState<LauncherMod?> = mutableStateOf(null),
    val droppedFile: MutableState<LauncherFile?> = mutableStateOf(null)
) : SharedComponentData<ModsComponent>(
    component,
    reload
) {
    companion object {
        fun of(component: ModsComponent, reload: () -> Unit) = SharedModsData(component, reload)
    }
}