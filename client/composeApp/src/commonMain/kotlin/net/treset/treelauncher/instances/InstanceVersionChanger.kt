package net.treset.treelauncher.instances

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.treelauncher.AppContextData
import net.treset.treelauncher.backend.creation.VersionCreator
import net.treset.treelauncher.backend.data.InstanceData
import net.treset.treelauncher.backend.util.CreationStatus
import net.treset.treelauncher.backend.util.exception.ComponentCreationException
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.creation.CreationPopup
import net.treset.treelauncher.generic.*
import net.treset.treelauncher.localization.strings

@Composable
fun InstanceVersionChanger(
    instance: InstanceData,
    appContext: AppContextData,
    redrawCurrent: () -> Unit
) {
    var creator: VersionCreator? by remember { mutableStateOf(null) }
    var creationStatus: CreationStatus? by remember { mutableStateOf(null) }
    var showDone by remember { mutableStateOf(false) }
    var showFailed: Exception? by remember { mutableStateOf(null) }

    TitledColumn(
        title = strings().manager.instance.change.activeTitle(InstanceDetails.VERSION, instance.versionComponents[0].first.name),
    ) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            VersionSelector(
                onDone = {
                     creator = it
                },
                defaultVersionId = instance.versionComponents[0].second.versionNumber,
                defaultVersionType = when(instance.versionComponents[0].second.versionType) {
                    "fabric" -> VersionType.FABRIC
                    "forge" -> VersionType.FORGE
                    else -> VersionType.VANILLA
                },
                defaultLoaderVersion = instance.versionComponents[0].second.loaderVersion,
            )
        }
    }

    creator?.let {
        PopupOverlay(
            type = PopupType.WARNING,
            titleRow = { Text(strings().manager.instance.change.title()) },
            content = { Text(strings().manager.instance.change.message()) },
            buttonRow = {
                Button(
                    onClick = {
                        creator = null
                    },
                    color = MaterialTheme.colorScheme.error
                ) {
                    Text(strings().manager.instance.change.cancel())
                }
                Button(
                    onClick = {
                        it.statusCallback = { status ->
                            creationStatus = status
                        }
                        Thread {
                            val id = try {
                                it.execute()
                            } catch(e: ComponentCreationException) {
                                LOGGER.error(e) { "Failed to create Version" }
                                showFailed = e
                                creationStatus = null
                                return@Thread
                            }
                            showDone = true
                            instance.instance.second.versionComponent = id
                            LauncherFile.of(
                                instance.instance.first.directory,
                                instance.instance.first.details
                            ).write(instance.instance.second)
                            appContext.files.reloadVersionManifest()
                            appContext.files.reloadVersionComponents()
                            appContext.files.reloadJavaManifest()
                            appContext.files.reloadJavaComponents()
                            instance.reloadVersionComponent(appContext.files)
                            instance.reloadJavaComponent(appContext.files)
                            redrawCurrent()
                            creationStatus = null
                        }.start()
                        creator = null
                    }
                ) {
                    Text(strings().manager.instance.change.confirm())
                }
            }
        )
    }

    creationStatus?.let {
        CreationPopup(it)
    }

    if(showDone) {
        PopupOverlay(
            type = PopupType.SUCCESS,
            titleRow = { Text(strings().manager.instance.change.success()) },
            buttonRow = {
                Button(
                    onClick = {
                        showDone = false
                    }
                ) {
                    Text(strings().manager.instance.change.back())
                }
            }
        )
    }

    showFailed?.let {
        PopupOverlay(
            type = PopupType.ERROR,
            titleRow = { Text(strings().manager.instance.change.failure()) },
            content = { Text(it.toString()) },
            buttonRow = {
                Button(
                    onClick = {
                        showFailed = null
                    }
                ) {
                    Text(strings().manager.instance.change.back())
                }
            }
        )
    }
}

private val LOGGER = KotlinLogging.logger {}
