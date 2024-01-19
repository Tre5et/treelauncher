package net.treset.treelauncher.components

import androidx.compose.runtime.*
import net.treset.mc_version_loader.resoucepacks.Resourcepack
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.creation.ResourcepackCreator
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.creation.CreationMode
import net.treset.treelauncher.localization.strings

@Composable
fun Resourcepacks(
    appContext: AppContext
) {
    var components by remember { mutableStateOf(appContext.files.resourcepackComponents.sortedBy { it.name }) }

    var resourcepacks: List<Resourcepack> by remember { mutableStateOf(emptyList()) }

    Components(
        strings().selector.resourcepacks.title(),
        components,
        appContext,
        getCreator = { state ->
            when(state.mode) {
                CreationMode.NEW -> state.name?.let {
                    ResourcepackCreator(
                        state.name,
                        appContext.files.launcherDetails.typeConversion,
                        appContext.files.resourcepackManifest
                    )
                }
                CreationMode.INHERIT -> state.name?.let{ state.existing?.let {
                    ResourcepackCreator(
                        state.name,
                        state.existing,
                        appContext.files.resourcepackManifest
                    )
                }}
                CreationMode.USE -> null
            }
        },
        reload = {
            appContext.files.reloadResourcepackManifest()
            appContext.files.reloadResourcepackComponents()
            components = appContext.files.resourcepackComponents.sortedBy { it.name }
        },
        detailsContent = { current, _, _ ->
            LaunchedEffect(current) {
                resourcepacks = LauncherFile.of(current.directory).listFiles()
                    .mapNotNull {
                        try {
                            Resourcepack.from(it)
                        } catch (e: Exception) {
                            null
                        }
                    }
            }

            if(resourcepacks.isNotEmpty()) {
                resourcepacks.forEach {
                    ResourcepackButton(it)
                }
            }
        }
    )
}