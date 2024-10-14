package net.treset.treelauncher.instances

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.treset.treelauncher.AppContextData
import net.treset.treelauncher.backend.data.InstanceData
import net.treset.treelauncher.backend.data.manifest.ComponentManifest
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.generic.ComboBox
import net.treset.treelauncher.generic.IconButton
import net.treset.treelauncher.generic.Text
import net.treset.treelauncher.generic.TitledColumn
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.icons

@Composable
fun InstanceComponentChanger(
    instance: InstanceData,
    type: InstanceDetails,
    allowUnselect: Boolean = false,
    appContext: AppContextData,
    redrawSelected: () -> Unit
) {
    var components: Array<ComponentManifest> by remember { mutableStateOf(emptyArray()) }

    var current: ComponentManifest? by remember(type) { mutableStateOf(null) }

    var selected: ComponentManifest? by remember(current) { mutableStateOf(current) }

    val loadCurrent = {
        current = when (type) {
            InstanceDetails.SAVES -> instance.savesComponent
            InstanceDetails.RESOURCE_PACKS -> instance.resourcepacksComponent
            InstanceDetails.OPTIONS -> instance.optionsComponent
            InstanceDetails.MODS -> instance.modsComponent?.first
            else -> null
        }
    }

    LaunchedEffect(type) {
        components = when(type) {
            InstanceDetails.SAVES -> appContext.files.savesComponents
            InstanceDetails.RESOURCE_PACKS -> appContext.files.resourcepackComponents
            InstanceDetails.OPTIONS -> appContext.files.optionsComponents
            InstanceDetails.MODS -> appContext.files.modsComponents.map { it.first }.toTypedArray()
            else -> emptyArray()
        }

        loadCurrent()
    }

    TitledColumn(
        headerContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(text = strings().manager.instance.change.activeTitle(type, current?.name))
                current?.let {
                    IconButton(
                        onClick = {
                            LauncherFile.of(it.directory).open()
                        },
                        icon = icons().folder,
                        size = 32.dp,
                        tooltip = strings().selector.component.openFolder()
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
                allowUnselect = allowUnselect,
                placeholder = strings().manager.instance.change.noComponent(),
                selected = selected,
                toDisplayString = { name },
                loading = components.isEmpty()
            )
            IconButton(
                onClick = {
                    when(type) {
                        InstanceDetails.SAVES -> {
                            selected?.id?.let { id ->
                                instance.instance.second.savesComponent = id
                                instance.reloadSavesComponent(appContext.files)
                            }
                        }
                        InstanceDetails.RESOURCE_PACKS -> {
                            selected?.id?.let { id ->
                                instance.instance.second.resourcepacksComponent = id
                                instance.reloadResourcepacksComponent(appContext.files)
                            }
                        }
                        InstanceDetails.OPTIONS -> {
                            selected?.id?.let { id ->
                                instance.instance.second.optionsComponent = id
                                instance.reloadOptionsComponent(appContext.files)
                            }
                        }
                        InstanceDetails.MODS -> {
                            instance.instance.second.modsComponent = selected?.id
                            instance.reloadModsComponent(appContext.files)
                        }
                        else -> {}
                    }
                    loadCurrent()
                    LauncherFile.of(instance.instance.first.directory, instance.instance.first.details).write(instance.instance.second)
                    redrawSelected()
                },
                icon = icons().change,
                enabled = (allowUnselect || selected != null) && selected != current,
                tooltip = strings().changer.apply()
            )
        }
    }
}