package net.treset.treelauncher.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.treset.mc_version_loader.saves.Save
import net.treset.mc_version_loader.saves.Server
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.generic.IconButton
import net.treset.treelauncher.generic.RenamePopup
import net.treset.treelauncher.generic.TitledColumn
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.icons
import java.io.IOException

@Composable
fun Saves(
    appContext: AppContext
) {
    Components(
        strings().selector.saves.title(),
        appContext.files.savesComponents,
        {
            appContext.files.reloadSavesManifest()
            appContext.files.reloadSavesComponents()
        }
    ) {component, redraw, reload ->
        var showRename by remember { mutableStateOf(false) }

        var saves: List<Save> by remember { mutableStateOf(emptyList()) }
        var servers: List<Server> by remember { mutableStateOf(emptyList()) }

        var selectedSave: Save? by remember { mutableStateOf(null) }
        var selectedServer: Server? by remember { mutableStateOf(null) }

        LaunchedEffect(component) {
            selectedSave = null
            selectedServer = null

            saves = LauncherFile.of(component.directory).listFiles()
                .filter { it.isDirectory }
                .mapNotNull {
                    try {
                        Save.from(it)
                    } catch (e: IOException) {
                        null
                    }
                }
                .sortedBy { it.name }

            val serversFile = LauncherFile.of(component.directory, ".included_files", "servers.dat")
            servers = if(serversFile.exists()) {
                try {
                    Server.from(serversFile)
                } catch (e: IOException) {
                    emptyList()
                }
            } else {
                emptyList()
            }
        }

        TitledColumn(
            headerContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    selectedSave?.let {
                        IconButton(
                            onClick = {
                                //TODO: play
                            },
                            highlighted = true,
                            modifier = Modifier.offset(y = (-10).dp)
                        ) {
                            Icon(
                                icons().play,
                                "Play",
                                modifier = Modifier.size(46.dp)
                                    .offset(y = 12.dp)
                            )
                        }
                    }

                    selectedServer?.let {
                        IconButton(
                            onClick = {
                                //TODO: play
                            },
                            highlighted = true,
                            modifier = Modifier.offset(y = (-10).dp)
                        ) {
                            Icon(
                                icons().play,
                                "Play",
                                modifier = Modifier.size(46.dp)
                                    .offset(y = 12.dp)
                            )
                        }
                    }

                    Text(component.name)

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
                            LauncherFile.of(component.directory).open()
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
                            //TODO: delete
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
            if(saves.isNotEmpty()) {
                Text(
                    strings().selector.saves.worlds(),
                    style = MaterialTheme.typography.titleMedium
                )
                saves.forEach {
                    SaveButton(
                        it,
                        selectedSave == it
                    ) {
                        selectedServer = null
                        selectedSave = if(selectedSave == it) {
                            null
                        } else {
                            it
                        }
                    }
                }
            }
            if(servers.isNotEmpty()) {
                Text(
                    strings().selector.saves.servers(),
                    style = MaterialTheme.typography.titleMedium
                )
                servers.forEach {
                    ServerButton(
                        it,
                        selectedServer == it
                    ) {
                        selectedSave = null
                        selectedServer = if(selectedServer == it) {
                            null
                        } else {
                            it
                        }
                    }
                }
            }
        }

        if(showRename) {
            RenamePopup(
                manifest = component,
                editValid = { name -> name.isNotBlank() && name != component.name },
                onDone = {name ->
                    showRename = false
                    name?.let { newName ->
                        component.name = newName
                        LauncherFile.of(
                            component.directory,
                            appConfig().MANIFEST_FILE_NAME
                        ).write(component)
                        redraw()
                    }
                }
            )
        }
    }
}