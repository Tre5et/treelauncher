package net.treset.treelauncher.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.data.InstanceData
import net.treset.treelauncher.backend.launching.ResourceManager
import net.treset.treelauncher.backend.util.exception.FileLoadException
import net.treset.treelauncher.backend.util.exception.GameResourceException
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.generic.*
import net.treset.treelauncher.localization.strings
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
        if(AppContext.files.launcherDetails.activeInstance != null) {
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
                        } catch (e: FileLoadException) {
                            AppContext.error(e)
                            cleanupStatus = CleanupState.FAILURE
                            return@Button
                        }

                        val instance = AppContext.files.instanceComponents.firstOrNull {
                            it.first.id == AppContext.files.launcherDetails.activeInstance
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
                        } catch (e: GameResourceException) {
                            AppContext.error(e)
                            cleanupStatus = CleanupState.FAILURE
                            return@Button
                        }

                        AppContext.files.launcherDetails.activeInstance = null
                        try {
                            LauncherFile.of(
                                AppContext.files.mainManifest.directory,
                                AppContext.files.mainManifest.details
                            ).write(AppContext.files.launcherDetails)
                        } catch (e: IOException) {
                            AppContext.error(e)
                            AppContext.files.launcherDetails.activeInstance = instance.first.id
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