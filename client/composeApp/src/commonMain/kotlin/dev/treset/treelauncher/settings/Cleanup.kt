package dev.treset.treelauncher.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.util.exception.FileLoadException
import dev.treset.treelauncher.generic.*
import dev.treset.treelauncher.localization.Strings

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
            Strings.settings.cleanup.button()
        )
    }

    if(showCleanup) {
        var includeLibraries by remember { mutableStateOf(true) }
        var state: Int by remember { mutableStateOf(0) }

        when(state) {
            0 -> PopupOverlay(
                titleRow = { Text(Strings.settings.cleanup.title()) },
                content = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            Strings.settings.cleanup.message(),
                        )

                        TitledCheckBox(
                            title = Strings.settings.cleanup.libraries(),
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
                        Text(Strings.settings.cleanup.cancel())
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
                        Text(Strings.settings.cleanup.confirm())
                    }
                }
            )
            1 -> PopupOverlay(
                titleRow = { Text(Strings.settings.cleanup.deleting()) }
            )
            2 -> PopupOverlay(
                type = PopupType.SUCCESS,
                titleRow = { Text(Strings.settings.cleanup.success()) },
                buttonRow = {
                    Button(
                        onClick = {
                            showCleanup = false
                        }
                    ) {
                        Text(Strings.settings.cleanup.close())
                    }
                }
            )
            3 -> PopupOverlay(
                type = PopupType.ERROR,
                titleRow = { Text(Strings.settings.cleanup.failureTitle()) },
                content = { Text(Strings.settings.cleanup.failureMessage()) },
                buttonRow = {
                    Button(
                        onClick = {
                            showCleanup = false
                        }
                    ) {
                        Text(Strings.settings.cleanup.close())
                    }
                }
            )
        }
    }
}