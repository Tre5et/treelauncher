package dev.treset.treelauncher.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.DragData
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.treset.mcdl.saves.Save
import dev.treset.mcdl.saves.Server
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.config.AppSettings
import dev.treset.treelauncher.backend.creation.*
import dev.treset.treelauncher.backend.data.InstanceData
import dev.treset.treelauncher.backend.data.manifest.InstanceComponent
import dev.treset.treelauncher.backend.data.manifest.SavesComponent
import dev.treset.treelauncher.backend.launching.GameLauncher
import dev.treset.treelauncher.backend.launching.resources.SavesDisplayData
import dev.treset.treelauncher.backend.util.QuickPlayData
import dev.treset.treelauncher.backend.util.Status
import dev.treset.treelauncher.backend.util.exception.FileLoadException
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.creation.ComponentCreator
import dev.treset.treelauncher.creation.CreationContent
import dev.treset.treelauncher.creation.CreationMode
import dev.treset.treelauncher.generic.*
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.login.LoginContext
import dev.treset.treelauncher.style.icons
import dev.treset.treelauncher.util.DetailsListDisplay
import dev.treset.treelauncher.util.launchGame
import java.io.IOException
import java.net.URI

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Saves() {
    var selected: SavesComponent? by remember { mutableStateOf(null) }

    var displayData by remember { mutableStateOf(SavesDisplayData(mapOf(), listOf(), {})) }
    var loading by remember { mutableStateOf(true) }

    var selectedSave: Save? by remember(selected) { mutableStateOf(null) }
    var selectedServer: Server? by remember(selected) { mutableStateOf(null) }

    var quickPlayData: QuickPlayData? by remember(selected) { mutableStateOf(null) }

    var showAdd by remember(selected) { mutableStateOf(false) }
    var filesToAdd by remember(selected) { mutableStateOf(emptyList<LauncherFile>()) }

    LaunchedEffect(showAdd) {
        if(!showAdd) {
            filesToAdd = emptyList()
        }
    }

    val reloadSaves = {
        Thread {
            displayData = selected?.getDisplayData(AppContext.files.gameDataDir) ?: SavesDisplayData(mapOf(), listOf(), {})
            loading = false
        }.start()
    }

    Components(
        Strings.selector.saves.title(),
        components = AppContext.files.savesComponents,
        componentManifest = AppContext.files.savesManifest,
        checkHasComponent = { details, component -> details.savesComponent == component.id },
        createContent = { onDone ->
            ComponentCreator(
                components = AppContext.files.savesComponents,
                allowUse = false,
                getCreator = SavesCreator::get,
                onDone = { onDone() }
            )
        },
        reload = {
            try {
                AppContext.files.reloadSaves()
            } catch (e: IOException) {
                AppContext.severeError(e)
            }
        },
        detailsContent = { current,  _ ->
            DisposableEffect(current, AppContext.runningInstance) {
                selected = current
                reloadSaves()

                onDispose {
                    selected = null
                }
            }

            if(showAdd) {
                FileImport(
                    current,
                    AppContext.files.savesComponents.toTypedArray(),
                    "saves",
                    {
                        try {
                            Save.get(this)
                        } catch (e: IOException) {
                            null
                        }
                    },
                    {
                        this.name
                    },
                    {
                        displayData.addSaves(it.map { it.second })
                    },
                    icons().saves,
                    Strings.manager.saves.import,
                    allowFilePicker = false,
                    allowDirectoryPicker = true,
                    filesToAdd = filesToAdd,
                    clearFilesToAdd = { filesToAdd = emptyList() }
                ) {
                    showAdd = false
                    reloadSaves()
                }
            } else if(displayData.saves.isEmpty() && displayData.servers.isEmpty() && !loading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        Strings.selector.saves.emptyTitle(),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Strings.selector.saves.empty().let {
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
                if (displayData.saves.isNotEmpty()) {
                    Text(
                        Strings.selector.saves.worlds(),
                        style = MaterialTheme.typography.titleMedium
                    )
                    displayData.saves.forEach {
                        SaveButton(
                            it.key,
                            selectedSave == it.key,
                            display = AppSettings.savesDetailsListDisplay.value,
                            onDelete = {
                                try {
                                    it.value.remove()
                                    if (selectedSave == it.key) {
                                        selectedSave = null
                                    }
                                    reloadSaves()
                                } catch (e: IOException) {
                                    AppContext.error(e)
                                }
                            },
                        ) {
                            selectedServer = null
                            selectedSave = if (selectedSave == it.key) {
                                null
                            } else {
                                it.key
                            }
                        }
                    }
                }
                if (displayData.servers.isNotEmpty()) {
                    Text(
                        Strings.selector.saves.servers(),
                        style = MaterialTheme.typography.titleMedium
                    )
                    displayData.servers.forEach {
                        ServerButton(
                            it,
                            selectedServer == it,
                            display = AppSettings.savesDetailsListDisplay.value,
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
                        LoginContext.isOffline(),
                        LoginContext.userAuth.minecraftUser,
                        playData
                    )

                    quickPlayData = null

                    launchGame(
                        launcher
                    ) { }
                }
            }
        },
        actionBarSpecial = { _, settingsShown, _ ->
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
                        tooltip = Strings.selector.saves.play.button(),
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
                        tooltip = Strings.selector.saves.play.button(),
                        enabled = AppContext.runningInstance == null
                    )
                }

                IconButton(
                    onClick = {
                        showAdd = true
                    },
                    icon = icons().add,
                    size = 32.dp,
                    tooltip = Strings.manager.saves.tooltipAdd()
                )
            }
        },
        actionBarBoxContent = {_, _, _ ->
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
                        tooltip = Strings.manager.component.import.back(),
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(end = 6.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    ListDisplayBox(
                        DetailsListDisplay.entries,
                        AppSettings.savesDetailsListDisplay.value,
                        {
                            AppSettings.savesDetailsListDisplay.value = it
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
        detailsScrollable = displayData.saves.isNotEmpty() || displayData.servers.isNotEmpty() || showAdd
    )
}

@Composable
private fun PlayPopup(
    component: SavesComponent,
    quickPlayData: QuickPlayData,
    onClose: () -> Unit,
    onConfirm: (QuickPlayData, InstanceComponent) -> Unit
) {
    var instances: List<InstanceComponent> by remember(component) { mutableStateOf(listOf()) }

    LaunchedEffect(component) {
        try {
            AppContext.files.reload()
        } catch (e: IOException) {
            AppContext.severeError(e)
        }

        instances = AppContext.files.instanceComponents
            .filter {
                it.savesComponent == component.id
            }
    }

    if (instances.isEmpty()) {
        PopupOverlay(
            type = PopupType.ERROR,
            titleRow = { Text(Strings.selector.saves.play.noTitle()) },
            content = { Text(Strings.selector.saves.play.noMessage()) },
            buttonRow = {
                Button(
                    onClick = onClose
                ) {
                    Text(Strings.selector.saves.play.noClose())
                }
            }
        )
    } else if (instances.size > 1) {
        var selectedInstance by remember { mutableStateOf(instances[0]) }

        PopupOverlay(
            type = PopupType.NONE,
            titleRow = { Text(Strings.selector.saves.play.multipleTitle()) },
            content = {
                Text(Strings.selector.saves.play.multipleMessage())
                ComboBox(
                    items = instances,
                    selected = selectedInstance,
                    onSelected = { selectedInstance = it },
                    toDisplayString = { name.value }
                )
            },
            buttonRow = {
                Button(
                    onClick = onClose,
                    color = MaterialTheme.colorScheme.error
                ) {
                    Text(Strings.selector.saves.play.multipleClose())
                }
                Button(
                    onClick = { onConfirm(quickPlayData, selectedInstance) },
                ) {
                    Text(Strings.selector.saves.play.multiplePlay())
                }
            }
        )
    } else {
        onConfirm(quickPlayData, instances[0])
    }
}

fun SavesCreator.get(content: CreationContent<SavesComponent>, onStatus: (Status) -> Unit): ComponentCreator<SavesComponent, out CreationData> {
    return when(content.mode) {
        CreationMode.NEW -> new(NewCreationData(content.newName!!, AppContext.files.savesManifest), onStatus)
        CreationMode.INHERIT -> inherit(InheritCreationData(content.inheritName!!, content.inheritComponent!!, AppContext.files.savesManifest), onStatus)
        CreationMode.USE -> use(UseCreationData(content.useComponent!!, AppContext.files.savesManifest), onStatus)
    }
}