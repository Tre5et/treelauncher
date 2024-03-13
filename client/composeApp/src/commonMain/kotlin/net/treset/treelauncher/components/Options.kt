package net.treset.treelauncher.components

import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.app
import net.treset.treelauncher.backend.config.appSettings
import net.treset.treelauncher.backend.creation.OptionsCreator
import net.treset.treelauncher.backend.util.exception.FileLoadException
import net.treset.treelauncher.creation.CreationMode
import net.treset.treelauncher.localization.strings

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Options() {
    var components by remember { mutableStateOf(AppContext.files.optionsComponents.sortedBy { it.name }) }

    Components(
        strings().selector.options.title(),
        components = components,
        componentManifest = AppContext.files.optionsManifest,
        checkHasComponent = { details, component -> details.optionsComponent == component.id },
        getCreator = { state ->
            when(state.mode) {
                CreationMode.NEW -> state.name?.let {
                    OptionsCreator(
                        state.name,
                        AppContext.files.launcherDetails.typeConversion,
                        AppContext.files.optionsManifest
                    )
                }
                CreationMode.INHERIT -> state.name?.let{ state.existing?.let {
                    OptionsCreator(
                        state.name,
                        state.existing,
                        AppContext.files.optionsManifest
                    )
                }}
                CreationMode.USE -> null
            }
        },
        reload = {
            try {
                AppContext.files.reloadOptionsManifest()
                AppContext.files.reloadOptionsComponents()
                components = AppContext.files.optionsComponents.sortedBy { it.name }
            } catch (e: FileLoadException) {
                app().severeError(e)
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