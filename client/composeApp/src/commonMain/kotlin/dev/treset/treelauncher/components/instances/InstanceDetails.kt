package dev.treset.treelauncher.components.instances

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.treset.treelauncher.generic.SelectorButton
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.icons

@Composable
fun SharedInstanceData.InstanceDetails() {
    var selectedDetails: InstanceDetailsType? by remember(component) { mutableStateOf(null) }

    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(1/2f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SelectorButton(
                title = Strings.manager.instance.details.version(),
                component = component.versionComponents.value[0],
                icon = icons().version,
                selected = selectedDetails == InstanceDetailsType.VERSION,
                onClick = {
                    selectedDetails = if (selectedDetails == InstanceDetailsType.VERSION) null else InstanceDetailsType.VERSION
                }
            )
            SelectorButton(
                title = Strings.manager.instance.details.saves(),
                component = component.savesComponent.value,
                icon = icons().saves,
                selected = selectedDetails == InstanceDetailsType.SAVES,
                onClick = {
                    selectedDetails = if (selectedDetails == InstanceDetailsType.SAVES) null else InstanceDetailsType.SAVES
                }
            )
            SelectorButton(
                title = Strings.manager.instance.details.resourcepacks(),
                component = component.resourcepacksComponent.value,
                icon = icons().resourcePacks,
                selected = selectedDetails == InstanceDetailsType.RESOURCE_PACKS,
                onClick = {
                    selectedDetails =
                        if (selectedDetails == InstanceDetailsType.RESOURCE_PACKS) null else InstanceDetailsType.RESOURCE_PACKS
                }
            )
            SelectorButton(
                title = Strings.manager.instance.details.options(),
                component = component.optionsComponent.value,
                icon = icons().options,
                selected = selectedDetails == InstanceDetailsType.OPTIONS,
                onClick = {
                    selectedDetails = if (selectedDetails ==InstanceDetailsType.OPTIONS) null else InstanceDetailsType.OPTIONS
                }
            )
            SelectorButton(
                title = Strings.manager.instance.details.mods(),
                component = component.modsComponent.value,
                icon = component.modsComponent.value?.let { icons().mods } ?: icons().add,
                selected = selectedDetails == InstanceDetailsType.MODS,
                onClick = {
                    selectedDetails = if (selectedDetails == InstanceDetailsType.MODS) null else InstanceDetailsType.MODS
                }
            )
            SelectorButton(
                title = Strings.manager.instance.details.settings(),
                icon = icons().settings,
                selected = selectedDetails == InstanceDetailsType.SETTINGS,
                onClick = {
                    selectedDetails = if (selectedDetails == InstanceDetailsType.SETTINGS) null else InstanceDetailsType.SETTINGS
                }
            )
        }
        selectedDetails?.let {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                when (it) {
                    InstanceDetailsType.SAVES, InstanceDetailsType.OPTIONS, InstanceDetailsType.RESOURCE_PACKS -> {
                        InstanceComponentChanger(
                            type = it,
                        )
                    }

                    InstanceDetailsType.MODS -> {
                        InstanceComponentChanger(
                            type = it,
                            allowUnselect = true,
                        )
                    }

                    InstanceDetailsType.VERSION -> {
                        InstanceVersionChanger()
                    }

                    InstanceDetailsType.SETTINGS -> {
                        InstanceSettings()
                    }
                }
            }
        }
    }
}

enum class InstanceDetailsType {
    VERSION,
    SAVES,
    RESOURCE_PACKS,
    OPTIONS,
    MODS,
    SETTINGS
}