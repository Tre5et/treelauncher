package net.treset.treelauncher.components

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import net.treset.mc_version_loader.launcher.LauncherInstanceDetails
import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.generic.PopupOverlay
import net.treset.treelauncher.generic.PopupType
import net.treset.treelauncher.localization.strings

@Composable
fun DeletePopup(
    component: LauncherManifest,
    appContext: AppContext,
    checkHasComponent: (LauncherInstanceDetails) -> Boolean,
    onClose: () -> Unit,
    onConfirm: () -> Unit
) {
    val usedBy = remember(component) {
        appContext.files.instanceComponents
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(strings().selector.component.delete.confirm())
            }
        }
    )
}