package dev.treset.treelauncher.instances

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.data.InstanceData
import dev.treset.treelauncher.generic.*
import dev.treset.treelauncher.localization.Strings
import java.io.IOException

@Composable
fun InstanceVersionChanger(
    instance: InstanceData,
    redrawCurrent: () -> Unit
) {
    var execute: (() -> Unit)? by remember { mutableStateOf(null) }
    var showDone by remember { mutableStateOf(false) }
    var showFailed: Exception? by remember { mutableStateOf(null) }

    TitledColumn(
        title = Strings.manager.instance.change.activeTitle(InstanceDetails.VERSION, instance.versionComponents[0].name),
    ) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            VersionSelector(
                onChange = { execute = it },
                onDone = {
                    try {
                        AppContext.files.reloadVersions()
                        AppContext.files.reloadJavas()
                        instance.reloadVersionComponent(AppContext.files)
                        instance.reloadJavaComponent(AppContext.files)
                        showDone = true
                        redrawCurrent()
                    } catch (e: IOException) {
                        AppContext.error(e)
                    }
                },
                defaultVersionId = instance.versionComponents[0].versionNumber,
                defaultVersionType = when(instance.versionComponents[0].versionType) {
                    "fabric" -> VersionType.FABRIC
                    "forge" -> VersionType.FORGE
                    else -> VersionType.VANILLA
                },
                defaultLoaderVersion = instance.versionComponents[0].loaderVersion,
            )
        }
    }

    execute?.let {
        PopupOverlay(
            type = PopupType.WARNING,
            titleRow = { Text(Strings.manager.instance.change.title()) },
            content = { Text(Strings.manager.instance.change.message()) },
            buttonRow = {
                Button(
                    onClick = {
                        execute = null
                    },
                    color = MaterialTheme.colorScheme.error
                ) {
                    Text(Strings.manager.instance.change.cancel())
                }
                Button(
                    onClick = {
                        try {
                            it()
                        } catch (e: IOException) {
                            showFailed = e
                        }
                        execute = null
                    }
                ) {
                    Text(Strings.manager.instance.change.confirm())
                }
            }
        )
    }

    if(showDone) {
        PopupOverlay(
            type = PopupType.SUCCESS,
            titleRow = { Text(Strings.manager.instance.change.success()) },
            buttonRow = {
                Button(
                    onClick = {
                        showDone = false
                    }
                ) {
                    Text(Strings.manager.instance.change.back())
                }
            }
        )
    }

    showFailed?.let {
        PopupOverlay(
            type = PopupType.ERROR,
            titleRow = { Text(Strings.manager.instance.change.failure()) },
            content = { Text(it.toString()) },
            buttonRow = {
                Button(
                    onClick = {
                        showFailed = null
                    }
                ) {
                    Text(Strings.manager.instance.change.back())
                }
            }
        )
    }
}
