package dev.treset.treelauncher.components.saves

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.creation.*
import dev.treset.treelauncher.backend.data.manifest.SavesComponent
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
fun Saves() {
    Components(
        Strings.selector.saves.title(),
        components = AppContext.files.savesComponents,
        componentManifest = AppContext.files.savesManifest,
        checkHasComponent = { details, component -> details.savesId == component.id },
        createContent = { onDone ->
            ComponentCreator(
                components = AppContext.files.savesComponents,
                allowUse = false,
                getCreator = SavesCreator::get,
                onDone = { onDone() }
            )
        },
        reload = {
            try {
                AppContext.files.reloadSaves()
            } catch (e: IOException) {
                AppContext.severeError(e)
            }
        },
        constructSharedData = SharedSavesData::of,
        detailsContent = { SavesDetails() },
        actionBarSpecial = { SavesActionBar() },
        actionBarBoxContent = { BoxContent() },
        detailsOnDrop = {
            filesToAdd.assignFrom(it.readFiles().map { LauncherFile.of(URI(it).path) })
            showAdd.value = true
        },
        detailsScrollable = { displayData.saves.isNotEmpty() || displayData.servers.isNotEmpty() || showAdd.value }
    )
}

fun SavesCreator.get(content: CreationContent<SavesComponent>, onStatus: (Status) -> Unit): ComponentCreator<SavesComponent, out CreationData> {
    return when(content.mode) {
        CreationMode.NEW -> new(NewCreationData(content.newName!!, AppContext.files.savesManifest), onStatus)
        CreationMode.INHERIT -> inherit(InheritCreationData(content.inheritName!!, content.inheritComponent!!, AppContext.files.savesManifest), onStatus)
        CreationMode.USE -> use(UseCreationData(content.useComponent!!, AppContext.files.savesManifest), onStatus)
    }
}