package net.treset.treelauncher.components.mods

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.mc_version_loader.launcher.LauncherMod
import net.treset.mc_version_loader.launcher.LauncherModsDetails
import net.treset.mc_version_loader.minecraft.MinecraftGame
import net.treset.mc_version_loader.minecraft.MinecraftVersion
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.app
import net.treset.treelauncher.backend.config.LauncherModSortType
import net.treset.treelauncher.backend.config.appSettings
import net.treset.treelauncher.backend.creation.ModsCreator
import net.treset.treelauncher.backend.util.EmptyingJobQueue
import net.treset.treelauncher.backend.util.exception.FileLoadException
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.components.Components
import net.treset.treelauncher.components.SortContext
import net.treset.treelauncher.creation.CreationMode
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
fun Mods(
    appContext: AppContext
) {
    var components by remember { mutableStateOf(appContext.files.modsComponents.sortedBy { it.first.name }) }

    var selected: Pair<LauncherManifest, LauncherModsDetails>? by remember { mutableStateOf(null) }

    var showSearch by remember(selected) { mutableStateOf(false) }
    var checkUpdates by remember(selected) { mutableStateOf(false) }

    var sort: LauncherModSortType by remember { mutableStateOf(appSettings().modSortType) }
    var reverse by remember { mutableStateOf(appSettings().isModSortReverse) }

    var editingMod: LauncherMod? by remember(selected) { mutableStateOf(null) }

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
                        "fabric",
                        state.version,
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
            try {
                appContext.files.reloadModsManifest()
                appContext.files.reloadModsComponents()
                components = appContext.files.modsComponents.sortedBy { it.first.name }
            } catch (e: FileLoadException) {
                app().severeError(e)
            }
        },
        createContent =  { onCreate: (ModsCreationState) -> Unit ->
            ModsCreation(
                components,
                onCreate = onCreate
            )
        },
        detailsContent = { current, _, _ ->
            var redrawMods by remember(current) { mutableStateOf(0) }

            val autoUpdate by remember { mutableStateOf(appSettings().isModsUpdate) }
            val disableNoVersion by remember { mutableStateOf(appSettings().isModsDisable) }
            val enableOnDownload by remember { mutableStateOf(appSettings().isModsEnable) }

            var versions: List<MinecraftVersion> by remember(current) { mutableStateOf(emptyList()) }
            var showSnapshots by remember(current) { mutableStateOf(false) }
            var selectedVersion: MinecraftVersion? by remember(current) { mutableStateOf(null) }

            var popupData: PopupData? by remember { mutableStateOf(null) }

            val mods: List<LauncherMod> = remember(sort, reverse, redrawMods, current.second.mods.size, current.second.modsVersion) {
                current.second.mods.sortedWith(sort.comparator).let {
                    if(reverse) it.reversed() else it
                }
            }

            DisposableEffect(current) {
                selected = current

                onDispose {
                    selected = null
                }
            }

            LaunchedEffect(current, showSnapshots) {
                Thread {
                    versions = if (showSnapshots) {
                        MinecraftGame.getVersions()
                    } else {
                        MinecraftGame.getReleases()
                    }.also { v ->
                        selectedVersion = v.firstOrNull {
                            it.id == current.second.modsVersion
                        }
                    }
                }.start()
            }

            val updateQueue = remember(current) {
                EmptyingJobQueue(
                    onEmptied = {
                        LauncherFile.of(
                            current.first.directory,
                            current.first.details
                        ).write(
                            current.second
                        )
                        redrawMods++
                    }
                ) {
                    current.second.mods
                }
            }

            DisposableEffect(current) {
                onDispose {
                    updateQueue.finish()
                }
            }

            val modContext = remember(current, current.second.modsVersion, autoUpdate, disableNoVersion, enableOnDownload) {
                ModContext(
                    autoUpdate,
                    disableNoVersion,
                    enableOnDownload,
                    current.second.modsVersion,
                    LauncherFile.of(current.first.directory)
                ) { element ->
                    updateQueue.add(element)
                }
            }

            editingMod?.let {
                ModsEdit(
                    modContext,
                    it
                ) {
                    editingMod = null
                }
            } ?: if(showSearch) {
                ModsSearch(
                    current,
                    modContext,
                    appContext
                ) {
                    showSearch = false
                }
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f, false),
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(mods) { mod ->
                            ModButton(
                                mod,
                                modContext,
                                checkUpdates
                            ) {
                                editingMod = mod
                            }
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
                        selected = selectedVersion,
                        allowSearch = true
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
                            selectedVersion?.let { v ->
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
                                            color = MaterialTheme.colorScheme.error
                                        ) {
                                            Text(strings().manager.mods.change.cancel())
                                        }

                                        Button(
                                            onClick = {
                                                modContext.registerChangingJob {
                                                    current.second.modsVersion = v.id

                                                    LauncherFile.of(
                                                        current.first.directory,
                                                        current.first.details
                                                    ).write(current.second)

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
                        enabled = selectedVersion?.let { it.id != current.second.modsVersion } ?: false
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
        },
        detailsScrollable = false,
        actionBarSpecial = { _, settingsOpen, _, _ ->
            if(!settingsOpen && !showSearch && editingMod == null) {
                IconButton(
                    onClick = {
                        showSearch = true
                    },
                    tooltip = strings().manager.mods.add()
                ) {
                    Icon(
                        icons().add,
                        "Add Mod",
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(
                    onClick = {
                        checkUpdates = true
                    },
                    tooltip = strings().manager.mods.update.tooltip(),
                    enabled = !checkUpdates
                ) {
                    Icon(
                        icons().update,
                        "Check Updates",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        },
        actionBarBoxContent = { _, settingsOpen, _, _ ->
            if(!settingsOpen && !showSearch && editingMod == null) {
                SortBox(
                    sorts = LauncherModSortType.entries,
                    modifier = Modifier.align(Alignment.CenterEnd),
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
            } else if((showSearch || editingMod != null) && !settingsOpen) {
                Box(
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    IconButton(
                        onClick = {
                            showSearch = false
                            editingMod = null
                        },
                        tooltip = strings().manager.mods.search.back()
                    ) {
                        Icon(
                            imageVector = icons().back,
                            contentDescription = "Back",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        },
        sortContext = SortContext(
            getSortType = { appSettings().modComponentSortType },
            setSortType = { appSettings().modComponentSortType = it },
            getReverse = { appSettings().isModComponentSortReverse },
            setReverse = { appSettings().isModComponentSortReverse = it }
        )
    )
}