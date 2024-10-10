package net.treset.treelauncher.util

import androidx.compose.material3.MaterialTheme
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.treset.mcdl.json.SerializationException
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.app
import net.treset.treelauncher.backend.update.UpdaterStatus
import net.treset.treelauncher.backend.update.updater
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.generic.Button
import net.treset.treelauncher.generic.PopupData
import net.treset.treelauncher.generic.PopupType
import net.treset.treelauncher.generic.Text
import net.treset.treelauncher.localization.strings
import java.io.IOException

fun onUpdate(
    coroutineScope: CoroutineScope,
    setPopup: (PopupData?) -> Unit
) {
    setPopup(
        PopupData(
            titleRow = { Text(strings().settings.update.checkingTitle()) },
            buttonRow = {
                Button(
                    onClick = { setPopup(null) }
                ) {
                    Text(strings().settings.update.cancel())
                }
            }
        )
    )
    coroutineScope.launch {
        val update = try {
            updater().getUpdate()
        } catch (e: IOException) {
            AppContext.errorIfOnline(e)
            return@launch
        }

        try {
            update.id?.let {
                setPopup(
                    PopupData(
                        titleRow = { Text(strings().settings.update.available()) },
                        content = { Text(strings().settings.update.availableMessage(it, update.message)) },
                        buttonRow = {
                            Button(
                                onClick = { setPopup(null) },
                                color = MaterialTheme.colorScheme.error
                            ) {
                                Text(strings().settings.update.cancel())
                            }

                            Button(
                                onClick = {
                                    setPopup(
                                        PopupData(
                                            titleRow = { Text(strings().settings.update.downloadingTitle()) }
                                        )
                                    )

                                    try {
                                        updater().executeUpdate { amount, total, file ->
                                            setPopup(
                                                PopupData(
                                                    titleRow = { Text(strings().settings.update.downloadingTitle()) },
                                                    content = {
                                                        Text(
                                                            strings().statusDetailsMessage(
                                                                file,
                                                                amount,
                                                                total
                                                            )
                                                        )
                                                    }
                                                )
                                            )
                                        }
                                    } catch(e: IOException) {
                                        setPopup(null)
                                        AppContext.error(e)
                                        return@Button
                                    }

                                    setPopup(
                                        PopupData(
                                            type = PopupType.SUCCESS,
                                            titleRow = { Text(strings().settings.update.successTitle()) },
                                            content = { Text(strings().settings.update.successMessage()) },
                                            buttonRow = {
                                                Button(
                                                    onClick = { setPopup(null) }
                                                ) {
                                                    Text(strings().settings.update.close())
                                                }

                                                Button(
                                                    onClick = {
                                                        app().exit(true)
                                                    }
                                                ) {
                                                    Text(strings().settings.update.successRestart())
                                                }
                                            }
                                        )
                                    )
                                }
                            ) {
                                Text(strings().settings.update.download())
                            }
                        }
                    )
                )
            } ?: run {
                if (update.latest == true) {
                    setPopup(
                        PopupData(
                            type = PopupType.SUCCESS,
                            titleRow = { Text(strings().settings.update.latestTitle()) },
                            content = { Text(strings().settings.update.latestMessage()) },
                            buttonRow = {
                                Button(
                                    onClick = { setPopup(null) }
                                ) {
                                    Text(strings().settings.update.close())
                                }
                            }
                        )
                    )
                } else {
                    setPopup(
                        PopupData(
                            type = PopupType.WARNING,
                            titleRow = { Text(strings().settings.update.unavailableTitle()) },
                            content = { Text(strings().settings.update.unavailableMessage()) },
                            buttonRow = {
                                Button(
                                    onClick = { setPopup(null) }
                                ) {
                                    Text(strings().settings.update.close())
                                }
                            }
                        )
                    )
                }
            }
        } catch (e: IOException) {
            AppContext.error(e)
        }
    }
}

fun checkUpdateOnStart(
    coroutineScope: CoroutineScope,
    setPopup: (PopupData?) -> Unit,
    onDone: () -> Unit
) {
    coroutineScope.launch {
        LOGGER.debug { "Checking for completed updates..." }
        val updateFile = LauncherFile.of("updater.json")
        if(!updateFile.isFile) {
            LOGGER.debug { "No update file found!" }
            setPopup(null)
            onDone()
            return@launch
        }

        val status = try {
            UpdaterStatus.fromJson(updateFile.readText())
        } catch (e: SerializationException) {
            LOGGER.warn(e) { "Failed to read update file" }
            setPopup(null)
            onDone()
            return@launch
        }

        status.exceptions?.let { exs ->
            LOGGER.warn { "Exceptions occurred during update: " + status.message }
            exs.forEach {
                LOGGER.warn { it }
            }
        }

        try {
            updateFile.delete()
        } catch (e: IOException) {
            LOGGER.warn(e) { "Failed to delete update file" }
        }

        setPopup(PopupData(
            type = status.status.type,
            titleRow = { Text(status.status.popupTitle()) },
            content = { Text(status.status.popupMessage()) },
            buttonRow = {
                if(status.status.type == PopupType.ERROR) {
                    Button(
                        onClick = { app().exit(force = true) },
                        color = MaterialTheme.colorScheme.error
                    ) {
                        Text(strings().updater.quit())
                    }
                } else {
                    Button(
                        onClick = {
                            setPopup(null)
                            onDone()
                        }
                    ) {
                        Text(strings().updater.close())
                    }
                }
            }

        ))
    }
}

private val LOGGER = KotlinLogging.logger {  }