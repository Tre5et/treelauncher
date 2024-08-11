package net.treset.treelauncher.components.mods

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.AppContextData
import net.treset.treelauncher.backend.data.LauncherMod
import net.treset.treelauncher.backend.data.LauncherModsDetails
import net.treset.treelauncher.backend.data.manifest.ComponentManifest
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.generic.Button
import net.treset.treelauncher.generic.IconButton
import net.treset.treelauncher.generic.PopupData
import net.treset.treelauncher.generic.Text
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.icons
import java.io.IOException

@Composable
fun ModsImport(
    component: Pair<ComponentManifest, LauncherModsDetails>,
    modContext: ModContext,
    appContext: AppContextData,
    droppedFile: LauncherFile? = null,
    close: () -> Unit
) {
    var selectedMods: List<Pair<LauncherMod, LauncherFile>> by remember(component) { mutableStateOf(emptyList()) }

    var anyFilesExist by remember(component) { mutableStateOf(false) }

    var popupContent: PopupData? by remember { mutableStateOf(null) }

    val filteredComponents = remember(component) {
        appContext.files.modsComponents
            .filter {
                it != component
            }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            if (anyFilesExist) {
                Text(
                    strings().manager.mods.import.importComponent(),
                    style = MaterialTheme.typography.titleSmall
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                filteredComponents.forEach { component ->
                    val files: List<LauncherMod> by remember(component) { mutableStateOf(component.second.mods) }

                    if (files.isNotEmpty()) {
                        LaunchedEffect(Unit) {
                            anyFilesExist = true
                        }

                        var expanded by remember { mutableStateOf(false) }
                        val rotation by animateFloatAsState(if (expanded) 0f else -90f)

                        Row(
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { expanded = !expanded }
                                .pointerHoverIcon(PointerIcon.Hand)
                                .padding(start = 8.dp)
                                .padding(4.dp)
                        ) {
                            IconButton(
                                onClick = { expanded = !expanded },
                                icon = icons().expand,
                                modifier = Modifier.rotate(rotation),
                                tooltip = strings().manager.mods.import.tooltipExpand(expanded)
                            )

                            Text(component.first.name)
                        }

                        AnimatedVisibility(
                            visible = expanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 28.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .offset(y = (-4).dp)
                                        .height(files.size * 44.dp)
                                        .width(1.dp)
                                        .background(LocalContentColor.current)
                                )
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    files.forEach {
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(4.dp))
                                                .clickable { }
                                                .pointerHoverIcon(PointerIcon.Hand)
                                                .padding(start = 8.dp)
                                                .padding(4.dp)
                                        ) {
                                            Icon(
                                                icons().mods,
                                                ""
                                            )

                                            Text(strings().manager.mods.import.displayName(it))

                                            IconButton(
                                                onClick = {
                                                    selectedMods += it to LauncherFile.of(component.first.directory, it.fileName)
                                                },
                                                icon = icons().add,
                                                tooltip = strings().manager.mods.import.tooltipAdd(),
                                                enabled = !selectedMods.map { it.first }.contains(it)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                strings().manager.mods.import.importFile(),
                style = MaterialTheme.typography.titleSmall
            )

            ModsEdit(
                modContext,
                onNewMod = { mod, file -> selectedMods += mod to file },
                droppedFile = droppedFile
            ) {}
        }

        if(selectedMods.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    strings().manager.mods.import.selectedFiles(),
                    style = MaterialTheme.typography.titleSmall
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                ) {
                    selectedMods.forEach {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { }
                                .pointerHoverIcon(PointerIcon.Hand)
                                .padding(start = 8.dp)
                                .padding(4.dp)
                        ) {
                            Icon(
                                icons().mods,
                                ""
                            )

                            Text(strings().manager.mods.import.displayName(it.first))

                            IconButton(
                                onClick = {
                                    selectedMods = selectedMods.filter { mod -> mod != it }
                                },
                                icon = icons().delete,
                                interactionTint = MaterialTheme.colorScheme.error,
                                tooltip = strings().manager.mods.import.delete()
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                popupContent = PopupData(
                    titleRow = { Text(strings().manager.mods.import.importing()) }
                )
                LOGGER.debug { "Importing mods into component: ${component.first.name}" }
                modContext.registerChangingJob { mods ->
                    selectedMods.forEach { orgMod ->
                        val mod = orgMod.first.clone()
                        val file = orgMod.second
                        LOGGER.debug { "Importing mod: ${file.path}" }

                        var newFile = LauncherFile.of(component.first.directory, file.name)
                        var found = false
                        for (i in 1..100) {
                            if (!newFile.exists()) {
                                found = true
                                break
                            }
                            LOGGER.debug { "Mod file already exists: ${newFile.path}: finding unique name..." }
                            val nameParts = newFile.name.split(".")
                            val newName = if (nameParts.size == 1) {
                                "${nameParts.first()}-"
                            } else {
                                "${nameParts.dropLast(1).joinToString(".")}-.${nameParts.last()}"
                            }
                            newFile = LauncherFile.of(component.first.directory, newName)
                            mod.fileName = newName
                        }
                        if (!found) {
                            AppContext.error(IOException("Failed to find a unique name for mod file: ${file.path}"))
                            return@registerChangingJob
                        }

                        try {
                            LOGGER.debug { "Copying mod file: ${file.path} -> ${newFile.path}" }
                            file.copyTo(newFile)
                        } catch (e: IOException) {
                            AppContext.error(e)
                        }

                        LOGGER.debug { "Adding mod to component: ${mod.name}" }
                        mods.add(mod)
                    }
                    close()
                }
            },
            enabled = selectedMods.isNotEmpty()
        ) {
            Text(strings().manager.mods.import.import())
        }
    }
}

fun LauncherMod.clone() = LauncherMod(currentProvider, description, isEnabled, url, iconUrl, name, fileName, version, downloads)

private val LOGGER = KotlinLogging.logger {}