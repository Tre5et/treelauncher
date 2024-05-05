package net.treset.treelauncher.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.DragData
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.treset.mc_version_loader.launcher.LauncherInstanceDetails
import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.mc_version_loader.saves.Save
import net.treset.mc_version_loader.saves.Server
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.config.appConfig
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
import net.treset.treelauncher.util.DetailsListDisplay
import net.treset.treelauncher.util.launchGame
import java.io.IOException
import java.net.URI

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Saves() {
    var components by remember { mutableStateOf(AppContext.files.savesComponents.sortedBy { it.name }) }

    var selected: LauncherManifest? by remember { mutableStateOf(null) }

    var saves: List<Pair<Save, LauncherFile>> by remember { mutableStateOf(emptyList()) }
    var servers: List<Server> by remember { mutableStateOf(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    var selectedSave: Save? by remember(selected) { mutableStateOf(null) }
    var selectedServer: Server? by remember(selected) { mutableStateOf(null) }

    var quickPlayData: QuickPlayData? by remember(selected) { mutableStateOf(null) }

    var showAdd by remember(selected) { mutableStateOf(false) }
    var filesToAdd by remember(selected) { mutableStateOf(emptyList<LauncherFile>()) }

    var listDisplay by remember { mutableStateOf(appSettings().savesDetailsListDisplay) }

    LaunchedEffect(showAdd) {
        if(!showAdd) {
            filesToAdd = emptyList()
        }
    }

    val reloadSaves = {
        selected?.let {
            saves = LauncherFile.of(it.directory).listFiles()
                .filter { it.isDirectory }
                .mapNotNull { file ->
                    try {
                        Save.from(file)
                    } catch (e: IOException) {
                        if(!file.name.startsWith(appConfig().includedFilesDirName)) {
                            Save(file.name, null, null)
                        } else {
                            null
                        }
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
            loading = false
        }
    }

    Components(
        strings().selector.saves.title(),
        components = components,
        componentManifest = AppContext.files.savesManifest,
        checkHasComponent = { details, component -> details.savesComponent == component.id },
        getCreator = { state ->
            when(state.mode) {
                CreationMode.NEW -> state.name?.let{
                    SavesCreator(
                        state.name,
                        AppContext.files.launcherDetails.typeConversion,
                        AppContext.files.savesManifest,
                        AppContext.files.gameDetailsManifest
                    )
                }
                CreationMode.INHERIT -> state.name?.let{ state.existing?.let {
                    SavesCreator(
                        state.name,
                        state.existing,
                        AppContext.files.savesManifest,
                        AppContext.files.gameDetailsManifest
                    )
                }}
                CreationMode.USE -> null
            }
        },
        reload = {
            try {
                AppContext.files.reloadSavesManifest()
                AppContext.files.reloadSavesComponents()
                components = AppContext.files.savesComponents.sortedBy { it.name }
            } catch (e: FileLoadException) {
                AppContext.severeError(e)
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
                    AppContext.files.savesComponents,
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
                    allowDirectoryPicker = true,
                    filesToAdd = filesToAdd,
                    clearFilesToAdd = { filesToAdd = emptyList() }
                ) {
                    showAdd = false
                    reloadSaves()
                }
            } else if(saves.isEmpty() && servers.isEmpty() && !loading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        strings().selector.saves.emptyTitle(),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        strings().selector.saves.empty().let {
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
                if (saves.isNotEmpty()) {
                    Text(
                        strings().selector.saves.worlds(),
                        style = MaterialTheme.typography.titleMedium
                    )
                    saves.forEach {
                        SaveButton(
                            it.first,
                            selectedSave == it.first,
                            display = listDisplay,
                            onDelete = {
                                try {
                                    it.second.remove()
                                    if (selectedSave == it.first) {
                                        selectedSave = null
                                    }
                                    reloadSaves()
                                } catch (e: IOException) {
                                    AppContext.error(e)
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
                    onClose = { quickPlayData = null }
                ) { playData, instance ->
                    val instanceData = try {
                        InstanceData.of(instance, AppContext.files)
                    } catch (e: FileLoadException) {
                        AppContext.severeError(e)
                        return@PlayPopup
                    }

                    val launcher = GameLauncher(
                        instanceData,
                        AppContext.files,
                        LoginContext.userAuth.minecraftUser!!,
                        playData
                    )

                    quickPlayData = null

                    launchGame(
                        launcher
                    ) { }
                }
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
                        tooltip = strings().selector.saves.play.button(),
                        enabled = AppContext.runningInstance == null
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
                        tooltip = strings().selector.saves.play.button(),
                        enabled = AppContext.runningInstance == null
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
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(end = 6.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    ListViewBox(
                        DetailsListDisplay.entries,
                        listDisplay,
                        {
                            listDisplay = it
                            appSettings().savesDetailsListDisplay = it
                        }
                    )
                }
            }
        },
        detailsOnDrop = {
            if(it is DragData.FilesList) {
                filesToAdd = it.readFiles().map { LauncherFile.of(URI(it).path) }
                showAdd = true
            }
        },
        detailsScrollable = saves.isNotEmpty() || servers.isNotEmpty() || showAdd,
        sortContext = SortContext(
            getSortType = { appSettings().savesComponentSortType },
            setSortType = { appSettings().savesComponentSortType = it },
            getReverse = { appSettings().isSavesComponentSortReverse },
            setReverse = { appSettings().isSavesComponentSortReverse = it }
        )
    )
}

@Composable
private fun PlayPopup(
    component: LauncherManifest,
    quickPlayData: QuickPlayData,
    onClose: () -> Unit,
    onConfirm: (QuickPlayData, Pair<LauncherManifest, LauncherInstanceDetails>) -> Unit
) {
    var instances: List<Pair<LauncherManifest, LauncherInstanceDetails>> by remember(component) { mutableStateOf(listOf()) }

    LaunchedEffect(component) {
        try {
            AppContext.files.reloadAll()
        } catch (e: FileLoadException) {
            AppContext.severeError(e)
        }

        instances = AppContext.files.instanceComponents
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