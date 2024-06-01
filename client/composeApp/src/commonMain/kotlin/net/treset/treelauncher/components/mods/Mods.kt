package net.treset.treelauncher.components.mods

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.mc_version_loader.launcher.LauncherMod
import net.treset.mc_version_loader.launcher.LauncherModsDetails
import net.treset.mc_version_loader.minecraft.MinecraftGame
import net.treset.mc_version_loader.minecraft.MinecraftVersion
import net.treset.mc_version_loader.mods.ModProvider
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.config.LauncherModSortType
import net.treset.treelauncher.backend.config.appSettings
import net.treset.treelauncher.backend.creation.ModsCreator
import net.treset.treelauncher.backend.util.EmptyingJobQueue
import net.treset.treelauncher.backend.util.exception.FileLoadException
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.components.Components
import net.treset.treelauncher.components.SortContext
import net.treset.treelauncher.components.mods.display.LauncherModDisplay
import net.treset.treelauncher.components.mods.display.ModDataProvider
import net.treset.treelauncher.creation.CreationMode
import net.treset.treelauncher.generic.*
import net.treset.treelauncher.generic.Button
import net.treset.treelauncher.generic.Text
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.icons
import net.treset.treelauncher.style.inverted
import net.treset.treelauncher.util.DetailsListDisplay

