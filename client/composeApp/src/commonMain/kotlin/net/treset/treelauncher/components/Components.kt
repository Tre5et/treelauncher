package net.treset.treelauncher.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.DragData
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.config.ComponentManifestSortType
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.creation.GenericComponentCreator
import net.treset.treelauncher.backend.data.LauncherInstanceDetails
import net.treset.treelauncher.backend.data.manifest.ComponentManifest
import net.treset.treelauncher.backend.data.manifest.LauncherManifestType
import net.treset.treelauncher.backend.data.manifest.ParentManifest
import net.treset.treelauncher.backend.util.CreationStatus
import net.treset.treelauncher.backend.util.exception.ComponentCreationException
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.creation.ComponentCreator
import net.treset.treelauncher.creation.CreationPopup
import net.treset.treelauncher.creation.CreationState
import net.treset.treelauncher.generic.*
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.icons
import java.io.IOException

data class SortContext(
    val getSortType: () -> ComponentManifestSortType,
    val setSortType: (ComponentManifestSortType) -> Unit,
    val getReverse: () -> Boolean,
    val setReverse: (Boolean) -> Unit
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <T, C:CreationState<T>> Components(
    title: String,
    components: List<T>,
    componentManifest: ParentManifest,
    checkHasComponent: (LauncherInstanceDetails, ComponentManifest) -> Boolean,
    getManifest: T.() -> ComponentManifest,
    isEnabled: T.() -> Boolean = {true},
    getCreator: (C) -> GenericComponentCreator?,
    reload: () -> Unit,
    createContent: @Composable ColumnScope.(onCreate: (C) -> Unit) -> Unit,
    actionBarSpecial: @Composable RowScope.(
        selected: T,
        settingsOpen: Boolean,
        redraw: () -> Unit,
        reload: () -> Unit
    ) -> Unit = {_,_,_,_->},
    actionBarBoxContent: @Composable BoxScope.(
        selected: T,
        settingsOpen: Boolean,
        redraw: () -> Unit,
        reload: () -> Unit
    ) -> Unit = {_,_,_,_->},
    detailsContent: @Composable ColumnScope.(
        selected: T,
        redraw: () -> Unit,
        reload: () -> Unit
    ) -> Unit = {_,_,_->},
    detailsOnDrop: ((DragData) -> Unit)? = null,
    detailsScrollable: Boolean = true,
    settingsDefault: Boolean = false,
    sortContext: SortContext? = null
) {
    var selected: T? by remember(components) { mutableStateOf(null) }

    var creatorSelected by remember { mutableStateOf(false) }

    var creationStatus: CreationStatus? by remember { mutableStateOf(null) }

    val redrawSelected: () -> Unit = {
        selected?.let {
            selected = null
            selected = it
        }
    }

    var sortType: ComponentManifestSortType by remember(sortContext) { mutableStateOf(sortContext?.getSortType?.let { it() } ?: ComponentManifestSortType.LAST_USED) }
    var sortReversed: Boolean by remember(sortContext) { mutableStateOf(sortContext?.getReverse?.let { it() } ?: false) }

    val actualComponents: List<T> = remember(components, sortType, sortReversed, AppContext.runningInstance) {
        components
            .sortedWith { o1, o2 ->
                sortType.comparator.compare(o1.getManifest(), o2.getManifest())
            }.let {
                if (sortReversed) {
                    it.reversed()
                } else {
                    it
                }
            }
    }

    LaunchedEffect(Unit) {
        reload()
    }


    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {

        TitledColumn(
            headerContent = {
                Text(title)

                sortContext?.let {
                    SortBox(
                        sorts = ComponentManifestSortType.entries,
                        reversed = it.getReverse(),
                        selected = it.getSortType(),
                        onReversed = {
                            it.setReverse(!sortReversed)
                            sortReversed = !sortReversed
                        },
                        onSelected = { new ->
                            it.setSortType(new)
                            sortType = new
                        },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            },
            modifier = Modifier.padding(12.dp),
            parentModifier = Modifier.fillMaxWidth(1 / 2f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            scrollable = false,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f, false)
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(actualComponents) { component ->
                        ComponentButton(
                            component = component.getManifest(),
                            selected = component == selected,
                            enabled = component.isEnabled(),
                            onClick = {
                                creatorSelected = false
                                selected = if(component == selected) {
                                    null
                                } else {
                                    component
                                }
                            }
                        )
                    }
                }
            }

            SelectorButton(
                title = strings().components.create(),
                icon = icons().add,
                selected = creatorSelected,
                onClick = {
                    selected = null
                    creatorSelected = !creatorSelected
                }
            )
        }

        selected?.let {
            var showSettings by remember(it) { mutableStateOf(settingsDefault) }

            var showRename by remember { mutableStateOf(false) }

            var showDelete by remember { mutableStateOf(false) }

            TitledColumn(
                headerContent = {
                    if(showSettings && !settingsDefault) {
                        Box(
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            IconButton(
                                onClick = { showSettings = false },
                                icon = icons().back,
                                size = 32.dp,
                                tooltip = strings().manager.component.back(),
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        actionBarSpecial(
                            it,
                            showSettings,
                            redrawSelected,
                            reload
                        )

                        Text(it.getManifest().name)

                        IconButton(
                            onClick = {
                                showRename = true
                            },
                            icon = icons().edit,
                            size = 32.dp,
                            tooltip = strings().selector.component.rename.title()
                        )
                        IconButton(
                            onClick = {
                                LauncherFile.of(it.getManifest().directory).open()
                            },
                            icon = icons().folder,
                            size = 32.dp,
                            tooltip = strings().selector.component.openFolder()
                        )
                        IconButton(
                            onClick = {
                                showDelete = true
                            },
                            icon = icons().delete,
                            size = 32.dp,
                            interactionTint = MaterialTheme.colorScheme.error,
                            tooltip = strings().selector.component.delete.tooltip()
                        )
                    }

                    actionBarBoxContent(
                        it,
                        showSettings,
                        redrawSelected,
                        reload
                    )
                },
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(12.dp),
                scrollable = false
            ) {
                val columnContent: @Composable () -> Unit = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f, false)
                                .let {mod ->
                                    if(detailsScrollable) {
                                        mod.verticalScroll(rememberScrollState())
                                    } else {
                                        mod
                                    }
                                },
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            detailsContent(
                                it,
                                redrawSelected,
                                reload
                            )
                        }

                        SelectorButton(
                            title = strings().manager.component.settings(),
                            icon = icons().settings,
                            selected = showSettings,
                            onClick = { showSettings = true }
                        )
                    }
                }

                if(showSettings) {
                    ComponentSettings(
                        it.getManifest(),
                    )
                } else {
                    detailsOnDrop?.let {
                        DroppableArea(
                            onDrop = it,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            columnContent()
                        }
                    } ?: columnContent()
                }
            }


            if (showRename) {
                RenamePopup(
                    manifest = it.getManifest(),
                    editValid = { name -> name.isNotBlank() && name != it.getManifest().name },
                    onDone = { name ->
                        showRename = false
                        name?.let { newName ->
                            it.getManifest().name = newName
                            try {
                                LauncherFile.of(
                                    it.getManifest().directory,
                                    appConfig().manifestFileName
                                ).write(it.getManifest())
                            } catch (e: IOException) {
                                AppContext.severeError(e)
                            }
                            redrawSelected()
                        }
                    }
                )
            }

            if (showDelete) {
                DeletePopup(
                    component = it.getManifest(),
                    checkHasComponent = { details -> checkHasComponent(details, it.getManifest()) },
                    onClose = { showDelete = false },
                    onConfirm = {
                        componentManifest.components.remove(it.getManifest().id)
                        try {
                            LauncherFile.of(
                                componentManifest.directory,
                                when(componentManifest.type) {
                                    LauncherManifestType.SAVES -> "saves.json"
                                    LauncherManifestType.MODS -> "mods.json"
                                    else -> appConfig().manifestFileName
                                }
                            ).write(componentManifest)
                            LauncherFile.of(it.getManifest().directory).remove()
                        } catch(e: IOException) {
                            AppContext.severeError(e)
                        }
                        reload()
                        showDelete = false
                    }
                )
            }

        }

        if(creatorSelected) {
            TitledColumn(
                title = strings().components.create(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(12.dp)
            ) {
                createContent { state ->
                    val creator = getCreator(state)

                    creator?.let { creation ->
                        creation.statusCallback = {
                            creationStatus = it
                        }

                        Thread {
                            try {
                                creation.execute()
                            } catch (e: ComponentCreationException) {
                                AppContext.error(e)
                            }
                            reload()
                            creationStatus = null
                        }.start()
                    }
                }
            }
        }

        creationStatus?.let {
            CreationPopup(it)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Components(
    title: String,
    componentManifest: ParentManifest,
    components: List<ComponentManifest>,
    checkHasComponent: (LauncherInstanceDetails, ComponentManifest) -> Boolean,
    isEnabled: ComponentManifest.() -> Boolean = {true},
    getCreator: (CreationState<ComponentManifest>) -> GenericComponentCreator?,
    reload: () -> Unit,
    actionBarSpecial: @Composable RowScope.(
        ComponentManifest,
        Boolean,
        () -> Unit,
        () -> Unit
    ) -> Unit = {_,_,_,_->},
    actionBarBoxContent: @Composable BoxScope.(
        selected: ComponentManifest,
        settingsOpen: Boolean,
        redraw: () -> Unit,
        reload: () -> Unit
    ) -> Unit = {_,_,_,_->},
    detailsContent: @Composable ColumnScope.(
        selected: ComponentManifest,
        redraw: () -> Unit,
        reload: () -> Unit
    ) -> Unit = {_,_,_->},
    detailsOnDrop: ((DragData) -> Unit)? = null,
    detailsScrollable: Boolean = false,
    settingsDefault: Boolean = false,
    sortContext: SortContext? = null
) = Components(
    title,
    components,
    componentManifest,
    checkHasComponent,
    { this },
    isEnabled,
    getCreator,
    reload,
    { onCreate ->
        ComponentCreator(
            existing = components.toList(),
            toDisplayString = { name },
            onCreate = onCreate,
            allowUse = false
        )
    },
    actionBarSpecial,
    actionBarBoxContent,
    detailsContent,
    detailsOnDrop,
    detailsScrollable,
    settingsDefault,
    sortContext
)