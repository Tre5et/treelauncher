package net.treset.treelauncher.components.mods

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.mc_version_loader.launcher.LauncherMod
import net.treset.mc_version_loader.launcher.LauncherModsDetails
import net.treset.mc_version_loader.minecraft.MinecraftGame
import net.treset.mc_version_loader.minecraft.MinecraftVersion
import net.treset.treelauncher.backend.config.LauncherModSortType
import net.treset.treelauncher.backend.config.appSettings
import net.treset.treelauncher.backend.util.EmptyingJobQueue
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.generic.*
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.icons

data class ModContext(
    val autoUpdate: Boolean,
    val disableNoVersion: Boolean,
    val enableOnDownload: Boolean,
    val version: String,
    val directory: LauncherFile,
    val registerChangingJob: ((MutableList<LauncherMod>) -> Unit) -> Unit,
)

@Composable
fun ColumnScope.ModsDetails(
    selected: Pair<LauncherManifest, LauncherModsDetails>,
    redraw: () -> Unit,
) {
    var showSearch by remember { mutableStateOf(false) }

    if(showSearch) {
        ModsSearch()
        return
    }

    var autoUpdate by remember { mutableStateOf(appSettings().isModsUpdate) }
    var disableNoVersion by remember { mutableStateOf(appSettings().isModsDisable) }
    var enableOnDownload by remember { mutableStateOf(appSettings().isModsEnable) }

    var checkUpdates by remember { mutableStateOf(false) }

    var sort: LauncherModSortType by remember { mutableStateOf(appSettings().modSortType) }
    var reverse by remember { mutableStateOf(appSettings().isModSortReverse) }

    var versions: List<MinecraftVersion> by remember(selected) { mutableStateOf(emptyList()) }
    var showSnapshots by remember(selected) { mutableStateOf(false) }
    var selectedVersion: MinecraftVersion? by remember { mutableStateOf(null) }

    var popupData: PopupData? by remember { mutableStateOf(null) }

    val mods: List<LauncherMod> = remember(sort, reverse, selected.second.mods.size, selected.second.modsVersion) {
        selected.second.mods.sortedWith(sort.comparator).let {
            if(reverse) it.reversed() else it
        }
    }

    LaunchedEffect(showSnapshots) {
        versions = if (showSnapshots) {
            MinecraftGame.getVersions()
        } else {
            MinecraftGame.getReleases()
        }.also { v ->
            selectedVersion = v.firstOrNull {
                it.id == selected.second.modsVersion
            }
        }
    }

    val updateQueue = remember {
        EmptyingJobQueue(
            onEmptied = {
                LauncherFile.of(
                    selected.first.directory,
                    selected.first.details
                ).write(
                    selected.second
                )
                redraw()
            }
        ) {
            selected.second.mods
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            updateQueue.finish()
        }
    }

    val modContext = remember(selected.second.modsVersion, autoUpdate, disableNoVersion, enableOnDownload) {
        ModContext(
            autoUpdate,
            disableNoVersion,
            enableOnDownload,
            selected.second.modsVersion,
            LauncherFile.of(selected.first.directory)
        ) { element ->
            updateQueue.add(element)
        }

    }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    showSearch = true
                },
            ) {
                Icon(
                    icons().add,
                    "Add Mod",
                    modifier = Modifier.size(46.dp)
                )
            }

            IconButton(
                onClick = {
                    checkUpdates = true
                    //TODO: use update options (may or may not happen)
                },

                ) {
                Icon(
                    icons().update,
                    "Check Updates",
                    modifier = Modifier.size(46.dp)
                )
            }

            Column {
                Row(
                    modifier = Modifier.requiredHeight(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TitledCheckBox(
                        title = strings().manager.mods.update.auto(),
                        checked = autoUpdate,
                        onCheckedChange = {
                            autoUpdate = it
                            appSettings().isModsUpdate = it
                        }
                    )

                    TitledCheckBox(
                        title = strings().manager.mods.update.enable(),
                        checked = enableOnDownload,
                        onCheckedChange = {
                            enableOnDownload = it
                            appSettings().isModsEnable = it
                        },
                        enabled = autoUpdate,
                    )
                }

                TitledCheckBox(
                    title = strings().manager.mods.update.disable(),
                    checked = disableNoVersion,
                    onCheckedChange = {
                        disableNoVersion = it
                        appSettings().isModsDisable = it
                    },
                    modifier = Modifier.requiredHeight(28.dp)
                )
            }
        }

        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SortBox(
                sorts = LauncherModSortType.entries,
                selected = sort,
                reversed = reverse,
                onSelected = {
                    sort = it
                    appSettings().modSortType = it
                },
                onReversed = {
                    reverse = !reverse
                    appSettings().isModSortReverse = reverse
                }
            )
        }
    }


    Box(
        modifier = Modifier
            .weight(1f, false),
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(mods) {mod ->
                ModButton(
                    mod,
                    modContext,
                    checkUpdates
                )
            }
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        ComboBox(
            versions,
            onSelected = {
                selectedVersion = it
            },
            loading = versions.isEmpty(),
            defaultSelected = selectedVersion,
        )

        TitledCheckBox(
            title = strings().creator.version.showSnapshots(),
            checked = showSnapshots,
            onCheckedChange = {
                showSnapshots = it
            }
        )

        IconButton(
            onClick = {
                selectedVersion?.let {v ->
                    popupData = PopupData(
                        type = PopupType.WARNING,
                        titleRow = { Text(strings().manager.mods.change.title()) },
                        content = {
                            Text(strings().manager.mods.change.message())
                        },
                        buttonRow = {
                            Button(
                                onClick = {
                                    popupData = null
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text(strings().manager.mods.change.cancel())
                            }

                            Button(
                                onClick = {
                                    modContext.registerChangingJob {
                                        selected.second.modsVersion = v.id

                                        LauncherFile.of(
                                            selected.first.directory,
                                            selected.first.details
                                        ).write(
                                            selected.second
                                        )

                                        popupData = null
                                    }
                                }
                            ) {
                                Text(strings().manager.mods.change.confirm())
                            }
                        }
                    )
                }
            },
            enabled = selectedVersion?.let { it.id != selected.second.modsVersion }?: false
        ) {
            Icon(
                icons().change,
                "Change Version"
            )
        }
    }

    popupData?.let {
        PopupOverlay(it)
    }
}