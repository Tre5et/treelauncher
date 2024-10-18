package dev.treset.treelauncher.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.data.InstanceData
import dev.treset.treelauncher.backend.launching.ResourceManager
import dev.treset.treelauncher.backend.util.exception.FileLoadException
import dev.treset.treelauncher.generic.*
import dev.treset.treelauncher.localization.strings
import java.io.IOException

private enum class CleanupState(
    val title: String,
    val content: String,
    val buttonRow: @Composable (close: () -> Unit) -> Unit
) {
    RUNNING(
        title = strings().fixFiles.runningTitle(),
        content = strings().fixFiles.runningMessage(),
        buttonRow = {}
    ),
    SUCCESS(
        title = strings().fixFiles.successTitle(),
        content = strings().fixFiles.successMessage(),
        buttonRow = {
            Button(
                onClick = it,
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(strings().fixFiles.close())
            }
        }
    ),
    FAILURE(
        title = strings().fixFiles.failureTitle(),
        content = strings().fixFiles.failureMessage(),
        buttonRow = {
            Button(
                onClick = it,
                color = MaterialTheme.colorScheme.error
            ) {
                Text(strings().fixFiles.close())
            }
        }
    )
}

@Composable
fun FixFiles() {
    var showPopup by remember { mutableStateOf(false) }
    var cleanupStatus: CleanupState? by remember { mutableStateOf(null) }

    var notification: NotificationData? by remember { mutableStateOf(null) }

    val errorColor = MaterialTheme.colorScheme.error
    LaunchedEffect(AppContext.files) {
        AppContext.files.reloadMain()
        if(AppContext.files.mainManifest.activeInstance != null) {
            notification = NotificationData(
                color = errorColor,
                onClick = {
                    showPopup = true
                },
                content = {
                    Text(strings().fixFiles.notification())
                }
            ).also { AppContext.addNotification(it) }
        }
    }

    if(showPopup) {
        PopupOverlay(
            type = PopupType.WARNING,
            titleRow = { Text(strings().fixFiles.title()) },
            content = {
                Text(strings().fixFiles.message())
            },
            buttonRow = {
                Button(
                    onClick = { showPopup = false },
                    color = MaterialTheme.colorScheme.error
                ) {
                    Text(strings().fixFiles.cancel())
                }

                Button(
                    onClick = {
                        cleanupStatus = CleanupState.RUNNING
                        showPopup = false

                        try {
                            AppContext.files.reload()
                        } catch (e: IOException) {
                            AppContext.error(e)
                            cleanupStatus = CleanupState.FAILURE
                            return@Button
                        }

                        val instance = AppContext.files.instanceComponents.firstOrNull {
                            it.id == AppContext.files.mainManifest.activeInstance
                        } ?: run {
                            AppContext.error(IOException("Unable to cleanup old instance: instance not found"))
                            cleanupStatus = CleanupState.FAILURE
                            return@Button
                        }

                        val instanceData = try {
                            InstanceData.of(instance, AppContext.files)
                        } catch (e: FileLoadException) {
                            AppContext.error(e)
                            cleanupStatus = CleanupState.FAILURE
                            return@Button
                        }

                        val resourceManager = ResourceManager(instanceData)

                        try {
                            resourceManager.cleanupResources()
                        } catch (e: IOException) {
                            AppContext.error(e)
                            cleanupStatus = CleanupState.FAILURE
                            return@Button
                        }

                        AppContext.files.mainManifest.activeInstance = null
                        try {
                            AppContext.files.mainManifest.write()
                        } catch (e: IOException) {
                            AppContext.error(e)
                            AppContext.files.mainManifest.activeInstance = instance.id
                            cleanupStatus = CleanupState.FAILURE
                            return@Button
                        }

                        cleanupStatus = CleanupState.SUCCESS
                        notification?.let { AppContext.dismissNotification(it) }
                    },
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(strings().fixFiles.confirm())
                }
            }
        )
    }

    cleanupStatus?.let {
        PopupOverlay(
            titleRow = { Text(it.title) },
            content = {
                Text(it.content)
            },
            buttonRow = { it.buttonRow { cleanupStatus = null } }
        )
    }
}