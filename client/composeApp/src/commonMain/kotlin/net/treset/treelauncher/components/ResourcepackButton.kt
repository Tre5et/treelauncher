package net.treset.treelauncher.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.style.TextAlign
import net.treset.mc_version_loader.resoucepacks.Resourcepack
import net.treset.treelauncher.generic.*
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.icons

@Composable
fun ResourcepackButton(
    resourcepack: Resourcepack,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember(resourcepack) { mutableStateOf(false) }

    ImageSelectorButton(
        selected = false,
        onClick = {},
        image = resourcepack.image?.toComposeImageBitmap() ?: useResource("img/default_pack.png") { loadImageBitmap(it) },
        title = resourcepack.name,
        subtitle = resourcepack.packMcmeta.pack.description
    ) {
        Box(
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            IconButton(
                onClick = {
                    showDeleteDialog = true
                },
                interactionTint = MaterialTheme.colorScheme.error,
                tooltip = strings().manager.saves.delete(),
            ) {
                Icon(
                    icons().delete,
                    "Delete World"
                )
            }
        }
    }

    if(showDeleteDialog) {
        PopupOverlay(
            type = PopupType.WARNING,
            titleRow = { Text(strings().manager.resourcepacks.deleteTitle()) },
            content = {
                Text(
                    strings().manager.resourcepacks.deleteMessage(),
                    textAlign = TextAlign.Center
                )
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