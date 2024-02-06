package net.treset.treelauncher.creation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.mc_version_loader.launcher.LauncherModsDetails
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.creation.*
import net.treset.treelauncher.backend.util.CreationStatus
import net.treset.treelauncher.generic.*
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.navigation.NavigationContext
import net.treset.treelauncher.navigation.NavigationState

@Composable
fun Create(
    appContext: AppContext,
    navContext: NavigationContext
) {
    val coroutineScope = rememberCoroutineScope()

    var instanceName by remember { mutableStateOf("") }

    var versionState: VersionState? by remember { mutableStateOf(null) }
    var savesState: CreationState<LauncherManifest>? by remember { mutableStateOf(null) }
    var resourcepackState: CreationState<LauncherManifest>? by remember { mutableStateOf(null) }
    var optionsState: CreationState<LauncherManifest>? by remember { mutableStateOf(null) }
    var modsState: CreationState<Pair<LauncherManifest, LauncherModsDetails>>? by remember { mutableStateOf(null) }

    var hasMods by remember(versionState?.versionType) { mutableStateOf(
        if(versionState?.versionType == VersionType.FABRIC) {
            true
        } else if(modsState?.name == null && modsState?.existing == null) {
            false
        } else true
    ) }

    var creationStatus: CreationStatus? by remember { mutableStateOf(null) }
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
                    onChange = { instanceName = it },
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
                    appContext = appContext,
                    showChange = false,
                    setCurrentState = { versionState = it }
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
                    existing = appContext.files.savesComponents.toList(),
                    showCreate = false,
                    setCurrentState = { savesState = it },
                    toDisplayString = { name },
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
                    existing = appContext.files.resourcepackComponents.toList(),
                    showCreate = false,
                    setCurrentState = { resourcepackState = it },
                    toDisplayString = { name },
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
                    existing = appContext.files.optionsComponents.toList(),
                    showCreate = false,
                    setCurrentState = { optionsState = it },
                    toDisplayString = { name },
                )
            }

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(12.dp)
                    .fillMaxHeight()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .requiredHeight(24.dp)
                ) {
                    Text(
                        strings().creator.instance.mods(),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Switch(
                        checked = hasMods,
                        onCheckedChange = {
                            hasMods = it
                        },
                        modifier = Modifier
                            .scale(0.7f)
                            .offset(y = (-1).dp)
                    )
                }
                if (hasMods) {
                    ComponentCreator(
                        existing = appContext.files.modsComponents.toList(),
                        showCreate = false,
                        setCurrentState = { modsState = it },
                        toDisplayString = { first.name },
                    )
                }
            }
        }

        Button(
            onClick = {
                versionState?.let { version -> if(version.isValid()) {
                    val versionCreator = getVersionCreator(version, appContext)
                savesState?.let { saves -> if(saves.isValid()) {
                    val savesCreator = when(saves.mode) {
                        CreationMode.NEW -> saves.name?.let {
                            SavesCreator(
                                saves.name,
                                appContext.files.launcherDetails.typeConversion,
                                appContext.files.savesManifest,
                                appContext.files.gameDetailsManifest
                            )
                        }

                        CreationMode.INHERIT -> saves.name?.let { saves.existing?.let {
                                SavesCreator(
                                    saves.name,
                                    saves.existing,
                                    appContext.files.savesManifest,
                                    appContext.files.gameDetailsManifest
                                )
                            }
                        }

                        CreationMode.USE -> saves.existing?.let {
                            SavesCreator(saves.existing)
                        }
                    }
                resourcepackState?.let { resourcepacks -> if(resourcepacks.isValid()) {
                    val resourcepackCreator = when(resourcepacks.mode) {
                        CreationMode.NEW -> resourcepacks.name?.let{
                            ResourcepackCreator(
                                resourcepacks.name,
                                appContext.files.launcherDetails.typeConversion,
                                appContext.files.resourcepackManifest
                            )
                        }
                        CreationMode.INHERIT -> resourcepacks.name?.let{ resourcepacks.existing?.let {
                            ResourcepackCreator(
                                resourcepacks.name,
                                resourcepacks.existing,
                                appContext.files.resourcepackManifest
                            )
                        }}
                        CreationMode.USE -> resourcepacks.existing?.let {
                            ResourcepackCreator(resourcepacks.existing)
                        }
                    }
                optionsState?.let { options -> if(options.isValid()) {
                    val optionsCreator = when(options.mode) {
                        CreationMode.NEW -> options.name?.let {
                            OptionsCreator(
                                options.name,
                                appContext.files.launcherDetails.typeConversion,
                                appContext.files.optionsManifest
                            )
                        }
                        CreationMode.INHERIT -> options.name?.let{ options.existing?.let {
                            OptionsCreator(
                                options.name,
                                options.existing,
                                appContext.files.optionsManifest
                            )
                        }}
                        CreationMode.USE -> options.existing?.let {
                            OptionsCreator(options.existing)
                        }
                    }
                val modsCreator: ModsCreator? = if(hasMods) {
                    modsState?.let { mods -> if (mods.isValid()) {
                            when (mods.mode) {
                                CreationMode.NEW -> mods.name?.let {
                                    ModsCreator(
                                        mods.name,
                                        appContext.files.launcherDetails.typeConversion,
                                        appContext.files.modsManifest,
                                        "fabric",
                                        version.minecraftVersion!!.id,
                                        appContext.files.gameDetailsManifest
                                    )
                                }

                                CreationMode.INHERIT -> mods.name?.let {
                                    mods.existing?.let {
                                        ModsCreator(
                                            mods.name,
                                            mods.existing,
                                            appContext.files.modsManifest,
                                            appContext.files.gameDetailsManifest
                                        )
                                    }
                                }

                                CreationMode.USE -> mods.existing?.let {
                                    ModsCreator(
                                        mods.existing
                                    )
                                }
                            }
                        } else null
                    }
                } else null

                val instanceCreator = InstanceCreator(
                    instanceName,
                    appContext.files.launcherDetails.typeConversion,
                    appContext.files.instanceManifest,
                    listOf(),
                    listOf(),
                    listOf(),
                    modsCreator,
                    optionsCreator?: return@Button,
                    resourcepackCreator?: return@Button,
                    savesCreator?: return@Button,
                    versionCreator?: return@Button
                )
                instanceCreator.statusCallback = { creationStatus = it }

                coroutineScope.launch {
                    try {
                        instanceCreator.execute()
                    } catch(e: Exception) {
                        creationException = e
                    }
                    showCreationDone = true
                    creationStatus = null
                }
            }}}}}}}}},
            enabled =
                instanceName.isNotBlank() &&
                versionState?.isValid() == true &&
                savesState?.isValid() == true &&
                resourcepackState?.isValid() == true &&
                optionsState?.isValid() == true &&
                (!hasMods || modsState ?.isValid() == true)
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
                        navContext.navigateTo(NavigationState.INSTANCES)
                    }
                ) {
                    Text(strings().creator.instance.popup.backToInstances())
                }
            }
        )
    }
}