package dev.treset.treelauncher.components.resourcepacks

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.creation.*
import dev.treset.treelauncher.backend.data.manifest.ResourcepackComponent
import dev.treset.treelauncher.backend.util.Status
import dev.treset.treelauncher.backend.util.assignFrom
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.components.BoxContent
import dev.treset.treelauncher.components.Components
import dev.treset.treelauncher.creation.ComponentCreator
import dev.treset.treelauncher.creation.CreationContent
import dev.treset.treelauncher.creation.CreationMode
import dev.treset.treelauncher.localization.Strings
import java.io.IOException
import java.net.URI

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Resourcepacks() {
    Components(
        Strings.selector.resourcepacks.title(),
        components = AppContext.files.resourcepackComponents,
        componentManifest = AppContext.files.resourcepackManifest,
        checkHasComponent = { instance, component -> instance.resourcepacksId.value == component.id.value },
        createContent = { onDone ->
            ComponentCreator(
                components = AppContext.files.resourcepackComponents,
                allowUse = false,
                getCreator = ResourcepackCreator::get,
                onDone = onDone
            )
        },
        reload = {
            try {
                AppContext.files.reloadResourcepacks()
            } catch (e: IOException) {
                AppContext.severeError(e)
            }
        },
        constructSharedData = SharedResourcepacksData::of,
        detailsContent = { ResourcepacksDetails() },
        actionBarSpecial = { ResourcepacksActionBar() },
        actionBarBoxContent = { BoxContent() },
        detailsOnDrop = {
            filesToAdd.assignFrom(it.readFiles().map { LauncherFile.of(URI(it).path) })
            showAdd.value = true
        },
        detailsScrollable = { displayData.resourcepacks.isNotEmpty() || showAdd.value }
    )
}

fun ResourcepackCreator.get(content: CreationContent<ResourcepackComponent>, onStatus: (Status) -> Unit): ComponentCreator<ResourcepackComponent, out CreationData> {
    return when(content.mode) {
        CreationMode.NEW -> new(NewCreationData(content.newName!!, AppContext.files.resourcepackManifest), onStatus)
        CreationMode.INHERIT -> inherit(InheritCreationData(content.inheritName!!, content.inheritComponent!!, AppContext.files.resourcepackManifest), onStatus)
        CreationMode.USE -> use(UseCreationData(content.useComponent!!, AppContext.files.resourcepackManifest), onStatus)
    }
}