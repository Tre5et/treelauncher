package net.treset.treelauncher.components

import androidx.compose.runtime.*
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.app
import net.treset.treelauncher.backend.creation.OptionsCreator
import net.treset.treelauncher.backend.util.exception.FileLoadException
import net.treset.treelauncher.creation.CreationMode
import net.treset.treelauncher.localization.strings

@Composable
fun Options(
    appContext: AppContext
) {
    var components by remember { mutableStateOf(appContext.files.optionsComponents.sortedBy { it.name }) }

    Components(
        strings().selector.options.title(),
        components,
        appContext,
        getCreator = { state ->
            when(state.mode) {
                CreationMode.NEW -> state.name?.let {
                    OptionsCreator(
                        state.name,
                        appContext.files.launcherDetails.typeConversion,
                        appContext.files.optionsManifest
                    )
                }
                CreationMode.INHERIT -> state.name?.let{ state.existing?.let {
                    OptionsCreator(
                        state.name,
                        state.existing,
                        appContext.files.optionsManifest
                    )
                }}
                CreationMode.USE -> null
            }
        },
        reload = {
            try {
                appContext.files.reloadOptionsManifest()
                appContext.files.reloadOptionsComponents()
                components = appContext.files.optionsComponents.sortedBy { it.name }
            } catch (e: FileLoadException) {
                app().severeError(e)
            }
        },
        settingsDefault = true
    )
}