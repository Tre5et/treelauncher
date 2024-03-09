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
    val versions: List<String>,
    val types: List<VersionType>,
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
        componentManifest = appContext.files.modsManifest,
        checkHasComponent = { details, component -> details.modsComponent == component.id },
        getManifest = { first },
        appContext = appContext,
        getCreator = { state: ModsCreationState ->
            when(state.mode) {
                CreationMode.NEW -> state.name?.let{ state.version?.let { state.type?.let { state.alternateLoader?.let {
                    ModsCreator(
                        state.name,
                        appContext.files.launcherDetails.typeConversion,
                        appContext.files.modsManifest,
                        if(state.alternateLoader && state.type == VersionType.QUILT) {
                            listOf(VersionType.QUILT.id, VersionType.FABRIC.id)
                        } else {
                            listOf(state.type.id)
                        },
                        listOf(state.version.id),
                        appContext.files.gameDetailsManifest
                    )
                }}}}
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
                showUse = false,
                onCreate = onCreate
            )
        },
        detailsContent = { current, _, _ ->
            val types = remember(current.second.types) {
                VersionType.fromIds(current.second.types)
            }

            var redrawMods by remember(current) { mutableStateOf(0) }

            val autoUpdate by remember { mutableStateOf(appSettings().isModsUpdate) }
            val disableNoVersion by remember { mutableStateOf(appSettings().isModsDisable) }
            val enableOnDownload by remember { mutableStateOf(appSettings().isModsEnable) }

            var searchContent by remember { mutableStateOf("") }

            var versions: List<MinecraftVersion> by remember(current) { mutableStateOf(emptyList()) }
            var showSnapshots by remember(current) { mutableStateOf(false) }
            var selectedVersion: MinecraftVersion? by remember(current) { mutableStateOf(null) }
            var selectedType: VersionType by remember(types) { mutableStateOf(types[0]) }
            var includeAlternateLoader by remember(types) { mutableStateOf(
                !(types[0] == VersionType.QUILT && types.size <= 1)
            ) }

            var popupData: PopupData? by remember { mutableStateOf(null) }

            val mods: List<LauncherMod> = remember(sort, reverse, redrawMods, current.second.mods.size, current.second.versions) {
                current.second.mods.sortedWith(sort.comparator).let {
                    if(reverse) it.reversed() else it
                }
            }

            val filteredMods  = remember(mods, searchContent) {
                mods.filter {
                    it.name.contains(searchContent, true)
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
                            it.id == current.second.versions[0]
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

            val modContext = remember(current, current.second.versions, types, autoUpdate, disableNoVersion, enableOnDownload) {
                ModContext(
                    autoUpdate,
                    disableNoVersion,
                    enableOnDownload,
                    current.second.versions,
                    types,
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
                TextBox(
                    text = searchContent,
                    onTextChanged = {
                        searchContent = it
                    },
                    placeholder = strings().manager.mods.searchPlaceholder(),
                    trailingIcon = {
                        Icon(
                            icons().search,
                            "Search"
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Box(
                    modifier = Modifier
                        .weight(1f, false),
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            if(filteredMods.isEmpty()) {
                                Text(
                                    strings().manager.mods.empty(),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        items(filteredMods) { mod ->
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

                    ComboBox(
                        items = VersionType.entries.filter { it != VersionType.VANILLA },
                        selected = selectedType,
                        onSelected = {
                            selectedType = it
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    )

                    TitledCheckBox(
                        title = strings().creator.version.showSnapshots(),
                        checked = showSnapshots,
                        onCheckedChange = {
                            showSnapshots = it
                        }
                    )

                    if(selectedType == VersionType.QUILT) {
                        TitledCheckBox(
                            title = strings().creator.mods.quiltIncludeFabric(),
                            checked = includeAlternateLoader,
                            onCheckedChange = {
                                includeAlternateLoader = it
                            }
                        )
                    }


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
                                                    current.second.versions = listOf(v.id)
                                                    current.second.types = if(selectedType == VersionType.QUILT && includeAlternateLoader) {
                                                        listOf(VersionType.QUILT.id, VersionType.FABRIC.id)
                                                    } else {
                                                        listOf(selectedType.id)
                                                    }

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
                        icon = icons().change,
                        enabled = selectedVersion?.let { it.id != current.second.versions[0] } ?: false
                                || selectedType != types[0]
                                || includeAlternateLoader != types.size > 1,
                        modifier = Modifier
                            .padding(start = 4.dp)
                    )
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
                    icon = icons().add,
                    size = 32.dp,
                    tooltip = strings().manager.mods.add()
                )

                IconButton(
                    onClick = {
                        checkUpdates = true
                    },
                    icon = icons().update,
                    size = 32.dp,
                    tooltip = strings().manager.mods.update.tooltip(),
                    enabled = !checkUpdates
                )
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
                        icon = icons().back,
                        size = 32.dp,
                        tooltip = strings().manager.mods.addMods.back()
                    )
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