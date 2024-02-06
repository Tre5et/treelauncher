package net.treset.treelauncher.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.treset.mc_version_loader.launcher.LauncherInstanceDetails
import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.mc_version_loader.saves.Save
import net.treset.mc_version_loader.saves.Server
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.app
import net.treset.treelauncher.backend.config.appSettings
import net.treset.treelauncher.backend.creation.SavesCreator
import net.treset.treelauncher.backend.data.InstanceData
import net.treset.treelauncher.backend.launching.GameLauncher
import net.treset.treelauncher.backend.util.QuickPlayData
import net.treset.treelauncher.backend.util.exception.FileLoadException
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.creation.CreationMode
import net.treset.treelauncher.generic.*
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
    var components by remember { mutableStateOf(appContext.files.savesComponents.sortedBy { it.name }) }

    var selected: LauncherManifest? by remember { mutableStateOf(null) }

    var saves: List<Save> by remember { mutableStateOf(emptyList()) }
    var servers: List<Server> by remember { mutableStateOf(emptyList()) }

    var selectedSave: Save? by remember(selected) { mutableStateOf(null) }
    var selectedServer: Server? by remember(selected) { mutableStateOf(null) }

    var popupData: PopupData? by remember { mutableStateOf(null) }

    var quickPlayData: QuickPlayData? by remember(selected) { mutableStateOf(null) }

    Components(
        strings().selector.saves.title(),
        components,
        appContext = appContext,
        getCreator = { state ->
            when(state.mode) {
                CreationMode.NEW -> state.name?.let{
                    SavesCreator(
                        state.name,
                        appContext.files.launcherDetails.typeConversion,
                        appContext.files.savesManifest,
                        appContext.files.gameDetailsManifest
                    )
                }
                CreationMode.INHERIT -> state.name?.let{ state.existing?.let {
                    SavesCreator(
                        state.name,
                        state.existing,
                        appContext.files.savesManifest,
                        appContext.files.gameDetailsManifest
                    )
                }}
                CreationMode.USE -> null
            }
        },
        reload = {
            try {
                appContext.files.reloadSavesManifest()
                appContext.files.reloadSavesComponents()
                components = appContext.files.savesComponents.sortedBy { it.name }
            } catch (e: FileLoadException) {
                app().severeError(e)
            }
        },
        actionBarSpecial = { _, settingsShown, _, _ ->
            if(!settingsShown) {
                selectedSave?.let {
                    IconButton(
                        onClick = {
                            quickPlayData = QuickPlayData(
                                QuickPlayData.Type.WORLD,
                                it.fileName
                            )
                        },
                        highlighted = true,
                        tooltip = strings().selector.saves.play.button()
                    ) {
                        Icon(
                            icons().play,
                            "Play",
                            modifier = Modifier.size(32.dp)
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
                        tooltip = strings().selector.saves.play.button()
                    ) {
                        Icon(
                            icons().play,
                            "Play",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        },
        detailsContent = { current, _, _ ->
            LaunchedEffect(current) {
                selected = current

                saves = LauncherFile.of(current.directory).listFiles()
                    .filter { it.isDirectory }
                    .mapNotNull {
                        try {
                            Save.from(it)
                        } catch (e: IOException) {
                            null
                        }
                    }
                    .sortedBy { it.name }

                val serversFile = LauncherFile.of(current.directory, ".included_files", "servers.dat")
                servers = if (serversFile.exists()) {
                    try {
                        Server.from(serversFile)
                    } catch (e: IOException) {
                        emptyList()
                    }
                } else {
                    emptyList()
                }
            }

            DisposableEffect(current) {
                onDispose {
                    selected = null
                }
            }

            if (saves.isNotEmpty()) {
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
                        selectedSave = if (selectedSave == it) {
                            null
                        } else {
                            it
                        }
                    }
                }
            }
            if (servers.isNotEmpty()) {
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
                        selectedServer = if (selectedServer == it) {
                            null
                        } else {
                            it
                        }
                    }
                }
            }

            quickPlayData?.let {
                PlayPopup(
                    component = current,
                    quickPlayData = it,
                    appContext = appContext,
                    onClose = { quickPlayData = null },
                    onConfirm = { playData, instance ->
                        val instanceData = try {
                            InstanceData.of(instance, appContext.files)
                        } catch (e: FileLoadException) {
                            app().severeError(e)
                            return@PlayPopup
                        }

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
                            { }
                        )
                    }
                )
            }
        },
        detailsScrollable = true,
        sortContext = SortContext(
            getSortType = { appSettings().savesComponentSortType },
            setSortType = { appSettings().savesComponentSortType = it },
            getReverse = { appSettings().isSavesComponentSortReverse },
            setReverse = { appSettings().isSavesComponentSortReverse = it }
        )
    )

    popupData?.let {
        PopupOverlay(it)
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
    var instances: List<Pair<LauncherManifest, LauncherInstanceDetails>> by remember(component) { mutableStateOf(listOf()) }

    LaunchedEffect(component) {
        try {
            appContext.files.reloadAll()
        } catch (e: FileLoadException) {
            app().severeError(e)
        }

        instances = appContext.files.instanceComponents
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
                    onSelected = { selectedInstance = it },
                    toDisplayString = { first.name }
                )
            },
            buttonRow = {
                Button(
                    onClick = onClose,
                    color = MaterialTheme.colorScheme.error
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