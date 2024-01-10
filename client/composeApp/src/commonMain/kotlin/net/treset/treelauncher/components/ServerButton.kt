package net.treset.treelauncher.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toComposeImageBitmap
import net.treset.mc_version_loader.saves.Server
import net.treset.treelauncher.generic.ImageSelectorButton

@Composable
fun ServerButton(
    server: Server,
    selected: Boolean,
    onClick: () -> Unit
) {
    ImageSelectorButton(
        selected = selected,
        onClick = onClick,
        image = server.image.toComposeImageBitmap(),
        title = server.name,
        subtitle = server.ip
    )
}