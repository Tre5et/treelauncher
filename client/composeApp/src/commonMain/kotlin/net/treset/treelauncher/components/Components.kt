package net.treset.treelauncher.components

import androidx.compose.foundation.layout.*
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
import net.treset.treelauncher.creation.CreationMode
import net.treset.treelauncher.generic.*
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.icons

@Composable
fun Components(
    title: String,
    components: List<LauncherManifest>,
    appContext: AppContext,
    getCreator: (CreationMode, String?, LauncherManifest?) -> GenericComponentCreator?,
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
) {
    var selected: LauncherManifest? by remember(components) { mutableStateOf(null) }

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
        ) {
            components.forEach { component ->
                ComponentButton(
                    component = component,
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

                        Text(it.name)

                        IconButton(
                            onClick = {
                                showRename = true
                            }
                        ) {
                            Icon(
                                icons().rename,
                                "Rename",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                LauncherFile.of(it.directory).open()
                            }
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
                            interactionTint = MaterialTheme.colorScheme.error
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
                modifier = Modifier.padding(12.dp)
            ) {
                if(settingsDefault || showSettings) {
                    ComponentSettings(
                        it,
                        onClose = { showSettings = false },
                        showBack = !settingsDefault
                    )
                } else {
                    detailsContent(
                        it,
                        redrawSelected,
                        reload
                    )

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
                    manifest = it,
                    editValid = { name -> name.isNotBlank() && name != it.name },
                    onDone = { name ->
                        showRename = false
                        name?.let { newName ->
                            it.name = newName
                            LauncherFile.of(
                                it.directory,
                                appConfig().MANIFEST_FILE_NAME
                            ).write(it)
                            redrawSelected()
                        }
                    }
                )
            }

            if (showDelete) {
                DeletePopup(
                    component = it,
                    appContext = appContext,
                    checkHasComponent = { details -> details.savesComponent == it.id },
                    onClose = { showDelete = false },
                    onConfirm = {
                        appContext.files.savesManifest.components.remove(it.id)
                        LauncherFile.of(
                            appContext.files.savesManifest.directory,
                            appContext.files.gameDetailsManifest.components[1]
                        ).write(appContext.files.savesManifest)
                        LauncherFile.of(it.directory).remove()
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
                ComponentCreator(
                    existing = components.toList(),
                    toDisplayString = { name },
                    onCreate = { mode, name, existing ->
                        val creator = getCreator(mode, name, existing)

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
                    },
                    allowUse = false
                )
            }
        }

        creationStatus?.let {
            CreationPopup(it)
        }
    }
}