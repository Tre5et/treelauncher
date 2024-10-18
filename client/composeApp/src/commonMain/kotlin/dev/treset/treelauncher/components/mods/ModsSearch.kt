package dev.treset.treelauncher.components.mods

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
import dev.treset.mcdl.exception.FileDownloadException
import dev.treset.mcdl.format.FormatUtils
import dev.treset.mcdl.mods.ModData
import dev.treset.mcdl.mods.ModProvider
import dev.treset.mcdl.mods.curseforge.CurseforgeSearch
import dev.treset.mcdl.mods.modrinth.ModrinthSearch
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.data.LauncherMod
import dev.treset.treelauncher.backend.data.manifest.ModsComponent
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.backend.util.string.FormatString
import dev.treset.treelauncher.components.mods.display.ModDataProvider
import dev.treset.treelauncher.components.mods.display.ModDataSearchDisplay
import dev.treset.treelauncher.generic.*
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.icons
import kotlin.math.log10
import kotlin.math.roundToInt

@Composable
fun ModsSearch(
    component: ModsComponent,
    modContext: ModContext,
    droppedFile: LauncherFile? = null,
    closeSearch: () -> Unit
) {
    var showLocal by remember { mutableStateOf(false) }

    if(showLocal) {
        ModsImport(
            component,
            modContext,
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
                            ModData.searchCombined(
                                tfValue,
                                modContext.versions,
                                modContext.types.map { it.id },
                            25,
                            0
                            )
                        } else if(searchContext.providers.contains(ModProvider.MODRINTH)) {
                            ModrinthSearch.search(
                                tfValue,
                                modContext.versions,
                                modContext.types.map { it.id },
                                25,
                                0
                            ).hits
                        } else {
                            CurseforgeSearch.search(
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
                placeholder = Strings.manager.mods.addMods.search(),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            results = null
                            searching = true
                        },
                        icon = icons().search,
                        tooltip = Strings.manager.mods.addMods.searchTooltip()
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
                    Text(Strings.manager.mods.addMods.noResults())
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
            title = Strings.manager.mods.addMods.addLocal(),
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