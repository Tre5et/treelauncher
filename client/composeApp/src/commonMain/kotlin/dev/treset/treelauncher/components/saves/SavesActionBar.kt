package dev.treset.treelauncher.components.saves

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.util.QuickPlayData
import dev.treset.treelauncher.generic.IconButton
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.icons

@Composable
fun SharedSavesData.SavesActionBar() {
    if(!settingsOpen.value && !showAdd.value) {
        selectedSave.value?.let {
            IconButton(
                onClick = {
                    quickPlayData.value = QuickPlayData(
                        QuickPlayData.Type.WORLD,
                        it.fileName
                    )
                },
                painter = icons().play,
                size = 32.dp,
                highlighted = true,
                tooltip = Strings.selector.saves.play.button(),
                enabled = AppContext.runningInstance == null
            )
        }

        selectedServer.value?.let {
            IconButton(
                onClick = {
                    quickPlayData.value = QuickPlayData(
                        QuickPlayData.Type.SERVER,
                        it.ip
                    )
                },
                painter = icons().play,
                size = 32.dp,
                highlighted = true,
                tooltip = Strings.selector.saves.play.button(),
                enabled = AppContext.runningInstance == null
            )
        }

        IconButton(
            onClick = {
                showAdd.value = true
            },
            icon = icons().add,
            size = 32.dp,
            tooltip = Strings.manager.saves.tooltipAdd()
        )
    }
}