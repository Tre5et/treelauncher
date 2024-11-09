package dev.treset.treelauncher.components.mods

import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.treset.mcdl.exception.FileDownloadException
import dev.treset.mcdl.minecraft.MinecraftVersion
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.data.LauncherMod
import dev.treset.treelauncher.backend.util.assignFrom
import dev.treset.treelauncher.backend.util.sort.sorted
import dev.treset.treelauncher.generic.*
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.disabledContainer
import dev.treset.treelauncher.style.icons

@Composable
fun SharedModsData.ModsDetails(scope: ColumnScope) {
    val listDisplay = remember(component.listDisplay.value) { component.listDisplay.value ?: AppContext.files.modsManifest.defaultListDisplay.value }

    val types = remember(component.types.toList()) {
        VersionType.fromIds(component.types)
    }

    var searchContent by remember { mutableStateOf("") }

    var versions: List<MinecraftVersion> by remember(component) { mutableStateOf(emptyList()) }
    var showSnapshots by remember(component) { mutableStateOf(false) }
    var selectedVersion: MinecraftVersion? by remember(component) { mutableStateOf(null) }
    var selectedType: VersionType by remember(types) { mutableStateOf(types[0]) }
    var includeAlternateLoader by remember(types) { mutableStateOf(
        (types[0] == VersionType.QUILT || types[0] == VersionType.NEO_FORGE)
                && types.size > 1
    ) }

    var popupData: PopupData? by remember { mutableStateOf(null) }

    val mods: List<LauncherMod> = remember(component.mods.toList(), component.sort.provider.value, component.sort.reverse.value, component.mods.toList(), component.versions.toList()) {
        component.mods.sorted(component.sort)
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

    var showUpdateBanner by remember(component) { mutableStateOf(false) }

    LaunchedEffect(component.mods.toList(), component.versions.toList(), component.types.toList(), component.providers.toList()) {
        component.mods.forEach {
            it.initializeDisplay(component)
        }
    }

    LaunchedEffect(component, showSnapshots) {
        Thread {
            versions = try {
                if (showSnapshots) {
                    MinecraftVersion.getAll()
                } else {
                    MinecraftVersion.getAll().filter { it.isRelease }
                }.also { v ->
                    selectedVersion = v.firstOrNull {
                        it.id == component.versions.firstOrNull()
                    }
                }
            } catch (e: FileDownloadException) {
                AppContext.error(e)
                emptyList()
            }
        }.start()
    }

    LaunchedEffect(checkUpdates.value) {
        if(checkUpdates.value > 0) {
            filteredMods.forEach {
                it.checkForUpdates(component)

                neverViewedMods.assignFrom(filteredMods.filterNot { it.visible.value })
                Thread {
                    showUpdateBanner = true
                    Thread.sleep(3000)
                    showUpdateBanner = false
                }.start()
            }
        }
    }

    editingMod.value?.let {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            ModsEdit(
                component,
                it,
                droppedFile = droppedFile.value
            ) {
                editingMod.value = null
            }
        }
    } ?: if(showSearch.value) {
        ModsSearch(
            component,
            droppedFile = droppedFile.value
        ) {
            showSearch.value = false
        }
    } else {
        LaunchedEffect(component) {
            droppedFile.value = null
        }

        if(mods.isEmpty()) {
            Column(
                modifier = with(scope) { Modifier.weight(1f, true).fillMaxWidth() },
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
                modifier = with(scope) { Modifier.weight(1f, true) },
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
                            component,
                            display = listDisplay
                        ) {
                            editingMod.value = it
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

            if(selectedType == VersionType.QUILT || selectedType == VersionType.NEO_FORGE) {
                TitledCheckBox(
                    title = Strings.creator.mods.includeAlternateLoader(selectedType),
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
                                        component.registerJob {
                                            component.versions.assignFrom(listOf(v.id))
                                            component.types.assignFrom(
                                                if(selectedType == VersionType.QUILT && includeAlternateLoader) {
                                                    listOf(VersionType.QUILT.id, VersionType.FABRIC.id)
                                                } else if(selectedType == VersionType.NEO_FORGE && includeAlternateLoader) {
                                                    listOf(VersionType.NEO_FORGE.id, VersionType.FORGE.id)
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
                enabled = selectedVersion?.let { it.id != component.versions.firstOrNull() } ?: false
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
}