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
import net.treset.mc_version_loader.resoucepacks.Resourcepack
import net.treset.mc_version_loader.resoucepacks.Texturepack
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.config.appSettings
import net.treset.treelauncher.backend.creation.ResourcepackCreator
import net.treset.treelauncher.backend.data.manifest.ComponentManifest
import net.treset.treelauncher.backend.util.exception.FileLoadException
import net.treset.treelauncher.backend.util.file.LauncherFile
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

    var selected: ComponentManifest? by remember { mutableStateOf(null) }

    var resourcepacks: List<Pair<Resourcepack, LauncherFile>> by remember { mutableStateOf(emptyList()) }
    var texturepacks: List<Pair<Texturepack, LauncherFile>> by remember { mutableStateOf(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    var showAdd by remember(selected) { mutableStateOf(false) }
    var filesToAdd by remember(selected) { mutableStateOf(emptyList<LauncherFile>()) }

    var listDisplay by remember { mutableStateOf(appSettings().resourcepacksDetailsListDisplay) }

    val reloadPacks = {
        selected?.let { current ->
            resourcepacks = LauncherFile.of(current.directory, "resourcepacks").listFiles()
                .mapNotNull { file ->
                    try {
                        Resourcepack.from(file)
                    } catch (e: Exception) {
                        Resourcepack(file.name, null, null)
                    }?.let { it to file }
                }
            texturepacks = LauncherFile.of(current.directory, "texturepacks").listFiles()
                .mapNotNull { file ->
                    try {
                        Texturepack.from(file)
                    } catch (e: Exception) {
                        Texturepack(file.name, null, null)
                    }?.let { it to file }
                }
        }
        loading = false
    }

    LaunchedEffect(showAdd) {
        if(!showAdd) {
            filesToAdd = emptyList()
        }
    }

    Components(
        strings().selector.resourcepacks.title(),
        components = components,
        componentManifest = AppContext.files.resourcepackManifest,
        checkHasComponent = { details, component -> details.resourcepacksComponent == component.id },
        getCreator = { state ->
            when(state.mode) {
                CreationMode.NEW -> state.name?.let {
                    ResourcepackCreator(
                        state.name,
                        AppContext.files.launcherDetails.typeConversion,
                        AppContext.files.resourcepackManifest
                    )
                }
                CreationMode.INHERIT -> state.name?.let{ state.existing?.let {
                    ResourcepackCreator(
                        state.name,
                        state.existing,
                        AppContext.files.resourcepackManifest
                    )
                }}
                CreationMode.USE -> null
            }
        },
        reload = {
            try {
                AppContext.files.reloadResourcepackManifest()
                AppContext.files.reloadResourcepackComponents()
                components = AppContext.files.resourcepackComponents.sortedBy { it.name }
            } catch (e: FileLoadException) {
                AppContext.severeError(e)
            }
        },
        detailsContent = { current, _, _ ->
            DisposableEffect(current) {
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
                    clearFilesToAdd = { filesToAdd = emptyList() }
                ) {
                    showAdd = false
                    reloadPacks()
                }
            } else {
                if(resourcepacks.isEmpty() && texturepacks.isEmpty() && !loading) {
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
                    if(resourcepacks.isNotEmpty()) {
                        Text(
                            strings().selector.resourcepacks.resourcepacks(),
                            style = MaterialTheme.typography.titleMedium
                        )
                        resourcepacks.forEach {
                            ResourcepackButton(
                                it.first,
                                display = listDisplay
                            ) {
                                try {
                                    it.second.remove()
                                    reloadPacks()
                                } catch (e: IOException) {
                                    AppContext.error(e)
                                }
                            }
                        }
                    }
                    if(texturepacks.isNotEmpty()) {
                        Text(
                            strings().selector.resourcepacks.texturepacks(),
                            style = MaterialTheme.typography.titleMedium
                        )
                        texturepacks.forEach {
                            TexturepackButton(
                                it.first,
                                display = listDisplay
                            ) {
                                try {
                                    it.second.remove()
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
        detailsScrollable = resourcepacks.isNotEmpty() || showAdd,
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
        Resourcepack.from(this)
    } catch (e: IOException) {
        try {
            Texturepack.from(this)
        } catch (e: IOException) {
            LOGGER.warn(e) { "Unable to parse imported resourcepack: ${this.name}" }
            null
        }
    }
}

private val LOGGER = KotlinLogging.logger {  }