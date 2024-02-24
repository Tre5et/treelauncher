package net.treset.treelauncher.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.darkrockstudios.libraries.mpfilepicker.DirectoryPicker
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.treelauncher.app
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.generic.*
import net.treset.treelauncher.localization.Strings
import net.treset.treelauncher.style.icons
import java.io.IOException

@Composable
fun <T> FileImport(
    component: LauncherManifest,
    components: Array<LauncherManifest>,
    toFile: LauncherFile.() -> T?,
    getDisplayName: T.() -> String,
    icon: ImageVector,
    stringPackage: Strings.Manager.Component.ImportStrings,
    allowFilePicker: Boolean = true,
    allowDirectoryPicker: Boolean = false,
    fileExtensions: List<String> = listOf(),
    close: () -> Unit
) {
    var tfFile by remember(component) { mutableStateOf("") }
    var fileError by remember(tfFile) { mutableStateOf(false) }
    var showFilePicker by remember(component) { mutableStateOf(false) }
    var showDirPicker by remember(component) { mutableStateOf(false) }

    var selectedFiles: List<Pair<T, LauncherFile>> by remember { mutableStateOf(emptyList()) }

    var popupContent: PopupData? by remember(component) { mutableStateOf(null) }

    var anyFilesExist by remember { mutableStateOf(false) }

    val filteredComponents = remember(component) {
        components
            .filter {
                it != component
            }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            if(anyFilesExist) {
                Text(
                    stringPackage.importComponent(),
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
                    var files: List<Pair<T, LauncherFile>> by remember(component) { mutableStateOf(emptyList()) }

                    LaunchedEffect(component) {
                        files = LauncherFile.of(component.directory)
                            .listFiles()
                            .mapNotNull { raw ->
                                raw.toFile()?.let { Pair(it, raw) }
                            }
                    }

                    if(files.isNotEmpty()) {
                        LaunchedEffect(Unit) {
                            anyFilesExist = true
                        }

                        var expanded by remember { mutableStateOf(false) }
                        val rotation by animateFloatAsState(if(expanded) 0f else -90f)

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
                                tooltip = stringPackage.tooltipExpand(expanded)
                            ) {
                                Icon(
                                    icons().expand,
                                    "Expand",
                                    Modifier.rotate(rotation)
                                )
                            }

                            Text(component.name)
                        }

                        AnimatedVisibility(
                            visible = expanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Row (
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
                                                icon,
                                                ""
                                            )

                                            Text(it.first.getDisplayName())

                                            IconButton(
                                                onClick = {
                                                    selectedFiles += it
                                                },
                                                tooltip = stringPackage.tooltipAdd(),
                                                enabled = !selectedFiles.contains(it)
                                            ) {
                                                Icon(
                                                    icons().add,
                                                    "Add World"
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
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                stringPackage.importFile(),
                style = MaterialTheme.typography.titleSmall
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextBox(
                    text = tfFile,
                    onTextChanged = { tfFile = it },
                    isError = fileError,
                    modifier = Modifier
                        .weight(1f, false)
                )

                if (allowFilePicker) {
                    IconButton(
                        onClick = { showFilePicker = true },
                        tooltip = stringPackage.tooltipFile()
                    ) {
                        Icon(
                            icons().selectFile,
                            "Open File Picker"
                        )
                    }
                }

                if (allowDirectoryPicker) {
                    IconButton(
                        onClick = { showDirPicker = true },
                        tooltip = stringPackage.tooltipFile()
                    ) {
                        Icon(
                            icons().folder,
                            "Open Directory Picker"
                        )
                    }
                }

                IconButton(
                    onClick = {
                        val file = LauncherFile.of(tfFile)
                        file.toFile()?.let {
                            selectedFiles += Pair(it, file)
                            tfFile = ""
                        } ?: run {
                            fileError = true
                        }
                    },
                    tooltip = stringPackage.tooltipAdd(),
                    enabled = tfFile.isNotEmpty()
                ) {
                    Icon(
                        icons().add,
                        "Add World"
                    )
                }

                DirectoryPicker(
                    show = showDirPicker,
                    onFileSelected = {
                        it?.let {
                            tfFile = it
                        }
                        showDirPicker = false
                    },
                )

                FilePicker(
                    show = showFilePicker,
                    onFileSelected = {
                        it?.let {
                            tfFile = it.path
                        }
                        showFilePicker = false
                    },
                    fileExtensions = fileExtensions
                )
            }
        }

        if(selectedFiles.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    stringPackage.selectedFiles(),
                    style = MaterialTheme.typography.titleSmall
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                ) {
                    selectedFiles.forEach {
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
                                icon,
                                ""
                            )

                            Text(it.first.getDisplayName())

                            IconButton(
                                onClick = {
                                    selectedFiles = selectedFiles.filter { world -> world != it }
                                },
                                interactionTint = MaterialTheme.colorScheme.error,
                                tooltip = stringPackage.delete()
                            ) {
                                Icon(
                                    icons().delete,
                                    "Remove File",
                                )
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                Thread {
                    popupContent = PopupData(
                        titleRow = { Text(stringPackage.importing()) }
                    )
                    LOGGER.debug { "Importing files into component: ${component.name}" }
                    selectedFiles.forEach { file ->
                        LOGGER.debug { "Importing file: ${file.second.path}" }

                        var newDir = LauncherFile.of(component.directory, file.second.name)
                        var found = false
                        for(i in 1..100) {
                            if(!newDir.exists()) {
                                found = true
                                break
                            }
                            LOGGER.debug { "File already exists: ${newDir.path}: finding unique name" }
                            val nameParts = newDir.name.split(".")
                            val newName = if(nameParts.size == 1) {
                                "${nameParts.first()}-"
                            } else {
                                "${nameParts.dropLast(1).joinToString(".")}-.${nameParts.last()}"
                            }
                            newDir = LauncherFile.of(component.directory, newName)
                        }
                        if(!found) {
                            app().error(IOException("Failed to find a unique name for file: ${file.second.path}"))
                            return@Thread
                        }

                        try {
                            LOGGER.debug { "Copying file: ${file.second.path} -> ${newDir.path}" }
                            file.second.copyTo(newDir)
                        } catch (e: IOException) {
                            app().error(e)
                        }
                    }
                    close()
                }.start()
            },
            enabled = selectedFiles.isNotEmpty()
        ) {
            Text(stringPackage.import())
        }
    }

    popupContent?.let {
        PopupOverlay(it)
    }
}

private val LOGGER = KotlinLogging.logger {  }