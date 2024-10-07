package net.treset.treelauncher.components

import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.config.appSettings
import net.treset.treelauncher.backend.creation.OptionsCreator
import net.treset.treelauncher.creation.ComponentCreator
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
                getCreator = { OptionsCreator(AppContext.files.optionsManifest, it) },
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