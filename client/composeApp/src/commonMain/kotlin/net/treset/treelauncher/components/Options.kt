package net.treset.treelauncher.components

import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.config.appSettings
import net.treset.treelauncher.backend.creation.*
import net.treset.treelauncher.backend.data.manifest.OptionsComponent
import net.treset.treelauncher.backend.util.Status
import net.treset.treelauncher.creation.ComponentCreator
import net.treset.treelauncher.creation.CreationContent
import net.treset.treelauncher.creation.CreationMode
import net.treset.treelauncher.localization.strings
import java.io.IOException

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Options() {
    var components by remember { mutableStateOf(AppContext.files.optionsComponents.sortedBy { it.name }) }

    Components(
        strings().selector.options.title(),
        components = components,
        componentManifest = AppContext.files.optionsManifest,
        checkHasComponent = { details, component -> details.optionsComponent == component.id },
        createContent = { onDone ->
            ComponentCreator(
                existing = components,
                allowUse = false,
                getCreator = OptionsCreator::get,
                onDone = { onDone() }
            )
        },
        reload = {
            try {
                AppContext.files.reloadOptions()
                components = AppContext.files.optionsComponents.sortedBy { it.name }
            } catch (e: IOException) {
                AppContext.severeError(e)
            }
        },
        settingsDefault = true,
        sortContext = SortContext(
            getSortType = { appSettings().optionsComponentSortType },
            setSortType = { appSettings().optionsComponentSortType = it },
            getReverse = { appSettings().isOptionsComponentSortReverse },
            setReverse = { appSettings().isOptionsComponentSortReverse = it }
        )
    )
}

fun OptionsCreator.get(content: CreationContent<OptionsComponent>, onStatus: (Status) -> Unit): ComponentCreator<OptionsComponent, out CreationData> {
    return when(content.mode) {
        CreationMode.NEW -> new(NewCreationData(content.newName!!, AppContext.files.optionsManifest), onStatus)
        CreationMode.INHERIT -> inherit(InheritCreationData(content.inheritName!!, content.inheritComponent!!, AppContext.files.optionsManifest), onStatus)
        CreationMode.USE -> use(UseCreationData(content.useComponent!!, AppContext.files.optionsManifest), onStatus)
    }
}