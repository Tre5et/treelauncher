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
import dev.treset.treelauncher.backend.util.sort.SortProvider
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
    isEnabled: T.() -> Boolean = { true },
    reload: () -> Unit,
    constructSharedData: (T, () -> Unit) -> D,
    createContent: (@Composable ColumnScope.(onDone: (T?) -> Unit) -> Unit)? = null,
    actionBarSpecial: @Composable D.(RowScope) -> Unit = { },
    actionBarBoxContent: @Composable D.(BoxScope) -> Unit = { },
    actionBarFraction: Float = 1f,
    detailsContent: @Composable D.(ColumnScope) -> Unit = { },
    detailsOnDrop: (D.(DragData.FilesList) -> Unit)? = null,
    detailsScrollable: D.() -> Boolean = { true },
    selectorButton: @Composable (component: T, selected: Boolean, enabled: Boolean, onSelect: () -> Unit) -> Unit = { component, selected, enabled, onSelect ->
        ComponentButton(
            component = component,
            selected = selected,
            enabled = enabled,
            onClick = onSelect
        )
    },
    selectorFraction: Float = 1/2f,
    sorts: List<SortProvider<Component>> = ComponentSortProviders,
    allowSettings: Boolean = true,
    settingsDefault: Boolean = false,
    noComponentsContent: @Composable (ColumnScope.() -> Unit)? = null
) {
    @Suppress("NAME_SHADOWING")
    val components = components.toList()

    if(components.isEmpty()) {
        noComponentsContent?.let {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                noComponentsContent()
            }
            return
        }
    }

    var selected: T? by remember { mutableStateOf(null) }
    LaunchedEffect(components) {
        val current = components.find { it.id.value == selected?.id?.value }
        selected = current
    }

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
                    sorts = sorts,
                    sort = componentManifest.sort,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            },
            modifier = Modifier.padding(12.dp),
            parentModifier = Modifier.fillMaxWidth(selectorFraction),
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
                        selectorButton(
                            component,
                            component == selected,
                            component.isEnabled()
                        ) {
                            creatorSelected = false
                            selected = if (component == selected) {
                                null
                            } else {
                                component
                            }
                        }
                    }
                }
            }

            createContent?.let {
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

            if(sharedData.component.isEnabled()) {
                TitledColumn(
                    headerContent = {
                        if (it.settingsOpen.value && !settingsDefault) {
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .fillMaxWidth(actionBarFraction)
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
                                    .let { mod ->
                                        if (sharedData.detailsScrollable()) {
                                            mod.verticalScroll(rememberScrollState())
                                        } else {
                                            mod
                                        }
                                    },
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                sharedData.detailsContent(this)
                            }

                            if (allowSettings) {
                                SelectorButton(
                                    title = Strings.manager.component.settings(),
                                    icon = icons().settings,
                                    selected = it.settingsOpen.value,
                                    onClick = { it.settingsOpen.value = true }
                                )
                            }
                        }
                    }


                    if (allowSettings && it.settingsOpen.value) {
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
                            } catch (e: IOException) {
                                AppContext.severeError(e)
                            }
                            reload()
                            showDelete = false
                        }
                    )
                }
            }
        }

        createContent?.let {
            if (creatorSelected) {
                TitledColumn(
                    title = Strings.components.create(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(12.dp)
                ) {
                    createContent {
                        reload()
                        creatorSelected = false
                        it?.let {
                            selected = it
                        }
                    }
                }
            }
        }
    }
}