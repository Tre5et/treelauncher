package net.treset.treelauncher.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.util.exception.FileLoadException
import net.treset.treelauncher.generic.*
import net.treset.treelauncher.localization.strings

@Composable
fun Cleanup() {
    var showCleanup by remember { mutableStateOf(false) }

    Button(
        onClick = {
            showCleanup = true
        },
        enabled = AppContext.runningInstance == null
    ) {
        Text(
            strings().settings.cleanup.button()
        )
    }

    if(showCleanup) {
        var includeLibraries by remember { mutableStateOf(true) }
        var state: Int by remember { mutableStateOf(0) }

        when(state) {
            0 -> PopupOverlay(
                titleRow = { Text(strings().settings.cleanup.title()) },
                content = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            strings().settings.cleanup.message(),
                        )

                        TitledCheckBox(
                            title = strings().settings.cleanup.libraries(),
                            checked = includeLibraries,
                            onCheckedChange = {
                                includeLibraries = it
                            }
                        )
                    }
                },
                buttonRow = {
                    Button(
                        onClick = {
                            showCleanup = false
                        },
                        color = MaterialTheme.colorScheme.error
                    ) {
                        Text(strings().settings.cleanup.cancel())
                    }
                    Button(
                        onClick = {
                            Thread {
                                try {
                                    AppContext.files.cleanupVersions(includeLibraries)
                                    state = 2
                                } catch(e: FileLoadException) {
                                    AppContext.error(e)
                                    state = 3
                                }
                            }.start()
                            state = 1
                        }
                    ) {
                        Text(strings().settings.cleanup.confirm())
                    }
                }
            )
            1 -> PopupOverlay(
                titleRow = { Text(strings().settings.cleanup.deleting()) }
            )
            2 -> PopupOverlay(
                type = PopupType.SUCCESS,
                titleRow = { Text(strings().settings.cleanup.success()) },
                buttonRow = {
                    Button(
                        onClick = {
                            showCleanup = false
                        }
                    ) {
                        Text(strings().settings.cleanup.close())
                    }
                }
            )
            3 -> PopupOverlay(
                type = PopupType.ERROR,
                titleRow = { Text(strings().settings.cleanup.failureTitle()) },
                content = { Text(strings().settings.cleanup.failureMessage()) },
                buttonRow = {
                    Button(
                        onClick = {
                            showCleanup = false
                        }
                    ) {
                        Text(strings().settings.cleanup.close())
                    }
                }
            )
        }
    }
}