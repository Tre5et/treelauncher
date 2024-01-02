package net.treset.treelauncher.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.treelauncher.generic.TitledColumn

@Composable
fun Components(
    title: String,
    components: Array<LauncherManifest>,
    reload: () -> Unit,
    details: @Composable (
        LauncherManifest,
        () -> Unit,
        () -> Unit
    ) -> Unit,
) {
    var selected: LauncherManifest? by remember(components) { mutableStateOf(null) }

    val redrawSelected: () -> Unit = {
        selected?.let {
            selected = null
            selected = it
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {

        TitledColumn(
            title = title,
            modifier = Modifier.padding(12.dp),
            parentModifier = Modifier.fillMaxWidth(1 / 2f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            components.forEach { component ->
                ComponentButton(
                    component = component,
                    selected = component == selected,
                    onClick = {
                        selected = if(component == selected) {
                            null
                        } else {
                            component
                        }
                    }
                )
            }
        }

        selected?.let {
            details(
                it,
                redrawSelected,
                reload
            )
        }
    }
}