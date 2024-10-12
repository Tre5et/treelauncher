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
import io.github.oshai.kotlinlogging.KotlinLogging
import dev.treset.mcdl.resourcepacks.Resourcepack
import dev.treset.mcdl.resourcepacks.Texturepack
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.config.appSettings
import net.treset.treelauncher.backend.creation.*
import net.treset.treelauncher.backend.data.manifest.ResourcepackComponent
import net.treset.treelauncher.backend.launching.resources.ResourcepacksDisplayData
import net.treset.treelauncher.backend.util.Status
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.creation.ComponentCreator
import net.treset.treelauncher.creation.CreationContent
import net.treset.treelauncher.creation.CreationMode
import net.treset.treelauncher.generic.IconButton
import net.treset.treelauncher.generic.ListDisplayBox
import net.treset.treelauncher.generic.Text
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.icons
import net.treset.treelauncher.util.DetailsListDisplay
import java.io.IOException
import java.net.URI

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Resourcepacks() {
    var components by remember { mutableStateOf(AppContext.files.resourcepackComponents.sortedBy { it.name }) }

    var selected: ResourcepackComponent? by remember { mutableStateOf(null) }

    var showAdd by remember(selected) { mutableStateOf(false) }
    var filesToAdd by remember(selected) { mutableStateOf(emptyList<LauncherFile>()) }

    var listDisplay by remember { mutableStateOf(appSettings().resourcepacksDetailsListDisplay) }

    var displayData: ResourcepacksDisplayData by remember { mutableStateOf(ResourcepacksDisplayData(mapOf(), mapOf(), {}, {})) }
    var loading by remember { mutableStateOf(true) }

    val reloadPacks = {
        Thread {
            displayData = selected?.getDisplayData(AppContext.files.gameDataDir) ?: ResourcepacksDisplayData(mapOf(), mapOf(), {}, {})
            loading = false
        }.start()
    }

    Components(
        strings().selector.resourcepacks.title(),
        components = components,
        componentManifest = AppContext.files.resourcepackManifest,
        checkHasComponent = { details, component -> details.resourcepacksComponent == component.id },
        createContent = { onDone ->
            ComponentCreator(
                existing = components,
                allowUse = false,
                getCreator = ResourcepackCreator::get,
                onDone = { onDone() }
            )
        },
        reload = {
            try {
                AppContext.files.reloadResourcepacks()
                components = AppContext.files.resourcepackComponents.sortedBy { it.name }
            } catch (e: IOException) {
                AppContext.severeError(e)
            }
        },
        detailsContent = { current, _, _ ->
            LaunchedEffect(showAdd) {
                if(!showAdd) {
                    filesToAdd = emptyList()
                }
            }

            DisposableEffect(current, AppContext.runningInstance) {
                selected = current
                reloadPacks()

                onDispose {
                    selected = null
                }
            }

            if(showAdd) {
                FileImport(
                    current,
                    AppContext.files.resourcepackComponents,
                    arrayOf("resourcepacks", "texturepacks"),
                    {
                        when(this) {
                            is Texturepack -> 1
                            else -> 0
                        }
                    },
                    { this.toPack() },
                    {
                        when(this) {
                            is Resourcepack -> this.name
                            is Texturepack -> this.name
                            else -> ""
                        }
                    },
                    icons().resourcePacks,
                    strings().manager.resourcepacks.import,
                    fileExtensions = listOf("zip"),
                    allowDirectoryPicker = true,
                    filesToAdd = filesToAdd,
                    clearFilesToAdd = { filesToAdd = emptyList() },
                    addFiles = {
                        val texturepacks = it.filter { it.first is Texturepack }
                        val resourcepacks = it.filter { it.first !is Texturepack }

                        displayData.addResourcepacks(resourcepacks.map { it.second })
                        displayData.addTexturepacks(texturepacks.map { it.second })
                    }
                ) {
                    showAdd = false
                    reloadPacks()
                }
            } else {
                if(displayData.resourcepacks.isEmpty() && displayData.texturepacks.isEmpty() && !loading) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            strings().selector.resourcepacks.emptyTitle(),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            strings().selector.resourcepacks.empty().let {
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
                    if(displayData.resourcepacks.isNotEmpty()) {
                        Text(
                            strings().selector.resourcepacks.resourcepacks(),
                            style = MaterialTheme.typography.titleMedium
                        )
                        displayData.resourcepacks.forEach {
                            ResourcepackButton(
                                it.key,
                                display = listDisplay
                            ) {
                                try {
                                    it.value.remove()
                                    reloadPacks()
                                } catch (e: IOException) {
                                    AppContext.error(e)
                                }
                            }
                        }
                    }
                    if(displayData.texturepacks.isNotEmpty()) {
                        Text(
                            strings().selector.resourcepacks.texturepacks(),
                            style = MaterialTheme.typography.titleMedium
                        )
                        displayData.texturepacks.forEach {
                            TexturepackButton(
                                it.key,
                                display = listDisplay
                            ) {
                                try {
                                    it.value.remove()
                                    reloadPacks()
                                } catch (e: IOException) {
                                    AppContext.error(e)
                                }
                            }
                        }
                    }
                }
            }
        },
        actionBarSpecial = { _, settingsShown, _, _ ->
            if(!settingsShown || !showAdd) {
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
        actionBarBoxContent = { _, _, _, _ ->
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
                    ListDisplayBox(
                        DetailsListDisplay.entries,
                        listDisplay,
                        {
                            listDisplay = it
                            appSettings().resourcepacksDetailsListDisplay = it
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
        detailsScrollable = displayData.resourcepacks.isNotEmpty() || showAdd,
        sortContext = SortContext(
            getSortType = { appSettings().resourcepacksComponentSortType },
            setSortType = { appSettings().resourcepacksComponentSortType = it },
            getReverse = { appSettings().isResourcepacksComponentSortReverse },
            setReverse = { appSettings().isResourcepacksComponentSortReverse = it }
        )
    )
}

fun LauncherFile.toPack(): Any? {
    return try {
        Resourcepack.get(this)
    } catch (e: IOException) {
        try {
            Texturepack.get(this)
        } catch (e: IOException) {
            LOGGER.warn(e) { "Unable to parse imported resourcepack: ${this.name}" }
            null
        }
    }
}

fun ResourcepackCreator.get(content: CreationContent<ResourcepackComponent>, onStatus: (Status) -> Unit): ComponentCreator<ResourcepackComponent, out CreationData> {
    return when(content.mode) {
        CreationMode.NEW -> new(NewCreationData(content.newName!!, AppContext.files.resourcepackManifest), onStatus)
        CreationMode.INHERIT -> inherit(InheritCreationData(content.inheritName!!, content.inheritComponent!!, AppContext.files.resourcepackManifest), onStatus)
        CreationMode.USE -> use(UseCreationData(content.useComponent!!, AppContext.files.resourcepackManifest), onStatus)
    }
}

private val LOGGER = KotlinLogging.logger {  }