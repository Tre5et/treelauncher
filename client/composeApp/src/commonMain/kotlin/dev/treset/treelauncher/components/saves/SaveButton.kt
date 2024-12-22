package dev.treset.treelauncher.components.saves

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import dev.treset.mcdl.saves.Save
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.generic.*
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.icons
import dev.treset.treelauncher.util.ListDisplay
import org.jetbrains.compose.resources.imageResource
import treelauncher.composeapp.generated.resources.Res
import treelauncher.composeapp.generated.resources.default_save

@Composable
fun SaveButton(
    save: Save?,
    file: LauncherFile,
    selected: Boolean,
    display: ListDisplay,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var showDeleteDialog by remember(save) { mutableStateOf(false) }

    val image = save?.image?.toComposeImageBitmap() ?: imageResource(Res.drawable.default_save)

    when(display) {
        ListDisplay.FULL -> ImageSelectorButton(
            selected = selected,
            onClick = onClick,
            image = image,
            title = save?.name ?: file.name,
            subtitle = save?.fileName
        ) {
            Box(
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                IconButton(
                    onClick = {
                        showDeleteDialog = true
                    },
                    icon = icons().delete,
                    interactionTint = MaterialTheme.colorScheme.error,
                    tooltip = Strings.manager.saves.delete(),
                )
            }
        }

        ListDisplay.COMPACT -> CompactSelectorButton(
            selected = selected,
            onClick = onClick,
            image = image,
            title = save?.name ?: file.name,
            subtitle = save?.fileName
        ) {
            Box(
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                IconButton(
                    onClick = {
                        showDeleteDialog = true
                    },
                    icon = icons().delete,
                    interactionTint = MaterialTheme.colorScheme.error,
                    tooltip = Strings.manager.saves.delete()
                )
            }
        }

        ListDisplay.MINIMAL -> CompactSelectorButton(
            selected = selected,
            onClick = onClick,
            title = save?.name ?: file.name,
        ) {
            Box(
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                IconButton(
                    onClick = {
                        showDeleteDialog = true
                    },
                    icon = icons().delete,
                    interactionTint = MaterialTheme.colorScheme.error,
                    tooltip = Strings.manager.saves.delete()
                )
            }
        }
    }

    if(showDeleteDialog) {
        PopupOverlay(
            type = PopupType.WARNING,
            titleRow = { Text(Strings.manager.saves.deleteTitle(save)) },
            content = {
                Text(Strings.manager.saves.deleteMessage(save))
            },
            buttonRow = {
                Button(
                    onClick = { showDeleteDialog = false },
                ) {
                    Text(Strings.manager.saves.deleteCancel())
                }

                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    color = MaterialTheme.colorScheme.error
                ) {
                    Text(Strings.manager.saves.deleteConfirm(save))
                }
            }
        )
    }
}