data class ModContext(
    val autoUpdate: Boolean,
    val disableNoVersion: Boolean,
    val enableOnDownload: Boolean,
    val versions: List<String>,
    val types: List<VersionType>,
    val providers: List<ModProvider>,
    val directory: LauncherFile,
    val registerChangingJob: ((MutableList<LauncherMod>) -> Unit) -> Unit,
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Mods() {
    var components by remember { mutableStateOf(AppContext.files.modsComponents.sortedBy { it.first.name }) }

    var selected: Pair<LauncherManifest, LauncherModsDetails>? by remember { mutableStateOf(null) }

    var showSearch by remember(selected) { mutableStateOf(false) }
    var checkUpdates by remember(selected) { mutableStateOf(0) }

    var sort: LauncherModSortType by remember { mutableStateOf(appSettings().modSortType) }
    var reverse by remember { mutableStateOf(appSettings().isModSortReverse) }

    var editingMod: LauncherMod? by remember(selected) { mutableStateOf(null) }

    var listDisplay by remember { mutableStateOf(appSettings().modDetailsListDisplay) }

    var modrinth by remember { mutableStateOf(appSettings().isModsModrinth) }
    var curseforge by remember { mutableStateOf(appSettings().isModsCurseforge) }

    Components(
        title = strings().selector.mods.title(),
        components = components,
        componentManifest = AppContext.files.modsManifest,
        checkHasComponent = { details, component -> details.modsComponent == component.id },
        getManifest = { first },
        isEnabled = { first.id != AppContext.runningInstance?.modsComponent?.first?.id },
        getCreator = { state: ModsCreationState ->
            when(state.mode) {
                CreationMode.NEW -> state.name?.let{ state.version?.let { state.type?.let { state.alternateLoader?.let {
                    ModsCreator(
                        state.name,
                        AppContext.files.launcherDetails.typeConversion,
                        AppContext.files.modsManifest,
                        if(state.alternateLoader && state.type == VersionType.QUILT) {
                            listOf(VersionType.QUILT.id, VersionType.FABRIC.id)
                        } else {
                            listOf(state.type.id)
                        },
                        listOf(state.version.id),
                        AppContext.files.gameDetailsManifest
                    )
                }}}}
                CreationMode.INHERIT -> state.name?.let{ state.existing?.let {
                    ModsCreator(
                        state.name,
                        state.existing,
                        AppContext.files.modsManifest,
                        AppContext.files.gameDetailsManifest
                    )
                }}
                CreationMode.USE -> null
            }
        },
        reload = {
            try {
                AppContext.files.reloadModsManifest()
                AppContext.files.reloadModsComponents()
                components = AppContext.files.modsComponents.sortedBy { it.first.name }
            } catch (e: FileLoadException) {
                AppContext.severeError(e)
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

            val mods: List<LauncherMod> = remember(sort, reverse, redrawMods, modrinth, curseforge, current.second.mods.size, current.second.versions) {
                current.second.mods.sortedWith(sort.comparator).let {
                    if(reverse) it.reversed() else it
                }
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

            val modContext = remember(current, current.second.versions, types, autoUpdate, disableNoVersion, enableOnDownload, modrinth, curseforge) {
                ModContext(
                    autoUpdate,
                    disableNoVersion,
                    enableOnDownload,
                    current.second.versions,
                    types,
                    listOfNotNull(
                        if(modrinth) ModProvider.MODRINTH else null,
                        if(curseforge) ModProvider.CURSEFORGE else null
                    ),
                    LauncherFile.of(current.first.directory)
                ) { element ->
                    updateQueue.add(element)
                }
            }

            val filteredMods = remember(mods, searchContent, modContext) {
                mods.filter {
                    it.name.contains(searchContent, true)
                }.map {
                    LauncherModDisplay(it, modContext)
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

            DisposableEffect(current) {
                onDispose {
                    updateQueue.finish()
                }
            }

            LaunchedEffect(checkUpdates) {
                if(checkUpdates > 0) {
                    filteredMods.forEach {
                        it.checkForUpdates()
                    }
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
                    AppContext
                ) {
                    showSearch = false
                }
            } else {
                if(mods.isEmpty()) {
                    Column(
                        modifier = Modifier.weight(1f, true).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            strings().selector.mods.emptyTitle(),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            strings().selector.mods.empty().let {
                                Text(it.first)
                                Icon(
                                    icons().add,
                                    "Add",
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(it.second)
                            }
                        }
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
                        modifier = Modifier.weight(1f, true),
                    ) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                if (filteredMods.isEmpty()) {
                                    Text(
                                        strings().manager.mods.empty(),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            items(filteredMods) { mod ->
                                ModDataProvider(
                                    element = mod
                                ) {
                                    ModButton(
                                        display = listDisplay
                                    ) {

                                        editingMod = mod.mod
                                    }
                                }
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
                var updateExpanded by remember { mutableStateOf(false) }
                val updateRotation by animateFloatAsState(if(updateExpanded) 180f else 0f)

                var settingsExpanded by remember { mutableStateOf(false) }
                val settingsRotation by animateFloatAsState(if(settingsExpanded) 90f else 0f)

                IconButton(
                    onClick = {
                        showSearch = true
                    },
                    icon = icons().add,
                    size = 32.dp,
                    tooltip = strings().manager.mods.add()
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(end = 10.dp)
                ) {
                    IconButton(
                        onClick = {
                            updateExpanded = !updateExpanded
                        },
                        icon = icons().expand,
                        size = 24.dp,
                        tooltip = strings().manager.mods.update.settings(),
                        modifier = Modifier
                            .offset(x = 20.dp)
                            .rotate(updateRotation)
                    )

                    IconButton(
                        onClick = {
                            checkUpdates++
                        },
                        icon = icons().update,
                        size = 32.dp,
                        tooltip = strings().manager.mods.update.tooltip(),
                    )

                    DropdownMenu(
                        expanded = updateExpanded,
                        onDismissRequest = { updateExpanded = false },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(end = 8.dp)
                    ) {
                        ProvideTextStyle(
                            MaterialTheme.typography.bodyMedium
                        ) {

                            var update by remember { mutableStateOf(appSettings().isModsUpdate) }
                            var enable by remember { mutableStateOf(appSettings().isModsEnable) }
                            var disable by remember { mutableStateOf(appSettings().isModsDisable) }

                            TitledCheckBox(
                                strings().manager.mods.update.auto(),
                                update,
                                {
                                    update = it
                                    appSettings().isModsUpdate = it
                                    if (!it) {
                                        enable = false
                                        appSettings().isModsEnable = false
                                    }
                                }
                            )

                            if(update) {
                                TitledCheckBox(
                                    strings().manager.mods.update.enable(),
                                    enable,
                                    {
                                        enable = it
                                        appSettings().isModsEnable = it
                                    }
                                )
                            }

                            TitledCheckBox(
                                strings().manager.mods.update.disable(),
                                disable,
                                {
                                    disable = it
                                    appSettings().isModsDisable = it
                                }
                            )
                        }
                    }
                }

                Box(
                    contentAlignment = Alignment.Center,
                ) {
                    IconButton(
                        onClick = {
                            settingsExpanded = !settingsExpanded
                        },
                        icon = icons().settings,
                        size = 24.dp,
                        tooltip = strings().manager.mods.settings.tooltip(),
                        modifier = Modifier.rotate(settingsRotation)
                    )

                    DropdownMenu(
                        expanded = settingsExpanded,
                        onDismissRequest = { settingsExpanded = false },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(
                            strings().manager.mods.settings.providers(),
                            style = MaterialTheme.typography.titleSmall,
                        )

                        ProvideTextStyle(
                            MaterialTheme.typography.bodyMedium
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        modrinth = !modrinth
                                        appSettings().isModsModrinth = modrinth
                                    },
                                    icon = if(modrinth) icons().minus else icons().plus,
                                    size = 20.dp,
                                    tooltip = strings().manager.mods.settings.state(modrinth),
                                    enabled = !modrinth || curseforge
                                )

                                Text(
                                    strings().manager.mods.settings.modrinth(),
                                    style = LocalTextStyle.current.let {
                                        if(!modrinth) {
                                            it.copy(
                                                color = LocalTextStyle.current.color.copy(alpha = 0.8f).inverted(),
                                                fontStyle = FontStyle.Italic,
                                                textDecoration = TextDecoration.LineThrough
                                            )
                                        } else it
                                    }
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        curseforge = !curseforge
                                        appSettings().isModsCurseforge = curseforge
                                    },
                                    icon = if(curseforge) icons().minus else icons().plus,
                                    size = 20.dp,
                                    tooltip = strings().manager.mods.settings.state(curseforge),
                                    enabled = !curseforge || modrinth
                                )

                                Text(
                                    strings().manager.mods.settings.curseforge(),
                                    style = LocalTextStyle.current.let {
                                        if(!curseforge) {
                                            it.copy(
                                                color = LocalTextStyle.current.color.copy(alpha = 0.8f).inverted(),
                                                fontStyle = FontStyle.Italic,
                                                textDecoration = TextDecoration.LineThrough
                                            )
                                        } else it
                                    }
                                )
                            }

                            /*TitledCheckBox(
                                strings().manager.mods.modrinth(),
                                modrinth,
                                {
                                    modrinth = it
                                    appSettings().isModsModrinth = it
                                },
                                textColor = if (!modrinth && !curseforge) MaterialTheme.colorScheme.error else LocalContentColor.current
                            )

                            TitledCheckBox(
                                strings().manager.mods.curseforge(),
                                curseforge,
                                {
                                    curseforge = it
                                    appSettings().isModsCurseforge = it
                                },
                                textColor = if (!modrinth && !curseforge) MaterialTheme.colorScheme.error else LocalContentColor.current,
                                modifier = Modifier.offset(y = (-12).dp)
                            )*/
                        }
                    }
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

                ListDisplayBox(
                    displays = DetailsListDisplay.entries,
                    selected = listDisplay,
                    onSelected = {
                        listDisplay = it
                        appSettings().modDetailsListDisplay = it
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 18.dp)
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