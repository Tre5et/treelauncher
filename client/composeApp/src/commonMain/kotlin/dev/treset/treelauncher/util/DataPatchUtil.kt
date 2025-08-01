package dev.treset.treelauncher.util

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.app
import dev.treset.treelauncher.backend.data.patcher.DataPatcher
import dev.treset.treelauncher.backend.util.Status
import dev.treset.treelauncher.generic.*
import dev.treset.treelauncher.localization.Strings
import java.io.IOException

@Composable
fun DataPatcher(
    content: @Composable (recheck: () -> Unit) -> Unit
) {
    var recheck by remember { mutableStateOf(0) }

    val dataPatcher = remember(recheck) { DataPatcher() }
    var upgraded by remember(recheck) { mutableStateOf(!dataPatcher.upgradeNeeded()) }
    var error by remember(recheck) { mutableStateOf<Exception?>(null) }
    var status: List<Status> by remember { mutableStateOf(emptyList()) }
    var backup by remember(recheck) { mutableStateOf(true) }
    var fullBackup by remember(recheck) { mutableStateOf(false) }


    if(error != null) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(Strings.error.severeTitle()) },
            text = {
                Text(
                    Strings.error.severeMessage(error!!),
                    textAlign = TextAlign.Start
                )
            },
            containerColor = MaterialTheme.colorScheme.errorContainer,
            confirmButton = {
                Button(
                    onClick = { app().exit(force = true) },
                    color = MaterialTheme.colorScheme.error
                ) {
                    Text(Strings.error.severeClose())
                }
            }
        )
    } else if(upgraded) {
        content { recheck++ }
    } else {
        if(status.isNotEmpty()) {
            StatusPopup(status)
        } else {
            PopupOverlay(
                titleRow = { Text(Strings.launcher.patch.title()) },
                buttonRow = {
                    Button(
                        onClick = {
                            Thread {
                                try {
                                    dataPatcher.performUpgrade(backup, fullBackup) { status = it }
                                    AppContext.files.reload()
                                    status = emptyList()
                                    upgraded = true
                                } catch (e: Exception) {
                                    error = IOException("Failed to upgrade launcher data. RETRY MAY CORRUPT USER DATA!", e)
                                    AppContext.severeError(e)
                                }
                            }.start()
                        }
                    ) {
                        Text(Strings.launcher.patch.start())
                    }
                }
            ) {
                Text(Strings.launcher.patch.message())
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    TitledCheckBox(
                        checked = backup,
                        onCheckedChange = {
                            backup = it
                        },
                        title = Strings.launcher.patch.backup()
                    )
                    if (backup) {
                        TitledCheckBox(
                            checked = !fullBackup,
                            onCheckedChange = {
                                fullBackup = !it
                            },
                            title = Strings.launcher.patch.fullBackup()
                        )
                        Text(
                            Strings.launcher.patch.fullBackupHint(),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}