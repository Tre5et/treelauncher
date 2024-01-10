package net.treset.treelauncher.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toComposeImageBitmap
import net.treset.mc_version_loader.resoucepacks.Resourcepack
import net.treset.treelauncher.generic.ImageSelectorButton

@Composable
fun ResourcepackButton(
    resourcepack: Resourcepack
) {
    ImageSelectorButton(
        selected = false,
        onClick = {},
        image = resourcepack.image.toComposeImageBitmap(),
        title = resourcepack.name,
        subtitle = resourcepack.packMcmeta.pack.description
    )
}