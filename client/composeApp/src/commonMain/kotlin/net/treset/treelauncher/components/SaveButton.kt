package net.treset.treelauncher.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import net.treset.mc_version_loader.saves.Save
import net.treset.treelauncher.generic.*
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.icons
import net.treset.treelauncher.util.DetailsListDisplay

@Composable
fun SaveButton(
    save: Save,
    selected: Boolean,
    display: DetailsListDisplay,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var showDeleteDialog by remember(save) { mutableStateOf(false) }

    val image = save.image?.toComposeImageBitmap() ?: useResource("img/default_save.png") { loadImageBitmap(it) }

    when(display) {
        DetailsListDisplay.FULL -> ImageSelectorButton(
            selected = selected,
            onClick = onClick,
            image = image,
            title = save.name,
            subtitle = save.fileName
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
                    tooltip = strings().manager.saves.delete(),
                )
            }
        }

        DetailsListDisplay.COMPACT -> CompactSelectorButton(
            selected = selected,
            onClick = onClick,
            image = image,
            title = save.name,
            subtitle = save.fileName
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
                    tooltip = strings().manager.saves.delete()
                )
            }
        }

        DetailsListDisplay.MINIMAL -> CompactSelectorButton(
            selected = selected,
            onClick = onClick,
            title = save.name,
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
                    tooltip = strings().manager.saves.delete()
                )
            }
        }
    }

    if(showDeleteDialog) {
        PopupOverlay(
            type = PopupType.WARNING,
            titleRow = { Text(strings().manager.saves.deleteTitle(save)) },
            content = {
                Text(strings().manager.saves.deleteMessage(save))
            },
            buttonRow = {
                Button(
                    onClick = { showDeleteDialog = false },
                ) {
                    Text(strings().manager.saves.deleteCancel())
                }

                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    color = MaterialTheme.colorScheme.error
                ) {
                    Text(strings().manager.saves.deleteConfirm(save))
                }
            }
        )
    }
}