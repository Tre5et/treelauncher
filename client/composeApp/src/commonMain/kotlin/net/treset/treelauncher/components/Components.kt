package net.treset.treelauncher.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.creation.GenericComponentCreator
import net.treset.treelauncher.backend.util.CreationStatus
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.creation.ComponentCreator
import net.treset.treelauncher.creation.CreationPopup
import net.treset.treelauncher.creation.CreationState
import net.treset.treelauncher.generic.*
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.icons

@Composable
fun <T, C:CreationState<T>> Components(
    title: String,
    components: List<T>,
    manifest: T.() -> LauncherManifest,
    appContext: AppContext,
    getCreator: (C) -> GenericComponentCreator?,
    reload: () -> Unit,
    createContent: (@Composable ColumnScope.((C) -> Unit) -> Unit),
    actionBarSpecial: @Composable RowScope.(
        T,
        Boolean,
        () -> Unit,
        () -> Unit
    ) -> Unit = {_,_,_,_->},
    detailsContent: @Composable ColumnScope.(
        T,
        () -> Unit,
        () -> Unit
    ) -> Unit = {_,_,_->},
    detailsScrollable: Boolean = true,
    settingsDefault: Boolean = false
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


    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {

        TitledColumn(
            title = title,
            modifier = Modifier.padding(12.dp),
            parentModifier = Modifier.fillMaxWidth(1 / 2f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            scrollable = false
        ) {
            Box(
                modifier = Modifier
                    .weight(1f, false)
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(components) { component ->
                        ComponentButton(
                            component = component.manifest(),
                            selected = component == selected,
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
            var showSettings by remember { mutableStateOf(false) }

            var showRename by remember { mutableStateOf(false) }

            var showDelete by remember { mutableStateOf(false) }

            TitledColumn(
                headerContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        actionBarSpecial(
                            it,
                            settingsDefault || showSettings,
                            redrawSelected,
                            reload
                        )

                        Text(it.manifest().name)

                        IconButton(
                            onClick = {
                                showRename = true
                            },
                            tooltip = strings().selector.component.rename.title()
                        ) {
                            Icon(
                                icons().rename,
                                "Rename",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                LauncherFile.of(it.manifest().directory).open()
                            },
                            tooltip = strings().selector.component.openFolder()
                        ) {
                            Icon(
                                icons().folder,
                                "Open Folder",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                showDelete = true
                            },
                            interactionTint = MaterialTheme.colorScheme.error,
                            tooltip = strings().selector.component.delete.title()
                        ) {
                            Icon(
                                icons().delete,
                                "Delete",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                },
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(12.dp),
                scrollable = false
            ) {
                if(settingsDefault || showSettings) {
                    ComponentSettings(
                        it.manifest(),
                        onClose = { showSettings = false },
                        showBack = !settingsDefault
                    )
                } else {
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


            if (showRename) {
                RenamePopup(
                    manifest = it.manifest(),
                    editValid = { name -> name.isNotBlank() && name != it.manifest().name },
                    onDone = { name ->
                        showRename = false
                        name?.let { newName ->
                            it.manifest().name = newName
                            LauncherFile.of(
                                it.manifest().directory,
                                appConfig().MANIFEST_FILE_NAME
                            ).write(it.manifest())
                            redrawSelected()
                        }
                    }
                )
            }

            if (showDelete) {
                DeletePopup(
                    component = it.manifest(),
                    appContext = appContext,
                    checkHasComponent = { details -> details.savesComponent == it.manifest().id },
                    onClose = { showDelete = false },
                    onConfirm = {
                        appContext.files.savesManifest.components.remove(it.manifest().id)
                        LauncherFile.of(
                            appContext.files.savesManifest.directory,
                            appContext.files.gameDetailsManifest.components[1]
                        ).write(appContext.files.savesManifest)
                        LauncherFile.of(it.manifest().directory).remove()
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
                            creation.execute()
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

@Composable
fun Components(
    title: String,
    components: List<LauncherManifest>,
    appContext: AppContext,
    getCreator: (CreationState<LauncherManifest>) -> GenericComponentCreator?,
    reload: () -> Unit,
    actionBarSpecial: @Composable RowScope.(
        LauncherManifest,
        Boolean,
        () -> Unit,
        () -> Unit
    ) -> Unit = {_,_,_,_->},
    detailsContent: @Composable ColumnScope.(
        LauncherManifest,
        () -> Unit,
        () -> Unit
    ) -> Unit = {_,_,_->},
    settingsDefault: Boolean = false
) = Components(
    title,
    components,
    { this },
    appContext,
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
    detailsContent,
    settingsDefault
)