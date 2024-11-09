package dev.treset.treelauncher.components.options

import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.creation.*
import dev.treset.treelauncher.backend.data.manifest.OptionsComponent
import dev.treset.treelauncher.backend.util.Status
import dev.treset.treelauncher.components.Components
import dev.treset.treelauncher.components.SharedComponentData
import dev.treset.treelauncher.creation.ComponentCreator
import dev.treset.treelauncher.creation.CreationContent
import dev.treset.treelauncher.creation.CreationMode
import dev.treset.treelauncher.localization.Strings
import java.io.IOException

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Options() {
    Components(
        Strings.selector.options.title(),
        components = AppContext.files.optionsComponents,
        componentManifest = AppContext.files.optionsManifest,
        checkHasComponent = { details, component -> details.optionsId.value == component.id.value },
        createContent = { onDone ->
            ComponentCreator(
                components = AppContext.files.optionsComponents,
                allowUse = false,
                getCreator = OptionsCreator::get,
                onDone = onDone
            )
        },
        reload = {
            try {
                AppContext.files.reloadOptions()
            } catch (e: IOException) {
                AppContext.severeError(e)
            }
        },
        constructSharedData = { c,r ->
            SharedComponentData.of(c,r)
        },
        settingsDefault = true
    )
}

fun OptionsCreator.get(content: CreationContent<OptionsComponent>, onStatus: (Status) -> Unit): ComponentCreator<OptionsComponent, out CreationData> {
    return when(content.mode) {
        CreationMode.NEW -> new(NewCreationData(content.newName!!, AppContext.files.optionsManifest), onStatus)
        CreationMode.INHERIT -> inherit(InheritCreationData(content.inheritName!!, content.inheritComponent!!, AppContext.files.optionsManifest), onStatus)
        CreationMode.USE -> use(UseCreationData(content.useComponent!!, AppContext.files.optionsManifest), onStatus)
    }
}