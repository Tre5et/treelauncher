package dev.treset.treelauncher.instances

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.data.InstanceData
import dev.treset.treelauncher.backend.data.manifest.Component
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.generic.ComboBox
import dev.treset.treelauncher.generic.IconButton
import dev.treset.treelauncher.generic.Text
import dev.treset.treelauncher.generic.TitledColumn
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.icons
import java.io.IOException

@Composable
fun InstanceComponentChanger(
    instance: InstanceData,
    type: InstanceDetails,
    allowUnselect: Boolean = false,
    redrawSelected: () -> Unit
) {
    var components: Array<out Component> by remember { mutableStateOf(emptyArray()) }

    var current: Component? by remember(type) { mutableStateOf(null) }

    var selected: Component? by remember(current) { mutableStateOf(current) }

    val loadCurrent = {
        current = when (type) {
            InstanceDetails.SAVES -> instance.savesComponent
            InstanceDetails.RESOURCE_PACKS -> instance.resourcepacksComponent
            InstanceDetails.OPTIONS -> instance.optionsComponent
            InstanceDetails.MODS -> instance.modsComponent
            else -> null
        }
    }

    LaunchedEffect(type) {
        components = when(type) {
            InstanceDetails.SAVES -> AppContext.files.savesComponents.toTypedArray()
            InstanceDetails.RESOURCE_PACKS -> AppContext.files.resourcepackComponents.toTypedArray()
            InstanceDetails.OPTIONS -> AppContext.files.optionsComponents.toTypedArray()
            InstanceDetails.MODS -> AppContext.files.modsComponents.toTypedArray()
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
                Text(text = Strings.manager.instance.change.activeTitle(type, current?.name))
                current?.let {
                    IconButton(
                        onClick = {
                            LauncherFile.of(it.directory).open()
                        },
                        icon = icons().folder,
                        size = 32.dp,
                        tooltip = Strings.selector.component.openFolder()
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
                placeholder = Strings.manager.instance.change.noComponent(),
                selected = selected,
                toDisplayString = { name },
                loading = components.isEmpty()
            )
            IconButton(
                onClick = {
                    try {
                        when (type) {
                            InstanceDetails.SAVES -> {
                                selected?.id?.let { id ->
                                    instance.instance.savesComponent = id
                                    instance.reloadSavesComponent(AppContext.files)
                                }
                            }

                            InstanceDetails.RESOURCE_PACKS -> {
                                selected?.id?.let { id ->
                                    instance.instance.resourcepacksComponent = id
                                    instance.reloadResourcepacksComponent(AppContext.files)
                                }
                            }

                            InstanceDetails.OPTIONS -> {
                                selected?.id?.let { id ->
                                    instance.instance.optionsComponent = id
                                    instance.reloadOptionsComponent(AppContext.files)
                                }
                            }

                            InstanceDetails.MODS -> {
                                instance.instance.modsComponent = selected?.id
                                instance.reloadModsComponent(AppContext.files)
                            }

                            else -> {}
                        }
                        loadCurrent()
                        instance.instance.write()
                        redrawSelected()
                    } catch (e: IOException) {
                        AppContext.error(e)
                    }
                },
                icon = icons().change,
                enabled = (allowUnselect || selected != null) && selected != current,
                tooltip = Strings.changer.apply()
            )
        }
    }
}