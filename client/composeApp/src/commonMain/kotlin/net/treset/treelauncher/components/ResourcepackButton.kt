package net.treset.treelauncher.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import net.treset.mc_version_loader.resoucepacks.Resourcepack
import net.treset.treelauncher.generic.ImageSelectorButton

@Composable
fun ResourcepackButton(
    resourcepack: Resourcepack
) {
    ImageSelectorButton(
        selected = false,
        onClick = {},
        image = resourcepack.image?.toComposeImageBitmap() ?: useResource("img/default_pack.png") { loadImageBitmap(it) },
        title = resourcepack.name,
        subtitle = resourcepack.packMcmeta.pack.description
    )
}