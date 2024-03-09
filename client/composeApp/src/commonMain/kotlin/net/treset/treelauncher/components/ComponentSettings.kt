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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.backend.util.string.PatternString
import net.treset.treelauncher.generic.ComboBox
import net.treset.treelauncher.generic.IconButton
import net.treset.treelauncher.generic.Text
import net.treset.treelauncher.generic.TextBox
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.icons

@Composable
fun ColumnScope.ComponentSettings(
    component: LauncherManifest
) {
    var includedFiles: List<String> by remember { mutableStateOf(emptyList()) }

    LaunchedEffect(component) {
        includedFiles = component.includedFiles
    }

    DisposableEffect(component) {
        onDispose {
            component.includedFiles = includedFiles
            LauncherFile.of(
                component.directory,
                appConfig().manifestFileName
            ).write(component)
        }
    }

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
                    .clickable {  }
                    .pointerHoverIcon(PointerIcon.Hand)
                    .padding(start = 8.dp)
                    .padding(4.dp)
            ) {
                Icon(
                    PatternString.decode(it).let {name ->
                        if(name.endsWith("/") || name.endsWith("\\")) icons().folder else icons().file
                    },
                    "File"
                )

                Text(PatternString.decode(it))

                IconButton(
                    onClick = {
                        includedFiles = includedFiles.filter { file -> file != it }
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
                includedFiles = includedFiles + PatternString("$newArg${if(fileType == FileType.FOLDER) "/" else ""}").get()
                newArg = ""
            },
            icon = icons().add,
            enabled = newArg.isNotBlank(),
            tooltip = strings().manager.component.addFile()
        )
    }
}

private enum class FileType(val title: String) {
    FILE(strings().manager.component.file()),
    FOLDER(strings().manager.component.folder())
}