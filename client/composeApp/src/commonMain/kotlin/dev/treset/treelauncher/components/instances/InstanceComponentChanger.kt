package dev.treset.treelauncher.components.instances

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.data.manifest.Component
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.generic.ComboBox
import dev.treset.treelauncher.generic.IconButton
import dev.treset.treelauncher.generic.Text
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.icons
import java.io.IOException

@Composable
fun SharedInstanceData.InstanceComponentChanger(
    type: InstanceDetailsType,
    allowUnselect: Boolean = false,
) {
    val components = when(type) {
        InstanceDetailsType.SAVES -> AppContext.files.savesComponents
        InstanceDetailsType.RESOURCE_PACKS -> AppContext.files.resourcepackComponents
        InstanceDetailsType.OPTIONS -> AppContext.files.optionsComponents
        InstanceDetailsType.MODS -> AppContext.files.modsComponents
        else -> mutableListOf()
    }

    val current by when (type) {
        InstanceDetailsType.SAVES -> component.savesComponent
        InstanceDetailsType.RESOURCE_PACKS -> component.resourcepacksComponent
        InstanceDetailsType.OPTIONS -> component.optionsComponent
        InstanceDetailsType.MODS -> component.modsComponent
        else -> mutableStateOf(null)
    }

    var selected: Component? by remember(current) { mutableStateOf(current) }

    DisposableEffect(Unit) {
        headerContent.value = {
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

        onDispose {
            headerContent.value = {}
        }
    }

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
                        InstanceDetailsType.SAVES -> {
                            selected?.id?.value?.let { id ->
                                component.savesId.value = id
                            }
                        }

                        InstanceDetailsType.RESOURCE_PACKS -> {
                            selected?.id?.value?.let { id ->
                                component.resourcepacksId.value = id
                            }
                        }

                        InstanceDetailsType.OPTIONS -> {
                            selected?.id?.value?.let { id ->
                                component.optionsId.value = id
                            }
                        }

                        InstanceDetailsType.MODS -> {
                            component.modsId.value = selected?.id?.value
                        }

                        else -> {}
                    }
                    component.write()
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