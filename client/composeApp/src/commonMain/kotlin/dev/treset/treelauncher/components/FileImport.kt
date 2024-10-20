package dev.treset.treelauncher.components

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
import kotlinx.coroutines.delay
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.data.manifest.Component
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.generic.*
import dev.treset.treelauncher.localization.StringsEn
import dev.treset.treelauncher.style.icons
import java.io.IOException

@Composable
fun <T> FileImport(
    component: Component,
    components: Array<out Component>,
    directoryName: String,
    toFile: LauncherFile.() -> T?,
    getDisplayName: T.() -> String,
    addFiles: (List<Pair<T?, LauncherFile>>) -> Unit,
    icon: ImageVector,
    stringPackage: StringsEn.Manager.Component.ImportStrings,
    allowFilePicker: Boolean = true,
    allowDirectoryPicker: Boolean = false,
    fileExtensions: List<String> = listOf(),
    filesToAdd: List<LauncherFile> = emptyList(),
    clearFilesToAdd: () -> Unit = {},
    close: () -> Unit
) {
    FileImport(
        component = component,
        components = components,
        directoryNames = arrayOf(directoryName),
        getDirectoryName = { 0 },
        toFile = toFile,
        getDisplayName = getDisplayName,
        icon = icon,
        stringPackage = stringPackage,
        allowFilePicker = allowFilePicker,
        allowDirectoryPicker = allowDirectoryPicker,
        fileExtensions = fileExtensions,
        filesToAdd = filesToAdd,
        clearFilesToAdd = clearFilesToAdd,
        addFiles = addFiles,
        close = close
    )
}

@Composable
fun <T> FileImport(
    component: Component,
    components: Array<out Component>,
    directoryNames: Array<String>,
    getDirectoryName: T?.() -> Int,
    toFile: LauncherFile.() -> T?,
    getDisplayName: T.() -> String,
    icon: ImageVector,
    stringPackage: StringsEn.Manager.Component.ImportStrings,
    allowFilePicker: Boolean = true,
    allowDirectoryPicker: Boolean = false,
    fileExtensions: List<String> = listOf(),
    filesToAdd: List<LauncherFile> = emptyList(),
    clearFilesToAdd: () -> Unit = {},
    addFiles: (List<Pair<T?, LauncherFile>>) -> Unit = {},
    close: () -> Unit
) {
    var tfFile by remember(component) { mutableStateOf("") }
    var showFilePicker by remember(component) { mutableStateOf(false) }
    var showDirPicker by remember(component) { mutableStateOf(false) }

    var selectedFiles: List<Pair<T?, LauncherFile>> by remember { mutableStateOf(emptyList()) }

    var popupContent: PopupData? by remember(component) { mutableStateOf(null) }

    var anyFilesExist by remember { mutableStateOf(false) }

    val filteredComponents = remember(component) {
        components
            .filter {
                it != component
            }
    }

    LaunchedEffect(filesToAdd) {
        var interactingWithPopup = false
        for (file in filesToAdd) {
            while(interactingWithPopup) {
                delay(100)
            }
            file.toFile()?.let {
                selectedFiles += Pair(it, file)
                tfFile = ""
            } ?: run {
                interactingWithPopup = true
                popupContent = PopupData(
                    type = PopupType.WARNING,
                    titleRow = { Text(stringPackage.unknownTitle(file)) },
                    content = {
                        Text(stringPackage.unknownMessage(file))
                    },
                    buttonRow = {
                        Button(
                            onClick = {
                                interactingWithPopup = false
                                popupContent = null
                            },
                            color = MaterialTheme.colorScheme.error
                        ) {
                            Text(stringPackage.unknownCancel())
                        }
                        Button(
                            onClick = {
                                selectedFiles += Pair(null, file)
                                tfFile = ""
                                interactingWithPopup = false
                                popupContent = null
                            }
                        ) {
                            Text(stringPackage.unknownConfirm())
                        }
                    }
                )
            }
        }
        clearFilesToAdd()
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
                        val mutableFiles: MutableList<Pair<T, LauncherFile>> = mutableListOf()
                        for(directoryName in directoryNames) {
                            LauncherFile.of(component.directory, directoryName)
                                .listFiles()
                                .mapNotNull { raw ->
                                    raw.toFile()?.let { Pair(it, raw) }
                                }
                                .forEach {
                                    mutableFiles += it
                                }
                        }
                        files = mutableFiles.toList()
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
                                icon = icons().expand,
                                modifier = Modifier.rotate(rotation),
                                tooltip = stringPackage.tooltipExpand(expanded)
                            )

                            Text(component.name.value)
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
                                                icon = icons().add,
                                                tooltip = stringPackage.tooltipAdd(),
                                                enabled = !selectedFiles.contains(it)
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
                    modifier = Modifier
                        .weight(1f, false)
                )

                if (allowFilePicker) {
                    IconButton(
                        onClick = { showFilePicker = true },
                        icon = if(fileExtensions.size == 1 && fileExtensions[0] == "zip") icons().zip else icons().selectFile,
                        tooltip = stringPackage.tooltipFile()
                    )
                }

                if (allowDirectoryPicker) {
                    IconButton(
                        onClick = { showDirPicker = true },
                        icon = icons().folder,
                        tooltip = stringPackage.tooltipFile()
                    )
                }

                IconButton(
                    onClick = {
                        val file = LauncherFile.of(tfFile)
                        file.toFile()?.let {
                            selectedFiles += Pair(it, file)
                            tfFile = ""
                        } ?: run {
                            popupContent = PopupData(
                                type = PopupType.WARNING,
                                titleRow = { Text(stringPackage.unknownTitle(file)) },
                                content = {
                                    Text(stringPackage.unknownMessage(file))
                                },
                                buttonRow = {
                                    Button(
                                        onClick = {
                                            popupContent = null
                                        },
                                        color = MaterialTheme.colorScheme.error
                                    ) {
                                        Text(stringPackage.unknownCancel())
                                    }
                                    Button(
                                        onClick = {
                                            selectedFiles += Pair(null, file)
                                            tfFile = ""
                                            popupContent = null
                                        }
                                    ) {
                                        Text(stringPackage.unknownConfirm())
                                    }
                                }
                            )
                        }
                    },
                    icon = icons().add,
                    tooltip = stringPackage.tooltipAdd(),
                    enabled = tfFile.isNotEmpty()
                )

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

                            Text(it.first?.getDisplayName() ?: it.second.name)

                            IconButton(
                                onClick = {
                                    selectedFiles = selectedFiles.filter { world -> world != it }
                                },
                                icon = icons().delete,
                                interactionTint = MaterialTheme.colorScheme.error,
                                tooltip = stringPackage.delete()
                            )
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
                    try {
                        addFiles(selectedFiles)
                    } catch(e: IOException) {
                        LOGGER.error(e) { "Failed to import files into component: ${component.name}" }
                        AppContext.error(e)
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