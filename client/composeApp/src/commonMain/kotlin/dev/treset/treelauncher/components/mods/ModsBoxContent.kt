package dev.treset.treelauncher.components.mods

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.util.sort.LauncherModSortProviders
import dev.treset.treelauncher.generic.IconButton
import dev.treset.treelauncher.generic.ListDisplayBox
import dev.treset.treelauncher.generic.SortBox
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.icons
import dev.treset.treelauncher.util.ListDisplay

@Composable
fun SharedModsData.ModsBoxContent(scope: BoxScope) {
    if(!settingsOpen.value && !showSearch.value && editingMod.value == null) {
        SortBox(
            sorts = LauncherModSortProviders,
            modifier = with(scope) { Modifier.align(Alignment.CenterEnd) },
            sort = component.sort,
        )

        ListDisplayBox(
            displays = ListDisplay.entries,
            selected = component.listDisplay,
            default = AppContext.files.modsManifest.defaultListDisplay,
            modifier = with(scope) {
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 18.dp)
            }
        )
    } else if((showSearch.value || editingMod.value != null) && !settingsOpen.value) {
        Box(
            modifier = with(scope) { Modifier.align(Alignment.CenterStart) }
        ) {
            IconButton(
                onClick = {
                    showSearch.value = false
                    editingMod.value = null
                },
                icon = icons().back,
                size = 32.dp,
                tooltip = Strings.manager.mods.addMods.back()
            )
        }
    }
}