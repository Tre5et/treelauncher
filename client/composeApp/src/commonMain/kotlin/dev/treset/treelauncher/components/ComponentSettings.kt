package dev.treset.treelauncher.components

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
import dev.treset.treelauncher.backend.data.manifest.Component
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.backend.util.string.PatternString
import dev.treset.treelauncher.generic.*
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.icons
import java.net.URI

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ColumnScope.ComponentSettings(
    component: Component
) {
    DisposableEffect(component) {
        onDispose {
            component.write()
        }
    }

    DroppableArea(
        onDrop = { data ->
            if(data is DragData.FilesList) {
                data.readFiles()
                    .map { LauncherFile.of(URI(it).path) }
                    .forEach {
                        component.includedFiles += PatternString("${it.name}${if (it.isDirectory) "/" else ""}").get()
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
                Strings.manager.component.includedFiles(),
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

                component.includedFiles.forEach {
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
                                component.includedFiles.remove(it)
                            },
                            icon = icons().delete,
                            interactionTint = MaterialTheme.colorScheme.error,
                            tooltip = Strings.manager.component.deleteFile()
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
                    placeholder = Strings.manager.component.fileName(),
                )

                IconButton(
                    onClick = {
                        component.includedFiles += PatternString("$newArg${if (fileType == FileType.FOLDER) "/" else ""}").get()
                        newArg = ""
                    },
                    icon = icons().add,
                    enabled = newArg.isNotBlank(),
                    tooltip = Strings.manager.component.addFile()
                )
            }
        }
    }
}

private enum class FileType(val title: String) {
    FILE(Strings.manager.component.file()),
    FOLDER(Strings.manager.component.folder())
}