package net.treset.treelauncher.components.mods

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import net.treset.mc_version_loader.exception.FileDownloadException
import net.treset.mc_version_loader.format.FormatUtils
import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.mc_version_loader.launcher.LauncherMod
import net.treset.mc_version_loader.launcher.LauncherModsDetails
import net.treset.mc_version_loader.mods.MinecraftMods
import net.treset.mc_version_loader.mods.ModProvider
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.AppContextData
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.backend.util.string.FormatString
import net.treset.treelauncher.components.mods.display.ModDataProvider
import net.treset.treelauncher.components.mods.display.ModDataSearchDisplay
import net.treset.treelauncher.generic.*
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.icons
import kotlin.math.log10
import kotlin.math.roundToInt

@Composable
fun ModsSearch(
    component: Pair<LauncherManifest, LauncherModsDetails>,
    modContext: ModContext,
    appContext: AppContextData,
    droppedFile: LauncherFile? = null,
    closeSearch: () -> Unit
) {
    var showLocal by remember { mutableStateOf(false) }

    if(showLocal) {
        ModsImport(
            component,
            modContext,
            appContext,
            droppedFile = droppedFile,
        ) {
            closeSearch()
        }
        return
    }

    var tfValue by remember { mutableStateOf("") }

    var results: List<ModDataSearchDisplay>? by remember { mutableStateOf(null) }

    var searching by remember { mutableStateOf(false) }

    val searchContext = remember(modContext) {
        SearchContext.from(
            modContext,
        )
    }

    LaunchedEffect(droppedFile) {
        droppedFile?.let {
            if(!showLocal) {
                showLocal = true
            }
        }
    }

    LaunchedEffect(searching, searchContext) {
        if(searching) {
            Thread {
                try {
                    results = (
                        if(searchContext.providers.isEmpty() || searchContext.providers.containsAll(listOf(ModProvider.MODRINTH, ModProvider.CURSEFORGE))) {
                            MinecraftMods.searchCombinedMods(
                                tfValue,
                                modContext.versions,
                                modContext.types.map { it.id },
                            25,
                            0
                            )
                        } else if(searchContext.providers.contains(ModProvider.MODRINTH)) {
                            MinecraftMods.searchModrinth(
                                tfValue,
                                modContext.versions,
                                modContext.types.map { it.id },
                                25,
                                0
                            ).hits
                        } else {
                            MinecraftMods.searchCurseforge(
                                tfValue,
                                modContext.versions,
                                FormatUtils.modLoadersToCurseforgeModLoaders(modContext.types.map { it.id }),
                                25,
                                0
                            ).data
                        }
                    ).sortedWith { o1, o2 ->
                        (
                            FormatString.distance(tfValue, o1.name) -
                                FormatString.distance(tfValue, o2.name) +
                                log10((o2.downloadsCount / o1.downloadsCount).toDouble())
                        ).roundToInt()
                    }.map {
                        ModDataSearchDisplay(
                            it,
                            searchContext
                        )
                    }
                } catch (e: FileDownloadException) {
                    AppContext.error(e)
                }
                searching = false
            }.start()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextBox(
                tfValue,
                onTextChanged = { tfValue = it },
                placeholder = strings().manager.mods.addMods.search(),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            results = null
                            searching = true
                        },
                        icon = icons().search,
                        tooltip = strings().manager.mods.addMods.searchTooltip()
                    )
                },
                modifier = Modifier.onKeyEvent {
                    if (it.key == Key.Enter) {
                        results = null
                        searching = true
                    }
                    false
                }.weight(1f, true)
            )
        }

        if (searching) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(0.7f)
            )
        } else {
            results?.let {
                if (it.isEmpty()) {
                    Text(strings().manager.mods.addMods.noResults())
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f, false)
                    ) {
                        items(it) { mod ->
                            ModDataProvider(
                                mod
                            ) {
                                ModSearchButton()
                            }
                        }
                    }
                }
            }
        }

        SelectorButton(
            title = strings().manager.mods.addMods.addLocal(),
            icon = icons().add,
            selected = false,
        ) {
            showLocal = true
        }
    }
}

data class SearchContext(
    val autoUpdate: Boolean,
    val disableNoVersion: Boolean,
    val enableOnDownload: Boolean,
    val versions: List<String>,
    val types: List<VersionType>,
    val providers: List<ModProvider>,
    val directory: LauncherFile,
    val registerChangingJob: ((MutableList<LauncherMod>) -> Unit) -> Unit,
) {
    private var recheckCallbacks: MutableList<() -> Unit> = mutableListOf()

    fun registerRecheck(onRecheck: () -> Unit) {
        recheckCallbacks.add(onRecheck)
    }

    fun recheck() {
        recheckCallbacks.forEach { it() }
    }

    companion object {
        fun from(
            modContext: ModContext
        ): SearchContext = SearchContext(
            modContext.autoUpdate,
            modContext.disableNoVersion,
            modContext.enableOnDownload,
            modContext.versions,
            modContext.types,
            modContext.providers,
            modContext.directory,
            modContext.registerChangingJob
        )
    }
}