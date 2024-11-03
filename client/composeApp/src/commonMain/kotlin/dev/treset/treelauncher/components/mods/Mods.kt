package dev.treset.treelauncher.components.mods

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.components.Components
import dev.treset.treelauncher.localization.Strings
import java.io.IOException
import java.net.URI


@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Mods() {
    Components(
        title = Strings.selector.mods.title(),
        components = AppContext.files.modsComponents,
        componentManifest = AppContext.files.modsManifest,
        checkHasComponent = { details, component -> details.modsId == component.id },
        isEnabled = { id != AppContext.runningInstance?.modsComponent?.value?.id },
        reload = {
            try {
                AppContext.files.reloadMods()
            } catch (e: IOException) {
                AppContext.severeError(e)
            }
        },
        constructSharedData = SharedModsData::of,
        createContent =  { onDone ->
            ModsCreation(
                components = AppContext.files.modsComponents,
                showUse = false,
                onDone = { onDone() }
            )
        },
        detailsContent = { ModsDetails(it) },
        actionBarSpecial = { ModsActionBar() },
        actionBarBoxContent = { ModsBoxContent(it) },
        detailsOnDrop = {
            it.readFiles().firstOrNull()?.let {
                droppedFile.value = LauncherFile.of(URI(it).path)
                if(editingMod.value == null && !showSearch.value) {
                    showSearch.value = true
                }
            }
        },
        detailsScrollable = { false }
    )
}