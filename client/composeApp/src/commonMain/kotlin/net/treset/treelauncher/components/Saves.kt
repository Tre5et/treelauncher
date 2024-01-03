package net.treset.treelauncher.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.treset.mc_version_loader.launcher.LauncherInstanceDetails
import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.mc_version_loader.saves.Save
import net.treset.mc_version_loader.saves.Server
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.data.InstanceData
import net.treset.treelauncher.backend.launching.GameLauncher
import net.treset.treelauncher.backend.util.QuickPlayData
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.generic.*
import net.treset.treelauncher.generic.IconButton
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.login.LoginContext
import net.treset.treelauncher.style.icons
import net.treset.treelauncher.util.launchGame
import java.io.IOException

@Composable
fun Saves(
    appContext: AppContext,
    loginContext: LoginContext
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

        var quickPlayData: QuickPlayData? by remember { mutableStateOf(null) }

        var showDelete by remember { mutableStateOf(false) }

        var popupData: PopupData? by remember { mutableStateOf(null) }

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
                                quickPlayData = QuickPlayData(
                                    QuickPlayData.Type.WORLD,
                                    it.fileName
                                )
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
                                quickPlayData = QuickPlayData(
                                    QuickPlayData.Type.SERVER,
                                    it.ip
                                )
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

        if(showDelete) {
            DeletePopup(
                component = component,
                appContext = appContext,
                checkHasComponent = { details -> details.savesComponent == component.id }   ,
                onClose = { showDelete = false },
                onConfirm = {
                    appContext.files.savesManifest.components.remove(component.id)
                    LauncherFile.of(
                        appContext.files.savesManifest.directory,
                        appContext.files.gameDetailsManifest.components[1]
                    ).write(appContext.files.savesManifest)
                    LauncherFile.of(component.directory).remove()
                    reload()
                }
            )
        }

        quickPlayData?.let {
            PlayPopup(
                component = component,
                quickPlayData = it,
                appContext = appContext,
                onClose = { quickPlayData = null },
                onConfirm = { playData, instance ->
                    val instanceData = InstanceData.of(instance, appContext.files)

                    val launcher = GameLauncher(
                        instanceData,
                        appContext.files,
                        loginContext.userAuth.minecraftUser!!,
                        playData
                    )

                    quickPlayData = null

                    launchGame(
                        launcher,
                        { pd -> popupData = pd },
                        {  }
                    )
                }
            )
        }

        popupData?.let {
            PopupOverlay(it)
        }
    }
}

@Composable
private fun PlayPopup(
    component: LauncherManifest,
    quickPlayData: QuickPlayData,
    appContext: AppContext,
    onClose: () -> Unit,
    onConfirm: (QuickPlayData, Pair<LauncherManifest, LauncherInstanceDetails>) -> Unit
) {
    val instances = remember(component) {
        appContext.files.instanceComponents
            .filter {
                it.second.savesComponent == component.id
            }
    }

    if (instances.isEmpty()) {
        PopupOverlay(
            type = PopupType.ERROR,
            titleRow = { Text(strings().selector.saves.play.noTitle()) },
            content = { Text(strings().selector.saves.play.noMessage()) },
            buttonRow = {
                Button(
                    onClick = onClose
                ) {
                    Text(strings().selector.saves.play.noClose())
                }
            }
        )
    } else if (instances.size > 1) {
        var selectedInstance by remember { mutableStateOf(instances[0]) }

        PopupOverlay(
            type = PopupType.NONE,
            titleRow = { Text(strings().selector.saves.play.multipleTitle()) },
            content = {
                Text(strings().selector.saves.play.multipleMessage())
                ComboBox(
                    items = instances,
                    defaultSelected = selectedInstance,
                    onSelected = { selectedInstance = it }
                )
            },
            buttonRow = {
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(strings().selector.saves.play.multipleClose())
                }
                Button(
                    onClick = { onConfirm(quickPlayData, selectedInstance) },
                ) {
                    Text(strings().selector.saves.play.multiplePlay())
                }
            }
        )
    } else {
        onConfirm(quickPlayData, instances[0])
    }
}