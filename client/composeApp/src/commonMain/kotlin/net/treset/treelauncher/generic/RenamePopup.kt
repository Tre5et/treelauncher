package net.treset.treelauncher.generic

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.treelauncher.localization.strings

@Composable
fun RenamePopup(
    manifest: LauncherManifest,
    editValid: (String) -> Boolean,
    onDone: (String?) -> Unit
) {

    var tfName: String by remember { mutableStateOf(manifest.name) }
    PopupOverlay(
        titleRow = { Text(strings().selector.component.rename.title()) },
        content = {
            TextBox(
                tfName,
                {
                    tfName = it
                },
                placeholder = strings().selector.component.rename.prompt()
            )
        },
        buttonRow = {
            Button(
                onClick = {
                    onDone(null)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(strings().selector.component.rename.cancel())
            }
            Button(
                onClick = {
                    manifest.name = tfName
                    onDone(tfName)
                },
                enabled = editValid(tfName)
            ) {
                Text(strings().selector.component.rename.confirm())
            }
        }
    )
}