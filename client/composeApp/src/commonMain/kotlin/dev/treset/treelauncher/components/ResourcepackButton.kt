package dev.treset.treelauncher.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import dev.treset.mcdl.resourcepacks.Resourcepack
import dev.treset.treelauncher.generic.*
import dev.treset.treelauncher.localization.strings
import dev.treset.treelauncher.style.icons
import dev.treset.treelauncher.util.DetailsListDisplay

@Composable
fun ResourcepackButton(
    resourcepack: Resourcepack,
    display: DetailsListDisplay,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember(resourcepack) { mutableStateOf(false) }

    val image = resourcepack.image?.toComposeImageBitmap() ?: useResource("img/default_pack.png") { loadImageBitmap(it) }

    when(display) {
        DetailsListDisplay.FULL -> ImageSelectorButton(
            selected = false,
            onClick = {},
            image = image,
            title = resourcepack.name,
            subtitle = resourcepack.packMcmeta?.pack?.description
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
            selected = false,
            onClick = { },
            image = image,
            title = resourcepack.name,
            subtitle = resourcepack.packMcmeta?.pack?.description
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
            selected = false,
            onClick = { },
            title = resourcepack.name,
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
            titleRow = { Text(strings().manager.resourcepacks.deleteTexturepackTitle()) },
            content = {
                Text(strings().manager.resourcepacks.deleteMessage())
            },
            buttonRow = {
                Button(
                    onClick = { showDeleteDialog = false },
                ) {
                    Text(strings().manager.resourcepacks.deleteCancel())
                }

                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    color = MaterialTheme.colorScheme.error
                ) {
                    Text(strings().manager.resourcepacks.deleteConfirm())
                }
            }
        )
    }
}