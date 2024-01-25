package net.treset.treelauncher.components.mods

import androidx.compose.runtime.*
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.creation.ModsCreator
import net.treset.treelauncher.components.Components
import net.treset.treelauncher.creation.CreationMode
import net.treset.treelauncher.localization.strings

@Composable
fun Mods(
    appContext: AppContext
) {
    var components by remember { mutableStateOf(appContext.files.modsComponents.sortedBy { it.first.name }) }

    Components(
        title = strings().selector.mods.title(),
        components = components,
        manifest = { first },
        appContext = appContext,
        getCreator = { state: ModsCreationState ->
            when(state.mode) {
                CreationMode.NEW -> state.name?.let{ state.version?.let {
                    ModsCreator(
                        state.name,
                        appContext.files.launcherDetails.typeConversion,
                        appContext.files.modsManifest,
                        state.version,
                        "fabric",
                        appContext.files.gameDetailsManifest
                    )
                }}
                CreationMode.INHERIT -> state.name?.let{ state.existing?.let {
                    ModsCreator(
                        state.name,
                        state.existing,
                        appContext.files.modsManifest,
                        appContext.files.gameDetailsManifest
                    )
                }}
                CreationMode.USE -> null
            }
        },
        reload = {
            appContext.files.reloadModsManifest()
            appContext.files.reloadModsComponents()
            components = appContext.files.modsComponents.sortedBy { it.first.name }
        },
        createContent =  { onCreate: (ModsCreationState) -> Unit ->
            ModsCreation(
                components,
                onCreate = onCreate
            )
        },
        detailsContent = { selected, _, _ ->
            ModsDetails(selected)
        },
        detailsScrollable = false
    )
}