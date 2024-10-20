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
) {
    val components = when(type) {
        InstanceDetails.SAVES -> AppContext.files.savesComponents
        InstanceDetails.RESOURCE_PACKS -> AppContext.files.resourcepackComponents
        InstanceDetails.OPTIONS -> AppContext.files.optionsComponents
        InstanceDetails.MODS -> AppContext.files.modsComponents
        else -> mutableListOf()
    }

    val current by when (type) {
        InstanceDetails.SAVES -> instance.savesComponent
        InstanceDetails.RESOURCE_PACKS -> instance.resourcepacksComponent
        InstanceDetails.OPTIONS -> instance.optionsComponent
        InstanceDetails.MODS -> instance.modsComponent
        else -> mutableStateOf(null)
    }

    var selected: Component? by remember(current) { mutableStateOf(current) }

    TitledColumn(
        headerContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(text = Strings.manager.instance.change.activeTitle(type, current?.name?.value))
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
                toDisplayString = { name.value },
                loading = components.isEmpty()
            )
            IconButton(
                onClick = {
                    try {
                        when (type) {
                            InstanceDetails.SAVES -> {
                                selected?.id?.value?.let { id ->
                                    instance.instance.savesComponent.value = id
                                }
                            }

                            InstanceDetails.RESOURCE_PACKS -> {
                                selected?.id?.value?.let { id ->
                                    instance.instance.resourcepacksComponent.value = id
                                }
                            }

                            InstanceDetails.OPTIONS -> {
                                selected?.id?.value?.let { id ->
                                    instance.instance.optionsComponent.value = id
                                }
                            }

                            InstanceDetails.MODS -> {
                                instance.instance.modsComponent.value = selected?.id?.value
                            }

                            else -> {}
                        }
                        instance.instance.write()
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