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
        getCreator = { mode, name, existing ->
            when(mode) {
                CreationMode.NEW -> name?.let {
                    ResourcepackCreator(
                        name,
                        appContext.files.launcherDetails.typeConversion,
                        appContext.files.resourcepackManifest
                    )
                }
                CreationMode.INHERIT -> name?.let{ existing?.let {
                    ResourcepackCreator(
                        name,
                        existing,
                        appContext.files.resourcepackManifest
                    )
                }}
                CreationMode.USE -> existing?.let {
                    ResourcepackCreator(
                        existing
                    )
                }
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