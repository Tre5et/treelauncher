package net.treset.treelauncher.instances

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.data.InstanceData
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.generic.ComboBox
import net.treset.treelauncher.generic.IconButton
import net.treset.treelauncher.generic.TitledColumn
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.icons

@Composable
fun InstanceComponentChanger(
    instance: InstanceData,
    type: InstanceDetails,
    appContext: AppContext,
    redrawSelected: () -> Unit
) {
    var components: Array<LauncherManifest> by remember { mutableStateOf(emptyArray()) }

    var current: LauncherManifest? by remember { mutableStateOf(null) }

    var selected: LauncherManifest? by remember{ mutableStateOf(current) }

    val loadFirstSelected = {
        current = when (type) {
            InstanceDetails.SAVES -> instance.savesComponent
            InstanceDetails.RESOURCE_PACKS -> instance.resourcepacksComponent
            InstanceDetails.OPTIONS -> instance.optionsComponent
            InstanceDetails.MODS -> instance.modsComponent?.first
            else -> null
        }

        selected = current
    }

    LaunchedEffect(type) {
        components = when(type) {
            InstanceDetails.SAVES -> appContext.files.savesComponents
            InstanceDetails.RESOURCE_PACKS -> appContext.files.resourcepackComponents
            InstanceDetails.OPTIONS -> appContext.files.optionsComponents
            InstanceDetails.MODS -> appContext.files.modsComponents.map { it.first }.toTypedArray()
            else -> emptyArray()
        }

        loadFirstSelected()
    }

    TitledColumn(
        headerContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(text = strings().manager.instance.change.activeTitle(type, current?.name))
                IconButton(
                    onClick = {
                        current?.let {
                            LauncherFile.of(it.directory).open()
                        }
                    }
                ) {
                    Icon(
                        icons().folder,
                        "Open Folder",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        },
    ) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            ComboBox(
                items = components.toList(),
                onSelected = {
                    selected = it
                },
                defaultSelected = selected,
                toDisplayString = { name },
            )
            IconButton(
                onClick = {
                    selected?.let {
                        when(type) {
                            InstanceDetails.SAVES -> {
                                instance.instance.second.savesComponent = it.id
                                instance.reloadSavesComponent(appContext.files)
                            }
                            InstanceDetails.RESOURCE_PACKS -> {
                                instance.instance.second.resourcepacksComponent = it.id
                                instance.reloadResourcepacksComponent(appContext.files)
                            }
                            InstanceDetails.OPTIONS -> {
                                instance.instance.second.optionsComponent = it.id
                                instance.reloadOptionsComponent(appContext.files)
                            }
                            InstanceDetails.MODS -> {
                                instance.instance.second.modsComponent = it.id
                                instance.reloadModsComponent(appContext.files)
                            }
                            else -> {}
                        }
                        loadFirstSelected()
                        LauncherFile.of(instance.instance.first.directory, instance.instance.first.details).write(instance.instance.second)
                        redrawSelected()
                    }
                },
                enabled = selected != null && selected != current
            ) {
                Icon(
                    icons().change,
                    "change"
                )
            }
        }
    }
}