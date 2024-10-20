package dev.treset.treelauncher.creation

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
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.creation.*
import dev.treset.treelauncher.backend.data.manifest.OptionsComponent
import dev.treset.treelauncher.backend.data.manifest.ResourcepackComponent
import dev.treset.treelauncher.backend.data.manifest.SavesComponent
import dev.treset.treelauncher.backend.util.Status
import dev.treset.treelauncher.components.get
import dev.treset.treelauncher.components.mods.ModsCreation
import dev.treset.treelauncher.components.mods.ModsCreationContent
import dev.treset.treelauncher.components.mods.get
import dev.treset.treelauncher.generic.*
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.navigation.NavigationContext
import dev.treset.treelauncher.navigation.NavigationState
import java.io.IOException

@Composable
fun Create() {
    var prevInstanceName = remember { "" }
    var instanceName by remember { mutableStateOf("") }

    var versionContent: VersionCreationContent? by remember { mutableStateOf(null) }
    var savesContent: CreationContent<SavesComponent>? by remember { mutableStateOf(null) }
    var resourcepackContent: CreationContent<ResourcepackComponent>? by remember { mutableStateOf(null) }
    var optionsContent: CreationContent<OptionsComponent>? by remember { mutableStateOf(null) }
    var modsContent: ModsCreationContent? by remember { mutableStateOf(null) }

    var hasMods by remember(versionContent?.versionType == VersionType.VANILLA || versionContent?.versionType == null) { mutableStateOf(
        !((versionContent?.versionType == VersionType.VANILLA  || versionContent?.versionType == null) && (modsContent?.newName.isNullOrBlank() || modsContent?.newName == instanceName) && modsContent?.inheritName.isNullOrBlank() && modsContent?.inheritComponent == null && modsContent?.useComponent == null && modsContent?.newVersions?.getOrNull(0) == versionContent?.minecraftVersion?.id)
    ) }

    var creationStatus: Status? by remember { mutableStateOf(null) }
    var showCreationDone: Boolean by remember { mutableStateOf(false) }
    var creationException: Exception? by remember { mutableStateOf(null) }

    TitledColumn(
        title = Strings.creator.instance.title(),
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
                    Strings.creator.instance.instance(),
                    style = MaterialTheme.typography.titleSmall
                )
                TextBox(
                    text = instanceName,
                    onTextChanged = {
                        prevInstanceName = instanceName
                        instanceName = it
                    },
                    placeholder = Strings.creator.name()
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
                    Strings.creator.instance.version(),
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
                var defaultSavesName by remember { mutableStateOf("") }
                if(savesContent?.newName?.isBlank() != false || savesContent?.newName == prevInstanceName)  {
                    defaultSavesName = instanceName
                }

                Text(
                    Strings.creator.instance.saves(),
                    style = MaterialTheme.typography.titleSmall
                )
                ComponentCreator(
                    components = AppContext.files.savesComponents,
                    showCreate = false,
                    getCreator = SavesCreator::get,
                    defaultNewName = defaultSavesName,
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
                var defaultResourcepackName by remember { mutableStateOf("") }
                if(resourcepackContent?.newName?.isBlank() != false || resourcepackContent?.newName == prevInstanceName)  {
                    defaultResourcepackName = instanceName
                }

                Text(
                    Strings.creator.instance.resourcepacks(),
                    style = MaterialTheme.typography.titleSmall
                )
                ComponentCreator(
                    components = AppContext.files.resourcepackComponents,
                    showCreate = false,
                    getCreator = ResourcepackCreator::get,
                    defaultNewName = defaultResourcepackName,
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
                var defaultOptionsName by remember { mutableStateOf("") }
                if(optionsContent?.newName?.isBlank() != false || optionsContent?.newName == prevInstanceName)  {
                    defaultOptionsName = instanceName
                }

                Text(
                    Strings.creator.instance.options(),
                    style = MaterialTheme.typography.titleSmall
                )
                ComponentCreator(
                    components = AppContext.files.optionsComponents,
                    showCreate = false,
                    getCreator = OptionsCreator::get,
                    defaultNewName = defaultOptionsName,
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
                    Strings.creator.instance.mods(),
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
                    var defaultModsName by remember { mutableStateOf("") }
                    if(modsContent?.newName?.isBlank() != false || modsContent?.newName == prevInstanceName)  {
                        defaultModsName = instanceName
                    }
                    ModsCreation(
                        components = AppContext.files.modsComponents,
                        showCreate = false,
                        setContent = { modsContent = it },
                        defaultNewName = defaultModsName,
                        defaultVersion = versionContent?.minecraftVersion?.id,
                        defaultType = versionContent?.versionType?.let { if (it == VersionType.VANILLA) null else it },
                    )
                }
            }
        }

        Button(
            onClick = {
                if (instanceName.isBlank() || versionContent?.isValid() != true || savesContent?.isValid() != true || resourcepackContent?.isValid() != true || optionsContent?.isValid() != true || (hasMods && modsContent?.isValid() != true)) return@Button
                Thread {
                    creationStatus = Status(CreationStep.STARTING)

                    val start = System.currentTimeMillis()
                    val versionCreator = try {
                        VersionCreator.get(versionContent!!, {})
                    } catch (e: IOException) {
                        creationStatus = null
                        AppContext.error(e)
                        return@Thread
                    }
                    System.out.println("Getting creators took: " + (System.currentTimeMillis() - start))

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

                    try {
                        instanceCreator.create()
                    } catch (e: Exception) {
                        creationException = e
                    }
                    showCreationDone = true
                    creationStatus = null
                }.start()
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
                Strings.creator.buttonCreate()
            )
        }
    }

    creationStatus?.let {
        StatusPopup(it)
    }

    if(showCreationDone) {
        creationException?.let {
            LaunchedEffect(Unit) {
                AppContext.error(it)
            }

            PopupOverlay(
                type = PopupType.ERROR,
                titleRow = { Text(Strings.creator.instance.popup.failure()) },
                content = { Text(it.toString()) },
                buttonRow = {
                    Button(
                        onClick = {
                            showCreationDone = false
                            creationException = null
                        }
                    ) {
                        Text(Strings.creator.instance.popup.back())
                    }
                }
            )
        } ?: PopupOverlay(
            type = PopupType.SUCCESS,
            titleRow = { Text(Strings.creator.instance.popup.success()) },
            buttonRow = {
                Button(
                    onClick = {
                        showCreationDone = false
                        NavigationContext.navigateTo(NavigationState.INSTANCES)
                    }
                ) {
                    Text(Strings.creator.instance.popup.backToInstances())
                }
            }
        )
    }
}