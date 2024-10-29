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
import dev.treset.mcdl.exception.FileDownloadException
import dev.treset.mcdl.minecraft.MinecraftVersion
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.config.AppSettings
import dev.treset.treelauncher.backend.data.LauncherMod
import dev.treset.treelauncher.backend.data.manifest.ModsComponent
import dev.treset.treelauncher.backend.util.assignFrom
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.backend.util.sort.LauncherModSortType
import dev.treset.treelauncher.components.Components
import dev.treset.treelauncher.generic.*
import dev.treset.treelauncher.generic.Button
import dev.treset.treelauncher.generic.Text
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.disabledContainer
import dev.treset.treelauncher.style.icons
import dev.treset.treelauncher.style.inverted
import dev.treset.treelauncher.util.ListDisplay
import java.io.IOException
import java.net.URI


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
        isEnabled = { id != AppContext.runningInstance?.modsComponent?.value?.id },
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
            val listDisplay = remember(current.listDisplay.value) { current.listDisplay.value ?: AppContext.files.modsManifest.defaultListDisplay.value }

            val types = remember(current.types.toList()) {
                VersionType.fromIds(current.types)
            }

            var searchContent by remember { mutableStateOf("") }

            var versions: List<MinecraftVersion> by remember(current) { mutableStateOf(emptyList()) }
            var showSnapshots by remember(current) { mutableStateOf(false) }
            var selectedVersion: MinecraftVersion? by remember(current) { mutableStateOf(null) }
            var selectedType: VersionType by remember(types) { mutableStateOf(types[0]) }
            var includeAlternateLoader by remember(types) { mutableStateOf(
                types[0] == VersionType.QUILT && types.size > 1
            ) }

            var popupData: PopupData? by remember { mutableStateOf(null) }

            val mods: List<LauncherMod> = remember(current.mods.toList(), AppSettings.modSortType.value, AppSettings.isModSortReverse.value, current.mods.toList(), current.versions.toList()) {
                current.mods.sortedWith(AppSettings.modSortType.value.comparator).let {
                    if(AppSettings.isModSortReverse.value) it.reversed() else it
                }
            }

            val filteredMods = remember(mods, searchContent) {
                mods.filter {
                    it.name.value.contains(searchContent, true)
                }
            }

            val neverViewedMods = remember(filteredMods) {
                filteredMods.toMutableStateList()
            }

            val notVisibleMods = remember(filteredMods.count { it.visible.value }) {
                neverViewedMods -= filteredMods.filter { it.visible.value }.toSet()
                filteredMods.filterNot { it.visible.value }
            }

            val downloadingMods = remember(filteredMods.count { it.downloading.value }) {
                filteredMods.filter { it.downloading.value }
            }

            val updateAvailableMods = remember(filteredMods.count { it.updateAvailable.value == true }) {
                filteredMods.filter { it.updateAvailable.value == true }
            }

            val findingOrAvailableMods = remember(filteredMods.count { it.updateAvailable.value != false }) {
                filteredMods.filter { it.updateAvailable.value != false }
            }

            var showUpdateBanner by remember { mutableStateOf(false) }

            DisposableEffect(current) {
                selected = current

                onDispose {
                    try {
                        selected?.write()
                    } catch(e: IOException) {
                        AppContext.error(e)
                    }
                    selected = null
                }
            }

            LaunchedEffect(current.mods.toList(), current.versions.toList(), current.types.toList(), current.providers.toList()) {
                current.mods.forEach {
                    it.initializeDisplay(current)
                }
            }

            LaunchedEffect(current, showSnapshots) {
                Thread {
                    versions = try {
                        if (showSnapshots) {
                            MinecraftVersion.getAll()
                        } else {
                            MinecraftVersion.getAll().filter { it.isRelease }
                        }.also { v ->
                            selectedVersion = v.firstOrNull {
                                it.id == current.versions.firstOrNull()
                            }
                        }
                    } catch (e: FileDownloadException) {
                        AppContext.error(e)
                        emptyList()
                    }
                }.start()
            }

            LaunchedEffect(checkUpdates) {
                if(checkUpdates > 0) {
                    filteredMods.forEach {
                        it.checkForUpdates(current)

                        neverViewedMods.assignFrom(filteredMods.filterNot { it.visible.value })
                        Thread {
                            showUpdateBanner = true
                            Thread.sleep(3000)
                            showUpdateBanner = false
                        }.start()
                    }
                }
            }

            editingMod?.let {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    ModsEdit(
                        current,
                        it,
                        droppedFile = droppedFile
                    ) {
                        editingMod = null
                    }
                }
            } ?: if(showSearch) {
                ModsSearch(
                    current,
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

                            items(filteredMods) {
                                it.ModButton(
                                    current,
                                    display = listDisplay
                                ) {
                                    editingMod = it
                                }
                            }
                        }

                        val readyBannerMods = updateAvailableMods.intersect(neverViewedMods.toSet())
                        androidx.compose.animation.AnimatedVisibility(
                            visible = readyBannerMods.isNotEmpty(),
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
                                    Strings.manager.mods.update.notViewed(readyBannerMods.size),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            }
                        }

                        val updateBannerMods = downloadingMods.intersect(notVisibleMods.toSet())
                        androidx.compose.animation.AnimatedVisibility(
                            visible = updateBannerMods.isNotEmpty(),
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
                                    Strings.manager.mods.update.remaining(updateBannerMods.size),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            }
                        }

                        androidx.compose.animation.AnimatedVisibility(
                            visible = showUpdateBanner && findingOrAvailableMods.isEmpty(),
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
                                                current.registerJob {
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
        actionBarSpecial = { current, settingsOpen, _ ->
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

                    Box(
                        contentAlignment = Alignment.Center,
                    ) {
                        IconButton(
                            onClick = {
                                checkUpdates++
                            },
                            tooltip = Strings.manager.mods.update.tooltip(),
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    icons().update,
                                    "Update",
                                    modifier = Modifier.size(32.dp)
                                )
                                if(current.autoUpdate.value) {
                                    Icon(
                                        icons().auto,
                                        "Auto",
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .size(18.dp)
                                            .offset(y = 4.dp, x = (-4).dp)
                                    )
                                }
                            }
                        }
                    }

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
                                current.autoUpdate.value,
                                {
                                    current.autoUpdate.value = it
                                    AppSettings.modsDefaultAutoUpdate.value = it
                                }
                            )

                            if(current.autoUpdate.value) {
                                TitledCheckBox(
                                    Strings.manager.mods.update.enable(),
                                    current.enableOnUpdate.value,
                                    {
                                        current.enableOnUpdate.value = it
                                        AppSettings.modsDefaultEnableOnUpdate.value = it
                                    }
                                )
                            }

                            TitledCheckBox(
                                Strings.manager.mods.update.disable(),
                                current.disableOnNoVersion.value,
                                {
                                    current.disableOnNoVersion.value = it
                                    AppSettings.modsDefaultDisableOnNoVersion.value = it
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
                            current.providers.forEach {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            current.providers.moveApplicableDirection(it)
                                            AppSettings.modsDefaultProviders.assignFrom(current.providers.copyOrder())
                                        },
                                        icon = icons().down,
                                        size = 20.dp,
                                        tooltip = Strings.manager.mods.settings.order(current.providers.canMoveDown(it)),
                                        modifier = Modifier.rotate(if(current.providers.canMoveDown(it)) 0f else 180f)
                                    )

                                    IconButton(
                                        onClick = {
                                            it.enabled.value = !it.enabled.value
                                        },
                                        icon = if(it.enabled.value) icons().minus else icons().plus,
                                        size = 20.dp,
                                        tooltip = Strings.manager.mods.settings.state(it.enabled.value),
                                        enabled = !it.enabled.value || current.providers.find { it.enabled.value } != null
                                    )

                                    Text(
                                        Strings.manager.mods.settings.modProvider(it.provider),
                                        style = LocalTextStyle.current.let { style ->
                                            if(!it.enabled.value) {
                                                style.copy(
                                                    color = LocalTextStyle.current.color.copy(alpha = 0.8f).inverted(),
                                                    fontStyle = FontStyle.Italic,
                                                    textDecoration = TextDecoration.LineThrough
                                                )
                                            } else style
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        actionBarBoxContent = { current, settingsOpen, _ ->
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
                    displays = ListDisplay.entries,
                    selected = current.listDisplay,
                    default = AppContext.files.modsManifest.defaultListDisplay,
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
        }
    )
}