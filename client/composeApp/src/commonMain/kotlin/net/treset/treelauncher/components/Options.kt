package net.treset.treelauncher.components

import androidx.compose.runtime.*
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.creation.OptionsCreator
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
        getCreator = { mode, name, existing ->
            when(mode) {
                CreationMode.NEW -> name?.let {
                    OptionsCreator(
                        name,
                        appContext.files.launcherDetails.typeConversion,
                        appContext.files.optionsManifest
                    )
                }
                CreationMode.INHERIT -> name?.let{ existing?.let {
                    OptionsCreator(
                        name,
                        existing,
                        appContext.files.optionsManifest
                    )
                }}
                CreationMode.USE -> null
            }
        },
        reload = {
            appContext.files.reloadOptionsManifest()
            appContext.files.reloadOptionsComponents()
            components = appContext.files.optionsComponents.sortedBy { it.name }
        },
        settingsDefault = true
    )
}