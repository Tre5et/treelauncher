package dev.treset.treelauncher.components.resourcepacks

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import dev.treset.mcdl.resourcepacks.Texturepack
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.generic.*
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.icons
import dev.treset.treelauncher.util.ListDisplay
import org.jetbrains.compose.resources.imageResource
import treelauncher.composeapp.generated.resources.Res
import treelauncher.composeapp.generated.resources.default_pack

@Composable
fun TexturepackButton(
    texturepack: Texturepack?,
    file: LauncherFile,
    display: ListDisplay,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember(texturepack) { mutableStateOf(false) }

    val image = texturepack?.image?.toComposeImageBitmap() ?: imageResource(Res.drawable.default_pack)

    when(display) {
        ListDisplay.FULL -> ImageSelectorButton(
            selected = false,
            onClick = {},
            image = image,
            title = texturepack?.name ?: file.name,
            subtitle = texturepack?.description
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
            title = texturepack?.name ?: file.name,
            subtitle = texturepack?.description
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
            title = texturepack?.name ?: file.name,
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