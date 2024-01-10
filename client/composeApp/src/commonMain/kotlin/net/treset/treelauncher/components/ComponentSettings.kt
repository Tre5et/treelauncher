package net.treset.treelauncher.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import net.treset.treelauncher.generic.TextBox
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.icons

@Composable
fun ColumnScope.ComponentSettings(
    component: LauncherManifest,
    onClose: () -> Unit = {},
    showBack: Boolean = true
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
                appConfig().MANIFEST_FILE_NAME
            ).write(component)
        }
    }

    if(showBack) {
        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(
                icons().back,
                "Back",
                modifier = Modifier.size(32.dp)
            )
        }
    }

    Text(
        strings().manager.component.includedFiles(),
        style = MaterialTheme.typography.titleMedium
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(12.dp)
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
                    if(PatternString.decode(it).endsWith("/")) icons().folder else icons().file,
                    "File"
                )

                Text(PatternString.decode(it))

                IconButton(
                    onClick = {
                        includedFiles = includedFiles.filter { file -> file != it }
                    },
                    interactionTint = MaterialTheme.colorScheme.error
                ) {
                    Icon(
                        icons().delete,
                        "Delete File",
                    )
                }
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
            defaultSelected = fileType,
            onSelected = {
                fileType = it
            },
            toDisplayString = { title }
        )

        TextBox(
            text = newArg,
            onChange = {
                newArg = it
            },
            placeholder = strings().manager.component.fileName(),
        )

        IconButton(
            onClick = {
                includedFiles = includedFiles + PatternString("$newArg${if(fileType == FileType.FOLDER) "/" else ""}").get()
                newArg = ""
            },
            enabled = newArg.isNotBlank()
        ) {
            Icon(
                icons().add,
                "Add Argument",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

private enum class FileType(val title: String) {
    FILE(strings().manager.component.file()),
    FOLDER(strings().manager.component.folder())
}