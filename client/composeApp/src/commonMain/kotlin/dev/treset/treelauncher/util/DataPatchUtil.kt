package dev.treset.treelauncher.util

import androidx.compose.runtime.*
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.data.patcher.DataPatcher
import dev.treset.treelauncher.backend.util.Status
import dev.treset.treelauncher.generic.*
import dev.treset.treelauncher.localization.strings
import java.io.IOException

@Composable
fun DataPatcher(
    content: @Composable (recheck: () -> Unit) -> Unit
) {
    var recheck by remember { mutableStateOf(0) }

    val dataPatcher = remember(recheck) { DataPatcher() }
    var upgraded by remember(recheck) { mutableStateOf(!dataPatcher.upgradeNeeded()) }
    var error by remember(recheck) { mutableStateOf<Exception?>(null) }
    var status: Status? by remember { mutableStateOf(null) }
    var backup by remember(recheck) { mutableStateOf(true) }


    if(error != null) {
        AppContext.severeError(error!!)
    } else if(upgraded) {
        content { recheck++ }
    } else {
        status?.let {
            StatusPopup(it)
        } ?: PopupOverlay(
            titleRow = { Text(strings().launcher.patch.title()) },
            buttonRow = {
                Button(
                    onClick = {
                        Thread {
                            try {
                                dataPatcher.performUpgrade(backup) { state -> status = state }
                                AppContext.files.reload()
                            } catch (e: Exception) {
                                error = IOException("Failed to upgrade launcher data", e)
                            }
                            upgraded = true
                        }.start()
                    }
                ) {
                    Text(strings().launcher.patch.start())
                }
            }
        ) {
            Text(strings().launcher.patch.message())
            TitledCheckBox(
                checked = backup,
                onCheckedChange = {
                    backup = it
                },
                title = strings().launcher.patch.backup()
            )
            Text(strings().launcher.patch.backupHint())
        }
    }
}