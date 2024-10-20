package dev.treset.treelauncher.components.mods

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.DragData
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import dev.treset.mcdl.minecraft.MinecraftVersion
import dev.treset.mcdl.mods.ModProvider
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.config.AppSettings
import dev.treset.treelauncher.backend.config.LauncherModSortType
import dev.treset.treelauncher.backend.data.LauncherMod
import dev.treset.treelauncher.backend.data.manifest.ModsComponent
import dev.treset.treelauncher.backend.util.EmptyingJobQueue
import dev.treset.treelauncher.backend.util.assignFrom
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.components.Components
import dev.treset.treelauncher.components.SortContext
import dev.treset.treelauncher.components.mods.display.LauncherModDisplay
import dev.treset.treelauncher.components.mods.display.ModDataProvider
import dev.treset.treelauncher.generic.*
import dev.treset.treelauncher.generic.Button
import dev.treset.treelauncher.generic.Text
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.disabledContainer
import dev.treset.treelauncher.style.icons
import dev.treset.treelauncher.style.inverted
import dev.treset.treelauncher.util.DetailsListDisplay
import java.io.IOException
import java.net.URI

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

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Mods() {
    var selected: ModsComponent? by remember { mutableStateOf(null) }

    var showSearch by remember(selected) { mutableStateOf(false) }
    var checkUpdates by remember(selected) { mutableStateOf(0) }

    var editingMod: LauncherMod? by remember(selected) { mutableStateOf(null) }

    var droppedFile by remember { mutableStateOf<LauncherFile?>(null) }

    Components(
        title = Strings.selector.mods.title(),
        components = AppContext.files.modsComponents,
        componentManifest = AppContext.files.modsManifest,
        checkHasComponent = { details, component -> details.modsComponent == component.id },
        isEnabled = { id != AppContext.runningInstance?.modsComponent?.id },
        reload = {
            try {
                AppContext.files.reloadMods()
            } catch (e: IOException) {
                AppContext.severeError(e)
            }
        },
        createContent =  { onDone ->
            ModsCreation(
                components = AppContext.files.modsComponents,
                showUse = false,
                onDone = { onDone() }
            )
        },
        detailsContent = { current, _ ->
            val types = remember(current.types) {
                VersionType.fromIds(current.types)
            }

            var redrawMods by remember(current) { mutableStateOf(0) }

            var searchContent by remember { mutableStateOf("") }

            var versions: List<MinecraftVersion> by remember(current) { mutableStateOf(emptyList()) }
            var showSnapshots by remember(current) { mutableStateOf(false) }
            var selectedVersion: MinecraftVersion? by remember(current) { mutableStateOf(null) }
            var selectedType: VersionType by remember(types) { mutableStateOf(types[0]) }
            var includeAlternateLoader by remember(types) { mutableStateOf(
                !(types[0] == VersionType.QUILT && types.size <= 1)
            ) }

            var popupData: PopupData? by remember { mutableStateOf(null) }

            val mods: List<LauncherMod> = remember(AppSettings.modSortType.value, AppSettings.isModSortReverse.value, redrawMods, AppSettings.modrinthStatus.value, AppSettings.curseforgeStatus.value, current.mods.size, current.versions) {
                current.mods.sortedWith(AppSettings.modSortType.value.comparator).let {
                    if(AppSettings.isModSortReverse.value) it.reversed() else it
                }
            }

            val updateQueue = remember(current) {
                EmptyingJobQueue(
                    onEmptied = {
                        current.write()
                        redrawMods++
                    }
                ) {
                    current.mods
                }
            }

            val modContext = remember(current, current.versions, types, AppSettings.isModsUpdate.value, AppSettings.isModsDisable.value, AppSettings.isModsEnable.value, AppSettings.modrinthStatus.value, AppSettings.curseforgeStatus.value) {
                ModContext(
                    AppSettings.isModsUpdate.value,
                    AppSettings.isModsDisable.value,
                    AppSettings.isModsEnable.value,
                    current.versions,
                    types,
                    AppSettings.modProviders.filter { it.second }.map { it.first },
                    LauncherFile.of(current.directory, "mods")
                ) { element ->
                    updateQueue.add(element)
                }
            }

            val filteredMods = remember(mods, searchContent, modContext, showSearch) {
                mods.filter {
                    it.name.contains(searchContent, true)
                }.map {
                    LauncherModDisplay(it, modContext)
                }
            }

            var notViewedMods: List<LauncherModDisplay> by remember(filteredMods) { mutableStateOf(emptyList()) }
            val updateNotViewed = {
                notViewedMods = notViewedMods.filter { !it.visible }
            }

            var toUpdateMods: List<LauncherModDisplay> by remember(filteredMods) { mutableStateOf(emptyList()) }
            val updateToUpdate = {
                toUpdateMods = toUpdateMods.filter { it.downloading }
            }

            LaunchedEffect(filteredMods) {
                filteredMods.forEach {
                    it.onVisibility = {
                        updateNotViewed()
                    }
                    it.onDownloading = {
                        updateToUpdate()
                    }
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
                        MinecraftVersion.getAll()
                    } else {
                        MinecraftVersion.getAll().filter { it.isRelease }
                    }.also { v ->
                        selectedVersion = v.firstOrNull {
                            it.id == current.versions.firstOrNull()
                        }
                    }
                }.start()
            }

            DisposableEffect(current) {
                onDispose {
                    updateQueue.finish()
                }
            }

            var displayNoUpdates by remember(current) { mutableStateOf(false) }

            LaunchedEffect(checkUpdates) {
                if(checkUpdates > 0) {
                    filteredMods.filter {
                        it.checkForUpdates()
                    }.let {
                        if(it.isEmpty()) {
                            Thread {
                                displayNoUpdates = true
                                Thread.sleep(3000)
                                displayNoUpdates = false
                            }.start()
                        }

                        if(AppSettings.isModsUpdate.value) {
                            notViewedMods = emptyList()
                            toUpdateMods = it
                            updateToUpdate()
                        } else {
                            toUpdateMods = emptyList()
                            notViewedMods = it
                            updateNotViewed()
                        }
                    }
                }
            }

            editingMod?.let {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    ModsEdit(
                        modContext,
                        it,
                        droppedFile = droppedFile
                    ) {
                        editingMod = null
                    }
                }
            } ?: if(showSearch) {
                ModsSearch(
                    current,
                    modContext,
                    droppedFile = droppedFile
                ) {
                    showSearch = false
                }
            } else {
                LaunchedEffect(current) {
                    droppedFile = null
                }

                if(mods.isEmpty()) {
                    Column(
                        modifier = Modifier.weight(1f, true).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            Strings.selector.mods.emptyTitle(),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Strings.selector.mods.empty().let {
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
                        placeholder = Strings.manager.mods.searchPlaceholder(),
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
                                        Strings.manager.mods.empty(),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            items(filteredMods) { mod ->
                                ModDataProvider(
                                    element = mod
                                ) {
                                    ModButton(
                                        display = AppSettings.modDetailsListDisplay.value,
                                    ) {
                                        editingMod = mod.mod
                                    }
                                }
                            }
                        }

                        androidx.compose.animation.AnimatedVisibility(
                            visible = notViewedMods.isNotEmpty(),
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp, 4.dp, 0.dp, 0.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(start = 4.dp, end = 8.dp)
                            ) {
                                Icon(
                                    icons().down,
                                    "Down",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                )
                                Text(
                                    Strings.manager.mods.update.notViewed(notViewedMods.size),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            }
                        }

                        androidx.compose.animation.AnimatedVisibility(
                            visible = toUpdateMods.isNotEmpty(),
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp, 4.dp, 0.dp, 0.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    Strings.manager.mods.update.remaining(toUpdateMods.size),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            }
                        }

                        androidx.compose.animation.AnimatedVisibility(
                            visible = displayNoUpdates,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp, 4.dp, 0.dp, 0.dp))
                                    .border(2.dp, MaterialTheme.colorScheme.background.disabledContainer())
                                    .offset(y = 2.dp)
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .padding(horizontal = 8.dp)
                                    .padding(bottom = 2.dp)
                            ) {
                                Text(
                                    Strings.manager.mods.update.noUpdates(),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
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
                        title = Strings.creator.version.showSnapshots(),
                        checked = showSnapshots,
                        onCheckedChange = {
                            showSnapshots = it
                        }
                    )

                    if(selectedType == VersionType.QUILT) {
                        TitledCheckBox(
                            title = Strings.creator.mods.quiltIncludeFabric(),
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
                                    titleRow = { Text(Strings.manager.mods.change.title()) },
                                    content = {
                                        Text(Strings.manager.mods.change.message())
                                    },
                                    buttonRow = {
                                        Button(
                                            onClick = {
                                                popupData = null
                                            },
                                            color = MaterialTheme.colorScheme.error
                                        ) {
                                            Text(Strings.manager.mods.change.cancel())
                                        }

                                        Button(
                                            onClick = {
                                                modContext.registerChangingJob {
                                                    current.versions.assignFrom(listOf(v.id))
                                                    current.types.assignFrom(
                                                        if(selectedType == VersionType.QUILT && includeAlternateLoader) {
                                                            listOf(VersionType.QUILT.id, VersionType.FABRIC.id)
                                                        } else {
                                                            listOf(selectedType.id)
                                                        }
                                                    )

                                                    popupData = null
                                                }
                                            }
                                        ) {
                                            Text(Strings.manager.mods.change.confirm())
                                        }
                                    }
                                )
                            }
                        },
                        icon = icons().change,
                        enabled = selectedVersion?.let { it.id != current.versions.firstOrNull() } ?: false
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
        actionBarSpecial = { _, settingsOpen, _ ->
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
                    tooltip = Strings.manager.mods.add()
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
                        tooltip = Strings.manager.mods.update.settings(),
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
                        tooltip = Strings.manager.mods.update.tooltip(),
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

                            TitledCheckBox(
                                Strings.manager.mods.update.auto(),
                                AppSettings.isModsUpdate.value,
                                {
                                    AppSettings.isModsUpdate.value = it
                                    if (!it) {
                                        AppSettings.isModsEnable.value = false
                                    }
                                }
                            )

                            if(AppSettings.isModsUpdate.value) {
                                TitledCheckBox(
                                    Strings.manager.mods.update.enable(),
                                    AppSettings.isModsEnable.value,
                                    {
                                        AppSettings.isModsEnable.value = it
                                    }
                                )
                            }

                            TitledCheckBox(
                                Strings.manager.mods.update.disable(),
                                AppSettings.isModsDisable.value,
                                {
                                    AppSettings.isModsDisable.value = it
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
                        tooltip = Strings.manager.mods.settings.tooltip(),
                        modifier = Modifier.rotate(settingsRotation)
                    )

                    DropdownMenu(
                        expanded = settingsExpanded,
                        onDismissRequest = { settingsExpanded = false },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                Strings.manager.mods.settings.providers(),
                                style = MaterialTheme.typography.titleSmall,
                            )

                            val interactionSource = remember { MutableInteractionSource() }

                            val pressed by interactionSource.collectIsPressedAsState()
                            val hovered by interactionSource.collectIsHoveredAsState()
                            val nativeFocused by interactionSource.collectIsFocusedAsState()
                            val focused by remember(nativeFocused) { mutableStateOf(if(pressed) false else nativeFocused) }

                            val tooltipState = rememberTooltipState(
                                isPersistent = true
                            )

                            LaunchedEffect(focused) {
                                if(focused) {
                                    tooltipState.show(MutatePriority.UserInput)
                                } else {
                                    tooltipState.dismiss()
                                }
                            }

                            LaunchedEffect(hovered) {
                                if(hovered) {
                                    tooltipState.show(MutatePriority.UserInput)
                                } else {
                                    tooltipState.dismiss()
                                }
                            }

                            TooltipBox(
                                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                                tooltip = {
                                    PlainTooltip(
                                        caretProperties = TooltipDefaults.caretProperties,
                                    ) {
                                        Text(Strings.manager.mods.settings.help())
                                    }
                                },
                                state = tooltipState,
                                enableUserInput = false,
                            ) {
                                IconButton(
                                    onClick = {},
                                    icon = icons().help,
                                    size = 20.dp,
                                    interactionSource = interactionSource,
                                )
                            }
                        }

                        ProvideTextStyle(
                            MaterialTheme.typography.bodyMedium
                        ) {
                            AppSettings.modProviders.forEachIndexed { i,  provider ->
                                when(provider.first) {
                                    ModProvider.MODRINTH -> Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    AppSettings.modProviders = AppSettings.modProviders.reversed()
                                                },
                                                icon = icons().down,
                                                size = 20.dp,
                                                tooltip = Strings.manager.mods.settings.order(i == 0),
                                                modifier = Modifier.rotate(if(i == 0) 0f else 180f)
                                            )

                                            IconButton(
                                                onClick = {
                                                    AppSettings.modProviders = AppSettings.modProviders.map {
                                                        if(it.first == ModProvider.MODRINTH) {
                                                            it.first to !it.second
                                                        } else it
                                                    }
                                                },
                                                icon = if(provider.second) icons().minus else icons().plus,
                                                size = 20.dp,
                                                tooltip = Strings.manager.mods.settings.state(provider.second),
                                                enabled = !provider.second || AppSettings.modProviders.firstOrNull { it.first == ModProvider.CURSEFORGE }?.second ?: false
                                            )

                                            Text(
                                                Strings.manager.mods.settings.modrinth(),
                                                style = LocalTextStyle.current.let {
                                                    if(!provider.second) {
                                                        it.copy(
                                                            color = LocalTextStyle.current.color.copy(alpha = 0.8f).inverted(),
                                                            fontStyle = FontStyle.Italic,
                                                            textDecoration = TextDecoration.LineThrough
                                                        )
                                                    } else it
                                                }
                                            )
                                        }

                                    ModProvider.CURSEFORGE -> Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                AppSettings.modProviders = AppSettings.modProviders.reversed()
                                            },
                                            icon = icons().down,
                                            size = 20.dp,
                                            tooltip = Strings.manager.mods.settings.order(i == 0),
                                            modifier = Modifier.rotate(if(i == 0) 0f else 180f)
                                        )

                                        IconButton(
                                            onClick = {
                                                AppSettings.modProviders = AppSettings.modProviders.map {
                                                    if(it.first == ModProvider.CURSEFORGE) {
                                                        it.first to !it.second
                                                    } else it
                                                }
                                            },
                                            icon = if(provider.second) icons().minus else icons().plus,
                                            size = 20.dp,
                                            tooltip = Strings.manager.mods.settings.state(provider.second),
                                            enabled = !provider.second || AppSettings.modProviders.firstOrNull { it.first == ModProvider.MODRINTH }?.second ?: false
                                        )

                                        Text(
                                            Strings.manager.mods.settings.curseforge(),
                                            style = LocalTextStyle.current.let {
                                                if(!provider.second) {
                                                    it.copy(
                                                        color = LocalTextStyle.current.color.copy(alpha = 0.8f).inverted(),
                                                        fontStyle = FontStyle.Italic,
                                                        textDecoration = TextDecoration.LineThrough
                                                    )
                                                } else it
                                            }
                                        )
                                    }
                                }
                            }

                            /*TitledCheckBox(
                                Strings.manager.mods.modrinth(),
                                modrinth,
                                {
                                    modrinth = it
                                    appSettings().isModsModrinth = it
                                },
                                textColor = if (!modrinth && !curseforge) MaterialTheme.colorScheme.error else LocalContentColor.current
                            )

                            TitledCheckBox(
                                Strings.manager.mods.curseforge(),
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
        actionBarBoxContent = { _, settingsOpen, _ ->
            if(!settingsOpen && !showSearch && editingMod == null) {
                SortBox(
                    sorts = LauncherModSortType.entries,
                    modifier = Modifier.align(Alignment.CenterEnd),
                    selected = AppSettings.modSortType.value,
                    reversed = AppSettings.isModSortReverse.value,
                    onSelected = {
                        AppSettings.modSortType.value = it
                    },
                    onReversed = {
                        AppSettings.isModSortReverse.value = !AppSettings.isModSortReverse.value
                    }
                )

                ListDisplayBox(
                    displays = DetailsListDisplay.entries,
                    selected = AppSettings.modDetailsListDisplay.value,
                    onSelected = {
                        AppSettings.modDetailsListDisplay.value = it
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
                        tooltip = Strings.manager.mods.addMods.back()
                    )
                }
            }
        },
        detailsOnDrop = {
            if(it is DragData.FilesList) {
                it.readFiles().firstOrNull()?.let {
                    droppedFile = LauncherFile.of(URI(it).path)
                    if(editingMod == null && !showSearch) {
                        showSearch = true
                    }
                }
            }
        },
        sortContext = SortContext(
            getSortType = { AppSettings.modComponentSortType.value },
            setSortType = { AppSettings.modComponentSortType.value = it },
            getReverse = { AppSettings.isModComponentSortReverse.value },
            setReverse = { AppSettings.isModComponentSortReverse.value = it }
        )
    )
}