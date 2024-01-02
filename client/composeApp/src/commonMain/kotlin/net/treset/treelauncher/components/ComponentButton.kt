package net.treset.treelauncher.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.treelauncher.generic.SelectorButton

@Composable
fun ComponentButton(
    component: LauncherManifest,
    selected: Boolean,
    onClick: () -> Unit
) {
    SelectorButton(
        selected = selected,
        onClick = onClick,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                component.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(component.id)
        }
    }
}