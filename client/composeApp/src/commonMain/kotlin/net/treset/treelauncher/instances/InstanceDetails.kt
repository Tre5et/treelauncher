package net.treset.treelauncher.instances

import androidx.compose.runtime.*
import net.treset.treelauncher.backend.data.InstanceData
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.*

@Composable
fun InstanceDetails(instance: InstanceData) {
    var selectedDetails: InstanceDetails? by remember { mutableStateOf(null) }

    LaunchedEffect(instance) {
        selectedDetails = null
    }

    InstanceComponentButton(
        title = strings().manager.instance.details.version(),
        component = instance.versionComponents[0].first,
        icon = icons().version,
        selected = selectedDetails == InstanceDetails.VERSION,
        onClick = { selectedDetails = if(selectedDetails == InstanceDetails.VERSION) null else InstanceDetails.VERSION }
    )
    InstanceComponentButton(
        title = strings().manager.instance.details.saves(),
        component = instance.savesComponent,
        icon = icons().saves,
        selected = selectedDetails == InstanceDetails.SAVES,
        onClick = { selectedDetails = if(selectedDetails == InstanceDetails.SAVES) null else InstanceDetails.SAVES }
    )
    InstanceComponentButton(
        title = strings().manager.instance.details.resourcepacks(),
        component = instance.resourcepacksComponent,
        icon = icons().resourcePacks,
        selected = selectedDetails == InstanceDetails.RESOURCE_PACKS,
        onClick = { selectedDetails = if(selectedDetails == InstanceDetails.RESOURCE_PACKS) null else InstanceDetails.RESOURCE_PACKS }
    )
    InstanceComponentButton(
        title = strings().manager.instance.details.options(),
        component = instance.optionsComponent,
        icon = icons().options,
        selected = selectedDetails == InstanceDetails.OPTIONS,
        onClick = { selectedDetails = if(selectedDetails == InstanceDetails.OPTIONS) null else InstanceDetails.OPTIONS }
    )
    instance.modsComponent?.let { modsComponent ->
        InstanceComponentButton(
            title = strings().manager.instance.details.mods(),
            component = modsComponent.first,
            icon = icons().mods,
            selected = selectedDetails == InstanceDetails.MODS,
            onClick = { selectedDetails = if(selectedDetails == InstanceDetails.MODS) null else InstanceDetails.MODS }
        )
    }
    InstanceComponentButton(
        title = strings().manager.instance.settings(),
        icon = icons().settings,
        selected = selectedDetails == InstanceDetails.SETTINGS,
        onClick = { selectedDetails = if(selectedDetails == InstanceDetails.SETTINGS) null else InstanceDetails.SETTINGS }
    )
}