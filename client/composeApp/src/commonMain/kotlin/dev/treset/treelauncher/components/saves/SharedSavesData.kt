package dev.treset.treelauncher.components.saves

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import dev.treset.mcdl.saves.Save
import dev.treset.mcdl.saves.Server
import dev.treset.treelauncher.backend.data.manifest.SavesComponent
import dev.treset.treelauncher.backend.launching.resources.SavesDisplayData
import dev.treset.treelauncher.backend.util.QuickPlayData
import dev.treset.treelauncher.components.SharedAddableComponentData

class SharedSavesData(
    component: SavesComponent,
    reload: () -> Unit,
    var displayData: SavesDisplayData = SavesDisplayData(),
    val selectedSave: MutableState<Save?> = mutableStateOf(null),
    val selectedServer: MutableState<Server?> = mutableStateOf(null),
    val quickPlayData: MutableState<QuickPlayData?> = mutableStateOf(null),
) : SharedAddableComponentData<SavesComponent>(
    component,
    reload
) {
    companion object {
        fun of(component: SavesComponent, reload: () -> Unit): SharedSavesData = SharedSavesData(component, reload)
    }
}