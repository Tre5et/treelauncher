package dev.treset.treelauncher.generic

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import dev.treset.treelauncher.backend.data.manifest.Component
import dev.treset.treelauncher.localization.Strings

@Composable
fun RenamePopup(
    manifest: Component,
    editValid: (String) -> Boolean,
    onDone: (String?) -> Unit
) {

    var tfName: String by remember { mutableStateOf(manifest.name.value) }
    PopupOverlay(
        titleRow = { Text(Strings.selector.component.rename.title()) },
        content = {
            TextBox(
                tfName,
                {
                    tfName = it
                },
                placeholder = Strings.selector.component.rename.prompt(),
                modifier = Modifier.onKeyEvent {
                    if(it.key == Key.Enter && editValid(tfName)) {
                        manifest.name.value = tfName
                        onDone(tfName)
                    }
                    false
                }
            )
        },
        buttonRow = {
            Button(
                onClick = {
                    onDone(null)
                },
                color = MaterialTheme.colorScheme.error
            ) {
                Text(Strings.selector.component.rename.cancel())
            }
            Button(
                onClick = {
                    manifest.name.value = tfName
                    onDone(tfName)
                },
                enabled = editValid(tfName)
            ) {
                Text(Strings.selector.component.rename.confirm())
            }
        }
    )
}