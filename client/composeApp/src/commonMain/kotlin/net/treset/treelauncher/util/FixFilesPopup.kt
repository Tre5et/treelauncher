package net.treset.treelauncher.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextAlign
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.app
import net.treset.treelauncher.backend.data.InstanceData
import net.treset.treelauncher.backend.launching.ResourceManager
import net.treset.treelauncher.backend.util.exception.FileLoadException
import net.treset.treelauncher.backend.util.exception.GameResourceException
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.generic.Button
import net.treset.treelauncher.generic.PopupOverlay
import net.treset.treelauncher.generic.PopupType
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
fun FixFilesPopup(
    appContext: AppContext
) {
    var showPopup by remember { mutableStateOf(false) }
    var cleanupStatus: CleanupState? by remember { mutableStateOf(null) }

    LaunchedEffect(appContext.files) {
        if(appContext.files.launcherDetails.activeInstance != null) {
            showPopup = true
        }
    }

    if(showPopup) {
        PopupOverlay(
            type = PopupType.WARNING,
            titleRow = { Text(strings().fixFiles.title()) },
            content = {
                Text(
                    strings().fixFiles.message(),
                    textAlign = TextAlign.Center
                )
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
                            appContext.files.reloadAll()
                        } catch (e: FileLoadException) {
                            app().error(e)
                            cleanupStatus = CleanupState.FAILURE
                            return@Button
                        }

                        val instance = appContext.files.instanceComponents.firstOrNull {
                            it.first.id == appContext.files.launcherDetails.activeInstance
                        } ?: run {
                            app().error(IOException("Unable to cleanup old instance: instance not found"))
                            cleanupStatus = CleanupState.FAILURE
                            return@Button
                        }

                        val instanceData = try {
                            InstanceData.of(instance, appContext.files)
                        } catch (e: FileLoadException) {
                            app().error(e)
                            cleanupStatus = CleanupState.FAILURE
                            return@Button
                        }

                        val resourceManager = ResourceManager(instanceData)

                        try {
                            resourceManager.cleanupGameFiles()
                        } catch (e: GameResourceException) {
                            app().error(e)
                            cleanupStatus = CleanupState.FAILURE
                            return@Button
                        }

                        appContext.files.launcherDetails.activeInstance = null
                        try {
                            LauncherFile.of(
                                appContext.files.mainManifest.directory,
                                appContext.files.mainManifest.details
                            ).write(appContext.files.launcherDetails)
                        } catch (e: IOException) {
                            app().error(e)
                            appContext.files.launcherDetails.activeInstance = instance.first.id
                            cleanupStatus = CleanupState.FAILURE
                            return@Button
                        }

                        cleanupStatus = CleanupState.SUCCESS
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
                Text(
                    it.content,
                    textAlign = TextAlign.Center
                )
            },
            buttonRow = { it.buttonRow { cleanupStatus = null } }
        )
    }
}