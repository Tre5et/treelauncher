package dev.treset.treelauncher.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragData
import androidx.compose.ui.unit.dp
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.data.manifest.Component
import dev.treset.treelauncher.backend.data.manifest.InstanceComponent
import dev.treset.treelauncher.backend.data.manifest.ParentManifest
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.backend.util.sort.ComponentSortProviders
import dev.treset.treelauncher.backend.util.sort.sorted
import dev.treset.treelauncher.generic.*
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.icons
import java.io.IOException

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <T: Component, D: SharedComponentData<T>> Components(
    title: String,
    components: MutableList<T>,
    componentManifest: ParentManifest,
    checkHasComponent: (InstanceComponent, T) -> Boolean,
    isEnabled: T.() -> Boolean = {true},
    reload: () -> Unit,
    constructSharedData: (T, () -> Unit) -> D,
    createContent: @Composable ColumnScope.(onDone: () -> Unit) -> Unit,
    actionBarSpecial: @Composable D.(RowScope) -> Unit = { },
    actionBarBoxContent: @Composable D.(BoxScope) -> Unit = { },
    detailsContent: @Composable D.(ColumnScope) -> Unit = { },
    detailsOnDrop: (D.(DragData.FilesList) -> Unit)? = null,
    detailsScrollable: D.() -> Boolean = { true },
    settingsDefault: Boolean = false
) {
    @Suppress("NAME_SHADOWING")
    val components = components.toList()

    var selected: T? by remember(components) { mutableStateOf(null) }

    var creatorSelected by remember { mutableStateOf(false) }

    val actualComponents: List<T> = remember(components, componentManifest.sort.provider.value, componentManifest.sort.reverse.value, AppContext.runningInstance) {
        components.sorted(componentManifest.sort)
    }

    val sharedData: D? = remember(selected) { selected?.let { constructSharedData(it, reload) } }

    DisposableEffect(Unit) {
        reload()

        onDispose {
            try {
                componentManifest.write()
            } catch(e: IOException) {
                AppContext.error(e)
            }
        }
    }


    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {

        TitledColumn(
            headerContent = {
                Text(title)

                SortBox(
                    sorts = ComponentSortProviders,
                    sort = componentManifest.sort,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
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
                            component = component,
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
                title = Strings.components.create(),
                icon = icons().add,
                selected = creatorSelected,
                onClick = {
                    selected = null
                    creatorSelected = !creatorSelected
                }
            )
        }

        sharedData?.let {
            var showRename by remember { mutableStateOf(false) }

            var showDelete by remember { mutableStateOf(false) }

            DisposableEffect(it.component) {
                onDispose {
                    try {
                        sharedData.component.write()
                    } catch (e: IOException) {
                        AppContext.error(e)
                    }
                }
            }

            TitledColumn(
                headerContent = {
                    if(it.settingsOpen.value && !settingsDefault) {
                        Box(
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            IconButton(
                                onClick = { it.settingsOpen.value = false },
                                icon = icons().back,
                                size = 32.dp,
                                tooltip = Strings.manager.component.back(),
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        sharedData.actionBarSpecial(this)

                        Text(it.component.name.value)

                        IconButton(
                            onClick = {
                                showRename = true
                            },
                            icon = icons().edit,
                            size = 32.dp,
                            tooltip = Strings.selector.component.rename.title()
                        )
                        IconButton(
                            onClick = {
                                LauncherFile.of(it.component.directory).open()
                            },
                            icon = icons().folder,
                            size = 32.dp,
                            tooltip = Strings.selector.component.openFolder()
                        )
                        IconButton(
                            onClick = {
                                showDelete = true
                            },
                            icon = icons().delete,
                            size = 32.dp,
                            interactionTint = MaterialTheme.colorScheme.error,
                            tooltip = Strings.selector.component.delete.tooltip()
                        )
                    }

                    sharedData.actionBarBoxContent(this)
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
                                    if(sharedData.detailsScrollable()) {
                                        mod.verticalScroll(rememberScrollState())
                                    } else {
                                        mod
                                    }
                                },
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            sharedData.detailsContent(this)
                        }

                        SelectorButton(
                            title = Strings.manager.component.settings(),
                            icon = icons().settings,
                            selected = it.settingsOpen.value,
                            onClick = { it.settingsOpen.value = true }
                        )
                    }
                }

                if(it.settingsOpen.value) {
                    ComponentSettings(it.component)
                } else {
                    val dropFun = remember(it, detailsOnDrop) {
                        detailsOnDrop?.let { onDrop ->
                            { files: DragData.FilesList ->
                                it.onDrop(files)
                            }
                        }
                    }

                    dropFun?.let { onDrop ->
                        FilesListDroppableArea(
                            onDrop = dropFun,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            columnContent()
                        }
                    } ?: columnContent()
                }
            }


            if (showRename) {
                RenamePopup(
                    manifest = it.component,
                    editValid = { name -> name.isNotBlank() && name != it.component.name.value },
                    onDone = { name ->
                        showRename = false
                        name?.let { newName ->
                            it.component.name.value = newName
                            try {
                                it.component.write()
                            } catch (e: IOException) {
                                AppContext.severeError(e)
                            }
                        }
                    }
                )
            }

            if (showDelete) {
                DeletePopup(
                    component = it.component,
                    checkHasComponent = { details -> checkHasComponent(details, it.component) },
                    onClose = { showDelete = false },
                    onConfirm = {
                        try {
                            it.component.delete(componentManifest)
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
                title = Strings.components.create(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(12.dp)
            ) {
                createContent {
                    reload()
                    creatorSelected = false
                }
            }
        }
    }
}

/*@OptIn(ExperimentalComposeUiApi::class)
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
)*/