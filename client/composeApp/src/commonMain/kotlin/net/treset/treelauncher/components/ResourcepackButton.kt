package net.treset.treelauncher.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.treset.mc_version_loader.resoucepacks.Resourcepack
import net.treset.treelauncher.generic.*
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.icons
import net.treset.treelauncher.util.DetailsListDisplay

@Composable
fun ResourcepackButton(
    resourcepack: Resourcepack,
    display: DetailsListDisplay,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember(resourcepack) { mutableStateOf(false) }

    when(display) {
        DetailsListDisplay.FULL -> ImageSelectorButton(
            selected = false,
            onClick = {},
            image = resourcepack.image?.toComposeImageBitmap() ?: useResource("img/default_pack.png") { loadImageBitmap(it) },
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

        DetailsListDisplay.COMPACT -> SelectorButton(
            selected = false,
            onClick = { },
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Text(
                        resourcepack.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 18.dp, end = 36.dp)
                    )
                    resourcepack.packMcmeta?.pack?.description?.let {
                        Text(
                            it,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

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

        DetailsListDisplay.MINIMAL -> SelectorButton(
            selected = false,
            onClick = { },
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    resourcepack.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(start = 18.dp, end = 36.dp)
                        .align(Alignment.Center)
                )

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
            titleRow = { Text(strings().manager.resourcepacks.deleteTitle()) },
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