package dev.treset.treelauncher.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.data.manifest.Component
import dev.treset.treelauncher.backend.data.manifest.InstanceComponent
import dev.treset.treelauncher.generic.Button
import dev.treset.treelauncher.generic.PopupOverlay
import dev.treset.treelauncher.generic.PopupType
import dev.treset.treelauncher.generic.Text
import dev.treset.treelauncher.localization.Strings

@Composable
fun DeletePopup(
    component: Component,
    checkHasComponent: (InstanceComponent) -> Boolean,
    onClose: () -> Unit,
    onConfirm: () -> Unit
) {
    val usedBy = remember(component) {
        AppContext.files.instanceComponents
            .firstOrNull {
                checkHasComponent(it)
            }
    }

    usedBy?.let {
        PopupOverlay(
            type = PopupType.ERROR,
            titleRow = { Text(Strings.selector.component.delete.unableTitle()) },
            content = { Text(Strings.selector.component.delete.unableMessage(it)) },
            buttonRow = {
                Button (
                    onClick = onClose
                ) {
                    Text(Strings.selector.component.delete.unableClose())
                }
            }
        )
    } ?: PopupOverlay(
        type = PopupType.WARNING,
        titleRow = { Text(Strings.selector.component.delete.title()) },
        content = { Text(Strings.selector.component.delete.message()) },
        buttonRow = {
            Button(
                onClick = onClose,
                content = { Text(Strings.selector.component.delete.cancel()) }
            )
            Button(
                onClick = onConfirm,
                color = MaterialTheme.colorScheme.error
            ) {
                Text(Strings.selector.component.delete.confirm())
            }
        }
    )
}