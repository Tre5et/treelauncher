package dev.treset.treelauncher.components.resourcepacks

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import dev.treset.treelauncher.generic.IconButton
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.icons

@Composable
fun SharedResourcepacksData.ResourcepacksActionBar() {
    IconButton(
        onClick = {
            showAdd.value = true
        },
        icon = icons().add,
        size = 32.dp,
        tooltip = Strings.manager.saves.tooltipAdd()
    )
}