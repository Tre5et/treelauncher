package net.treset.treelauncher.components.mods

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.mc_version_loader.launcher.LauncherMod
import net.treset.mc_version_loader.launcher.LauncherModsDetails
import net.treset.mc_version_loader.mods.MinecraftMods
import net.treset.mc_version_loader.mods.ModData
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.backend.util.string.FormatString
import net.treset.treelauncher.generic.IconButton
import net.treset.treelauncher.generic.SelectorButton
import net.treset.treelauncher.generic.TextBox
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.icons
import kotlin.math.log10
import kotlin.math.roundToInt

@Composable
fun ModsSearch(
    component: Pair<LauncherManifest, LauncherModsDetails>,
    modContext: ModContext,
    appContext: AppContext,
    closeSearch: () -> Unit
) {
    var showLocal by remember { mutableStateOf(false) }

    if(showLocal) {
        ModsImport(
            component,
            modContext,
            appContext
        ) {
            closeSearch()
        }
        return
    }

    var tfValue by remember { mutableStateOf("") }
    var results: List<ModData>? by remember { mutableStateOf(null) }

    var searching by remember { mutableStateOf(false) }

    var recheckExising by remember { mutableStateOf(0) }

    val searchContext = remember(modContext, recheckExising) {
        SearchContext.from(
            modContext,
            recheckExising
        ) {
            recheckExising++
        }
    }

    LaunchedEffect(searching) {
        if(searching) {
            results = MinecraftMods.searchCombinedMods(
                tfValue,
                modContext.version,
                "fabric",
                25,
                0
            ).sortedWith { o1, o2 -> (
                    FormatString.distance(tfValue, o1.name) -
                    FormatString.distance(tfValue, o2.name) +
                    log10((o2.downloadsCount / o1.downloadsCount).toDouble())
                ).roundToInt()
            }
            searching = false
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
                onChange = { tfValue = it },
                placeholder = strings().manager.mods.search.search(),
                modifier = Modifier.onKeyEvent {
                    if (it.key == Key.Enter) {
                        results = null
                        searching = true
                    }
                    false
                }.weight(1f, true)
            )

            IconButton(
                onClick = {
                    results = null
                    searching = true
                },
                tooltip = strings().manager.mods.search.searchTooltip()
            ) {
                Icon(
                    imageVector = icons().search,
                    contentDescription = "Search",
                )
            }
        }

        if (searching) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(0.7f)
            )
        } else {
            results?.let {
                if (it.isEmpty()) {
                    Text(strings().manager.mods.search.noResults())
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f, false)
                    ) {
                        items(it) { mod ->
                            ModSearchButton(
                                mod,
                                searchContext
                            )
                        }
                    }
                }
            }
        }

        SelectorButton(
            title = strings().manager.mods.search.addLocal(),
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
    val version: String,
    val directory: LauncherFile,
    val registerChangingJob: ((MutableList<LauncherMod>) -> Unit) -> Unit,
    val recheck: Int,
    val requestRecheck: () -> Unit,
) {
    companion object {
        fun from(
            modContext: ModContext,
            recheck: Int,
            requestRecheck: () -> Unit
        ): SearchContext = SearchContext(
            modContext.autoUpdate,
            modContext.disableNoVersion,
            modContext.enableOnDownload,
            modContext.version,
            modContext.directory,
            modContext.registerChangingJob,
            recheck,
            requestRecheck
        )
    }
}