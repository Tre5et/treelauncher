package net.treset.treelauncher.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.DragData
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import net.treset.treelauncher.backend.data.manifest.Component
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.backend.util.string.PatternString
import net.treset.treelauncher.generic.*
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.icons
import java.net.URI

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ColumnScope.ComponentSettings(
    component: Component
) {
    var includedFiles: Array<String> by remember { mutableStateOf(emptyArray()) }

    LaunchedEffect(component) {
        includedFiles = component.includedFiles
    }

    DisposableEffect(component) {
        onDispose {
            component.includedFiles = includedFiles
            component.write()
        }
    }

    DroppableArea(
        onDrop = { data ->
            if(data is DragData.FilesList) {
                data.readFiles()
                    .map { LauncherFile.of(URI(it).path) }
                    .forEach {
                        includedFiles += PatternString("${it.name}${if (it.isDirectory) "/" else ""}").get()
                    }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                strings().manager.component.includedFiles(),
                style = MaterialTheme.typography.titleMedium
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(12.dp)
                    .weight(1f, false)
                    .verticalScroll(rememberScrollState())
            ) {

                includedFiles.forEach {
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
                            PatternString.decode(it).let { name ->
                                if (name.endsWith("/") || name.endsWith("\\")) icons().folder else icons().file
                            },
                            "File"
                        )

                        Text(PatternString.decode(it))

                        IconButton(
                            onClick = {
                                includedFiles = includedFiles.filter { file -> file != it }.toTypedArray()
                            },
                            icon = icons().delete,
                            interactionTint = MaterialTheme.colorScheme.error,
                            tooltip = strings().manager.component.deleteFile()
                        )
                    }

                }
            }

            var newArg by remember { mutableStateOf("") }
            var fileType by remember { mutableStateOf(FileType.FILE) }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ComboBox(
                    items = FileType.entries,
                    selected = fileType,
                    onSelected = { fileType = it },
                    toDisplayString = { title }
                )

                TextBox(
                    text = newArg,
                    onTextChanged = {
                        newArg = it
                    },
                    placeholder = strings().manager.component.fileName(),
                )

                IconButton(
                    onClick = {
                        includedFiles = includedFiles + PatternString("$newArg${if (fileType == FileType.FOLDER) "/" else ""}").get()
                        newArg = ""
                    },
                    icon = icons().add,
                    enabled = newArg.isNotBlank(),
                    tooltip = strings().manager.component.addFile()
                )
            }
        }
    }
}

private enum class FileType(val title: String) {
    FILE(strings().manager.component.file()),
    FOLDER(strings().manager.component.folder())
}