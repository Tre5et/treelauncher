package net.treset.treelauncher.creation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.creation.*
import net.treset.treelauncher.backend.data.manifest.OptionsComponent
import net.treset.treelauncher.backend.data.manifest.ResourcepackComponent
import net.treset.treelauncher.backend.data.manifest.SavesComponent
import net.treset.treelauncher.backend.util.Status
import net.treset.treelauncher.components.get
import net.treset.treelauncher.components.mods.ModsCreation
import net.treset.treelauncher.components.mods.ModsCreationContent
import net.treset.treelauncher.components.mods.get
import net.treset.treelauncher.generic.*
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.navigation.NavigationContext
import net.treset.treelauncher.navigation.NavigationState
import java.io.IOException

@Composable
fun Create() {
    var instanceName by remember { mutableStateOf("") }

    var versionContent: VersionCreationContent? by remember { mutableStateOf(null) }
    var savesContent: CreationContent<SavesComponent>? by remember { mutableStateOf(null) }
    var resourcepackContent: CreationContent<ResourcepackComponent>? by remember { mutableStateOf(null) }
    var optionsContent: CreationContent<OptionsComponent>? by remember { mutableStateOf(null) }
    var modsContent: ModsCreationContent? by remember { mutableStateOf(null) }

    var hasMods by remember(versionContent?.versionType == VersionType.VANILLA || versionContent?.versionType == null) { mutableStateOf(
        !((versionContent?.versionType == VersionType.VANILLA  || versionContent?.versionType == null)
                && modsContent?.newName == null && modsContent?.inheritName == null && modsContent?.inheritComponent == null && modsContent?.useComponent == null && modsContent?.newVersions?.getOrNull(0) == versionContent?.minecraftVersion?.id
        )
    ) }

    var creationStatus: Status? by remember { mutableStateOf(null) }
    var showCreationDone: Boolean by remember { mutableStateOf(false) }
    var creationException: Exception? by remember { mutableStateOf(null) }

    TitledColumn(
        title = strings().creator.instance.title(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(intrinsicSize = IntrinsicSize.Max)
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(12.dp)
                    .fillMaxHeight()
            ) {
                Text(
                    strings().creator.instance.instance(),
                    style = MaterialTheme.typography.titleSmall
                )
                TextBox(
                    text = instanceName,
                    onTextChanged = { instanceName = it },
                    placeholder = strings().creator.name()
                )
            }

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(12.dp)
                    .fillMaxHeight()
            ) {
                Text(
                    strings().creator.instance.version(),
                    style = MaterialTheme.typography.titleSmall
                )
                VersionSelector(
                    showChange = false,
                    setContent = { versionContent = it },
                )
            }

        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .height(intrinsicSize = IntrinsicSize.Max)
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(12.dp)
                    .fillMaxHeight()
            ) {
                Text(
                    strings().creator.instance.saves(),
                    style = MaterialTheme.typography.titleSmall
                )
                ComponentCreator(
                    existing = AppContext.files.savesComponents.toList(),
                    showCreate = false,
                    getCreator = SavesCreator::get,
                    setContent = { savesContent = it },
                )
            }

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(12.dp)
                    .fillMaxHeight()
            ) {
                Text(
                    strings().creator.instance.resourcepacks(),
                    style = MaterialTheme.typography.titleSmall
                )
                ComponentCreator(
                    existing = AppContext.files.resourcepackComponents.toList(),
                    showCreate = false,
                    getCreator = ResourcepackCreator::get,
                    setContent = { resourcepackContent = it },
                )
            }

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(12.dp)
                    .fillMaxHeight()
            ) {
                Text(
                    strings().creator.instance.options(),
                    style = MaterialTheme.typography.titleSmall
                )
                ComponentCreator(
                    existing = AppContext.files.optionsComponents.toList(),
                    showCreate = false,
                    getCreator = OptionsCreator::get,
                    setContent = { optionsContent = it },
                )
            }

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(12.dp)
                    .fillMaxHeight()
            ) {
                TitledSwitch(
                    strings().creator.instance.mods(),
                    checked = hasMods,
                    onCheckedChange = {
                        hasMods = it
                    },
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.offset(y = (-1).dp),
                )

                AnimatedVisibility(
                    visible = hasMods,
                    enter = expandHorizontally(),
                    exit = shrinkHorizontally()
                ) {
                    ModsCreation(
                        existing = AppContext.files.modsComponents.toList(),
                        showCreate = false,
                        setContent = { modsContent = it },
                        defaultVersion = versionContent?.minecraftVersion,
                        defaultType = versionContent?.versionType?.let { if (it == VersionType.VANILLA) null else it },
                    )
                }
            }
        }

        Button(
            onClick = {
                try {
                    if (instanceName.isBlank() || versionContent?.isValid() != true || savesContent?.isValid() != true || resourcepackContent?.isValid() != true || optionsContent?.isValid() != true || (hasMods && modsContent?.isValid() != true)) return@Button

                    val versionCreator = VersionCreator.get(versionContent!!, {})
                    val savesCreator = SavesCreator.get(savesContent!!, {})
                    val resourcepackCreator = ResourcepackCreator.get(resourcepackContent!!, {})
                    val optionsCreator = OptionsCreator.get(optionsContent!!, {})
                    val modsCreator = if (hasMods) ModsCreator.get(modsContent!!, {}) else null

                    val instanceCreator = InstanceCreator(
                        InstanceCreationData(
                            instanceName,
                            versionCreator,
                            savesCreator,
                            resourcepackCreator,
                            optionsCreator,
                            modsCreator,
                            AppContext.files.instanceManifest
                        )
                    ) { status ->
                        creationStatus = status
                    }

                    Thread {
                        try {
                            instanceCreator.create()
                        } catch (e: Exception) {
                            creationException = e
                        }
                        showCreationDone = true
                        creationStatus = null
                    }.start()
                } catch (e: IOException) {
                    creationStatus = null
                    AppContext.error(e)
                }
            },
            enabled =
                instanceName.isNotBlank() &&
                versionContent?.isValid() ?: false &&
                savesContent?.isValid() ?: false &&
                resourcepackContent?.isValid() ?: false &&
                optionsContent?.isValid() ?: false &&
                (!hasMods || modsContent?.isValid() ?: false)
        ) {
            Text(
                strings().creator.buttonCreate()
            )
        }
    }

    creationStatus?.let {
        CreationPopup(it)
    }

    if(showCreationDone) {
        creationException?.let {
            LaunchedEffect(Unit) {
                AppContext.error(it)
            }

            PopupOverlay(
                type = PopupType.ERROR,
                titleRow = { Text(strings().creator.instance.popup.failure()) },
                content = { Text(it.toString()) },
                buttonRow = {
                    Button(
                        onClick = {
                            showCreationDone = false
                            creationException = null
                        }
                    ) {
                        Text(strings().creator.instance.popup.back())
                    }
                }
            )
        } ?: PopupOverlay(
            type = PopupType.SUCCESS,
            titleRow = { Text(strings().creator.instance.popup.success()) },
            buttonRow = {
                Button(
                    onClick = {
                        showCreationDone = false
                        NavigationContext.navigateTo(NavigationState.INSTANCES)
                    }
                ) {
                    Text(strings().creator.instance.popup.backToInstances())
                }
            }
        )
    }
}