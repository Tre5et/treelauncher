package dev.treset.treelauncher.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import dev.treset.mcdl.resourcepacks.Texturepack
import dev.treset.treelauncher.generic.*
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.icons
import dev.treset.treelauncher.util.ListDisplay

@Composable
fun TexturepackButton(
    texturepack: Texturepack,
    display: ListDisplay,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember(texturepack) { mutableStateOf(false) }

    val image = texturepack.image?.toComposeImageBitmap() ?: useResource("img/default_pack.png") { loadImageBitmap(it) }

    when(display) {
        ListDisplay.FULL -> ImageSelectorButton(
            selected = false,
            onClick = {},
            image = image,
            title = texturepack.name,
            subtitle = texturepack.description
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
            selected = false,
            onClick = { },
            image = image,
            title = texturepack.name,
            subtitle = texturepack.description
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
            selected = false,
            onClick = { },
            title = texturepack.name,
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
            titleRow = { Text(Strings.manager.resourcepacks.deleteTitle()) },
            content = {
                Text(Strings.manager.resourcepacks.deleteMessage())
            },
            buttonRow = {
                Button(
                    onClick = { showDeleteDialog = false },
                ) {
                    Text(Strings.manager.resourcepacks.deleteCancel())
                }

                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    color = MaterialTheme.colorScheme.error
                ) {
                    Text(Strings.manager.resourcepacks.deleteConfirm())
                }
            }
        )
    }
}