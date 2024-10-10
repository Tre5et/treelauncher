package net.treset.treelauncher.util

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.data.patcher.DataPatcher
import net.treset.treelauncher.backend.util.Status
import net.treset.treelauncher.generic.*
import net.treset.treelauncher.localization.strings
import java.io.IOException

@Composable
fun DataPatcher(
    content: @Composable () -> Unit
) {
    val dataPatcher = remember { DataPatcher() }

    var upgraded by rememberSaveable{ mutableStateOf(!dataPatcher.upgradeNeeded()) }
    var error by remember { mutableStateOf<Exception?>(null) }
    var status: Status? by remember { mutableStateOf(null) }
    var backup by remember { mutableStateOf(true) }


    if(error != null) {
        AppContext.severeError(error!!)
    } else if(upgraded) {
        content()
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