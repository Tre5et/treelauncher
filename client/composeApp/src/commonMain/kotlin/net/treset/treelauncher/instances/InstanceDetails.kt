package net.treset.treelauncher.instances

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.data.InstanceData
import net.treset.treelauncher.backend.launching.GameLauncher
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.generic.*
import net.treset.treelauncher.generic.IconButton
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.login.LoginContext
import net.treset.treelauncher.style.*
import net.treset.treelauncher.util.launchGame

@Composable
fun InstanceDetails(
    instance: InstanceData,
    redrawSelected: () -> Unit,
    reloadInstances: () -> Unit,
    appContext: AppContext,
    loginContext: LoginContext,
) {
    var selectedDetails: InstanceDetails? by remember { mutableStateOf(null) }

    var popupContent: PopupData? by remember { mutableStateOf(null) }
    var showRename by remember { mutableStateOf(false) }

    LaunchedEffect(instance) {
        selectedDetails = null
    }

    TitledColumn(
        modifier = Modifier.padding(12.dp),
        parentModifier = Modifier.fillMaxWidth(1/2f),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        headerContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            val launcher = GameLauncher(
                                instance,
                                appContext.files,
                                loginContext.userAuth.minecraftUser!!
                            )
                            launchGame(
                                launcher,
                                { popupContent = it },
                                { redrawSelected() }
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
                    Text(instance.instance.first.name)
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
                            LauncherFile.of(instance.instance.first.directory).open()
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
                            deleteDialog(
                                instance,
                                appContext,
                                { pc -> popupContent = pc },
                                { reloadInstances() }
                            )
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
        }
    ) {
        SelectorButton(
            title = strings().manager.instance.details.version(),
            component = instance.versionComponents[0].first,
            icon = icons().version,
            selected = selectedDetails == InstanceDetails.VERSION,
            onClick = {
                selectedDetails = if (selectedDetails == InstanceDetails.VERSION) null else InstanceDetails.VERSION
            }
        )
        SelectorButton(
            title = strings().manager.instance.details.saves(),
            component = instance.savesComponent,
            icon = icons().saves,
            selected = selectedDetails == InstanceDetails.SAVES,
            onClick = {
                selectedDetails = if (selectedDetails == InstanceDetails.SAVES) null else InstanceDetails.SAVES
            }
        )
        SelectorButton(
            title = strings().manager.instance.details.resourcepacks(),
            component = instance.resourcepacksComponent,
            icon = icons().resourcePacks,
            selected = selectedDetails == InstanceDetails.RESOURCE_PACKS,
            onClick = {
                selectedDetails =
                    if (selectedDetails == InstanceDetails.RESOURCE_PACKS) null else InstanceDetails.RESOURCE_PACKS
            }
        )
        SelectorButton(
            title = strings().manager.instance.details.options(),
            component = instance.optionsComponent,
            icon = icons().options,
            selected = selectedDetails == InstanceDetails.OPTIONS,
            onClick = {
                selectedDetails = if (selectedDetails == InstanceDetails.OPTIONS) null else InstanceDetails.OPTIONS
            }
        )
        instance.modsComponent?.let { modsComponent ->
            SelectorButton(
                title = strings().manager.instance.details.mods(),
                component = modsComponent.first,
                icon = icons().mods,
                selected = selectedDetails == InstanceDetails.MODS,
                onClick = {
                    selectedDetails = if (selectedDetails == InstanceDetails.MODS) null else InstanceDetails.MODS
                }
            )
        }
        SelectorButton(
            title = strings().manager.instance.details.settings(),
            icon = icons().settings,
            selected = selectedDetails == InstanceDetails.SETTINGS,
            onClick = {
                selectedDetails = if (selectedDetails == InstanceDetails.SETTINGS) null else InstanceDetails.SETTINGS
            }
        )
    }

    selectedDetails?.let {
        when (it) {
            InstanceDetails.SAVES, InstanceDetails.OPTIONS, InstanceDetails.RESOURCE_PACKS, InstanceDetails.MODS -> {
                InstanceComponentChanger(
                    instance = instance,
                    type = it,
                    appContext = appContext,
                    redrawSelected = redrawSelected
                )
            }
            InstanceDetails.VERSION -> {
                InstanceVersionChanger(
                    instance = instance,
                    appContext = appContext,
                    redrawCurrent = redrawSelected
                )
            }
            InstanceDetails.SETTINGS -> {
                InstanceSettings(instance)
            }
        }
    }

    if(showRename) {
        RenamePopup(
            manifest = instance.instance.first,
            editValid = { name -> name.isNotBlank() && name != instance.instance.first.name },
            onDone = {name ->
                showRename = false
                name?.let { newName ->
                    instance.instance.first.name = newName
                    LauncherFile.of(
                        instance.instance.first.directory,
                        appConfig().MANIFEST_FILE_NAME
                    ).write(instance.instance.first)
                    redrawSelected()
                }
            }
        )
    }

    popupContent?.let {
        PopupOverlay(it)
    }
}

private fun deleteDialog(
    instance: InstanceData,
    appContext: AppContext,
    setPopup: (PopupData?) -> Unit,
    onSuccess: () -> Unit
) {
    setPopup(
        PopupData(
            type = PopupType.WARNING,
            titleRow = { Text(strings().selector.instance.delete.title()) },
            content =  { Text(
                strings().selector.instance.delete.message(),
                textAlign = TextAlign.Center,
            ) },
            buttonRow = {
                Button(
                    onClick = { setPopup(null) },
                    content = { Text(strings().selector.instance.delete.cancel()) }
                )
                Button(
                    onClick = {
                        instance.delete(appContext.files)
                        onSuccess()
                        setPopup(null)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(strings().selector.instance.delete.confirm())
                }
            }
        )
    )
}

enum class InstanceDetails {
    VERSION,
    SAVES,
    RESOURCE_PACKS,
    OPTIONS,
    MODS,
    SETTINGS
}