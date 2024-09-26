package net.treset.treelauncher.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.data.LauncherInstanceDetails
import net.treset.treelauncher.backend.data.manifest.Component
import net.treset.treelauncher.generic.Button
import net.treset.treelauncher.generic.PopupOverlay
import net.treset.treelauncher.generic.PopupType
import net.treset.treelauncher.generic.Text
import net.treset.treelauncher.localization.strings

@Composable
fun DeletePopup(
    component: Component,
    checkHasComponent: (LauncherInstanceDetails) -> Boolean,
    onClose: () -> Unit,
    onConfirm: () -> Unit
) {
    val usedBy = remember(component) {
        AppContext.files.instanceComponents
            .firstOrNull {
                checkHasComponent(it.second)
            }
    }

    usedBy?.let {
        PopupOverlay(
            type = PopupType.ERROR,
            titleRow = { Text(strings().selector.component.delete.unableTitle()) },
            content = { Text(strings().selector.component.delete.unableMessage(it.first)) },
            buttonRow = {
                Button (
                    onClick = onClose
                ) {
                    Text(strings().selector.component.delete.unableClose())
                }
            }
        )
    } ?: PopupOverlay(
        type = PopupType.WARNING,
        titleRow = { Text(strings().selector.component.delete.title()) },
        content = { Text(strings().selector.component.delete.message()) },
        buttonRow = {
            Button(
                onClick = onClose,
                content = { Text(strings().selector.component.delete.cancel()) }
            )
            Button(
                onClick = onConfirm,
                color = MaterialTheme.colorScheme.error
            ) {
                Text(strings().selector.component.delete.confirm())
            }
        }
    )
}