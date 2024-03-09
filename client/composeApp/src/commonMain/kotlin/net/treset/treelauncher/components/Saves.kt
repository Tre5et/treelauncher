package net.treset.treelauncher.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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

    var saves: List<Pair<Save, LauncherFile>> by remember { mutableStateOf(emptyList()) }
    var servers: List<Server> by remember { mutableStateOf(emptyList()) }

    var selectedSave: Save? by remember(selected) { mutableStateOf(null) }
    var selectedServer: Server? by remember(selected) { mutableStateOf(null) }

    var popupData: PopupData? by remember { mutableStateOf(null) }

    var quickPlayData: QuickPlayData? by remember(selected) { mutableStateOf(null) }

    var showAdd by remember(selected) { mutableStateOf(false) }

    val reloadSaves = {
        selected?.let {
            saves = LauncherFile.of(it.directory).listFiles()
                .filter { it.isDirectory }
                .mapNotNull { file ->
                    try {
                        Save.from(file)
                    } catch (e: IOException) {
                        null
                    }?.let { it to file }
                }
                .sortedBy { it.first.name }
            val serversFile = LauncherFile.of(it.directory, ".included_files", "servers.dat")
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
    }

    Components(
        strings().selector.saves.title(),
        components = components,
        componentManifest = appContext.files.savesManifest,
        checkHasComponent = { details, component -> details.savesComponent == component.id },
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
        detailsContent = { current, _, _ ->
            DisposableEffect(current) {
                selected = current
                reloadSaves()

                onDispose {
                    selected = null
                }
            }

            if(showAdd) {
                FileImport(
                    current,
                    appContext.files.savesComponents,
                    {
                        try {
                            Save.from(this)
                        } catch (e: IOException) {
                            null
                        }
                    },
                    {
                        this.name
                    },
                    icons().saves,
                    strings().manager.saves.import,
                    allowFilePicker = false,
                    allowDirectoryPicker = true
                ) {
                    showAdd = false
                    reloadSaves()
                }
            } else {
                if (saves.isNotEmpty()) {
                    Text(
                        strings().selector.saves.worlds(),
                        style = MaterialTheme.typography.titleMedium
                    )
                    saves.forEach {
                        SaveButton(
                            it.first,
                            selectedSave == it.first,
                            onDelete = {
                                try {
                                    it.second.remove()
                                    if(selectedSave == it.first) {
                                        selectedSave = null
                                    }
                                    reloadSaves()
                                } catch (e: IOException) {
                                    app().error(e)
                                }
                            },
                        ) {
                            selectedServer = null
                            selectedSave = if (selectedSave == it.first) {
                                null
                            } else {
                                it.first
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
        actionBarSpecial = { _, settingsShown, _, _ ->
            if(!settingsShown && !showAdd) {
                selectedSave?.let {
                    IconButton(
                        onClick = {
                            quickPlayData = QuickPlayData(
                                QuickPlayData.Type.WORLD,
                                it.fileName
                            )
                        },
                        painter = icons().play,
                        size = 32.dp,
                        highlighted = true,
                        tooltip = strings().selector.saves.play.button()
                    )
                }

                selectedServer?.let {
                    IconButton(
                        onClick = {
                            quickPlayData = QuickPlayData(
                                QuickPlayData.Type.SERVER,
                                it.ip
                            )
                        },
                        painter = icons().play,
                        size = 32.dp,
                        highlighted = true,
                        tooltip = strings().selector.saves.play.button()
                    )
                }

                IconButton(
                    onClick = {
                        showAdd = true
                    },
                    icon = icons().add,
                    size = 32.dp,
                    tooltip = strings().manager.saves.tooltipAdd()
                )
            }
        },
        actionBarBoxContent = {_, _, _, _ ->
            if(showAdd) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = {
                            showAdd = false
                        },
                        icon = icons().back,
                        size = 32.dp,
                        tooltip = strings().manager.component.import.back(),
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
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
                    selected = selectedInstance,
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