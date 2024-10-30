package dev.treset.treelauncher.components.resourcepacks

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.treset.mcdl.resourcepacks.Resourcepack
import dev.treset.mcdl.resourcepacks.Texturepack
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.components.FileImport
import dev.treset.treelauncher.generic.Text
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.icons
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.IOException

@Composable
fun SharedResourcepacksData.ResourcepacksDetails() {
    val listDisplay = remember(component.listDisplay.value) { component.listDisplay.value ?: AppContext.files.resourcepackManifest.defaultListDisplay.value }

    var loading by remember(component) { mutableStateOf(true) }

    val reloadPacks = {
        Thread {
            displayData = component.getDisplayData(AppContext.files.gameDataDir)
            loading = false
        }.start()
    }

    LaunchedEffect(showAdd) {
        if(!showAdd.value) {
            filesToAdd.clear()
        }
    }

    LaunchedEffect(component, AppContext.runningInstance) {
        reloadPacks()
    }

    if(showAdd.value) {
        FileImport(
            component,
            AppContext.files.resourcepackComponents.toTypedArray(),
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
            Strings.manager.resourcepacks.import,
            fileExtensions = listOf("zip"),
            allowDirectoryPicker = true,
            filesToAdd = filesToAdd,
            addFiles = {
                val texturepacks = it.filter { it.first is Texturepack }
                val resourcepacks = it.filter { it.first !is Texturepack }

                displayData.addResourcepacks(resourcepacks.map { it.second })
                displayData.addTexturepacks(texturepacks.map { it.second })
            }
        ) {
            showAdd.value = false
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
                    Strings.selector.resourcepacks.emptyTitle(),
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Strings.selector.resourcepacks.empty().let {
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
                    Strings.selector.resourcepacks.resourcepacks(),
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
                    Strings.selector.resourcepacks.texturepacks(),
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

private val LOGGER = KotlinLogging.logger {  }