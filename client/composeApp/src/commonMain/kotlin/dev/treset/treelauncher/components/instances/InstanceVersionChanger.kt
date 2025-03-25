package dev.treset.treelauncher.components.instances

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.data.manifest.VersionComponent
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.generic.*
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.icons
import java.io.IOException

@Composable
fun SharedInstanceData.InstanceVersionChanger() {
    var execute: (() -> VersionComponent)? by remember { mutableStateOf(null) }
    var showDone by remember { mutableStateOf(false) }
    var showFailed: Exception? by remember { mutableStateOf(null) }

    DisposableEffect(Unit) {
        headerContent.value = {
            Text(
                Strings.manager.instance.change.activeTitle(InstanceDetailsType.VERSION, component.versionComponents.value[0].name.value)
            )
            IconButton(
                onClick = {
                    LauncherFile.of(component.versionComponents.value[0].directory).open()
                },
                icon = icons().folder,
                size = 32.dp,
                tooltip = Strings.selector.component.openFolder()
            )
        }

        onDispose {
            headerContent.value = {}
        }
    }

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
                    execute = null
                    showDone = true
                } catch (e: IOException) {
                    AppContext.error(e)
                }
                component.versionId.value = it.id.value
                try {
                    component.write()
                } catch (e: IOException) {
                    AppContext.error(e)
                }

            },
            defaultVersionId = component.versionComponents.value[0].versionNumber.value,
            defaultVersionType = when(component.versionComponents.value[0].versionType.value) {
                "fabric" -> VersionType.FABRIC
                "forge" -> VersionType.FORGE
                "quilt" -> VersionType.QUILT
                "neoforge" -> VersionType.NEO_FORGE
                else -> VersionType.VANILLA
            },
            defaultLoaderVersion = component.versionComponents.value[0].loaderVersion.value,
        )
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
                        Thread {
                            try {
                                it()
                            } catch (e: IOException) {
                                showFailed = e
                            }
                        }.start()
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
