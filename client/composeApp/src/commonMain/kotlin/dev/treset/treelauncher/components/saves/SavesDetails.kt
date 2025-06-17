package dev.treset.treelauncher.components.saves

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.treset.mcdl.saves.Save
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.launching.GameLauncher
import dev.treset.treelauncher.components.FileImport
import dev.treset.treelauncher.generic.Text
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.login.LoginContext
import dev.treset.treelauncher.style.icons
import dev.treset.treelauncher.util.launchGame
import java.io.IOException

@Composable
fun SharedSavesData.SavesDetails() {
    var loading by remember(component) { mutableStateOf(true) }

    val reloadSaves: () -> Unit = {
        Thread {
            with(component) { displayData.reload(AppContext.files.gameDataDir) }
            loading = false
        }.start()
    }

    val listDisplay = remember(component.listDisplay.value) { component.listDisplay.value ?: AppContext.files.savesManifest.defaultListDisplay.value }

    LaunchedEffect(component, AppContext.runningInstance) {
        reloadSaves()
    }

    if(showAdd.value) {
        FileImport(
            component,
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
            filesToAdd,
            allowFilePicker = false,
            allowDirectoryPicker = true,
        ) {
            showAdd.value = false
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
            displayData.saves.toMap().forEach {
                SaveButton(
                    it.key,
                    it.value,
                    selectedSave.value == it.key,
                    display = listDisplay,
                    onDelete = {
                        try {
                            it.value.remove()
                            if (selectedSave.value == it.key) {
                                selectedSave.value = null
                            }
                            reloadSaves()
                        } catch (e: IOException) {
                            AppContext.error(e)
                        }
                    },
                ) {
                    selectedServer.value = null
                    selectedSave.value = if (selectedSave.value == it.key) {
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
                    selectedServer.value == it,
                    display = listDisplay,
                ) {
                    selectedSave.value = null
                    selectedServer.value = if (selectedServer.value == it) {
                        null
                    } else {
                        it
                    }
                }
            }
        }
    }

    quickPlayData.value?.let {
        PlayPopup(
            component = component,
            quickPlayData = it,
            onClose = { quickPlayData.value = null }
        ) { quickPlay, instance ->
            val launcher = GameLauncher(
                instance,
                AppContext.files,
                LoginContext.isOffline(),
                LoginContext.userAuth.minecraftUser,
                quickPlay
            )

            quickPlayData.value = null

            launchGame(
                launcher
            ) { }
        }
    }
}