package net.treset.treelauncher.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
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
        image = server.image?.toComposeImageBitmap() ?: useResource("img/default_save.png") { loadImageBitmap(it) },
        title = server.name,
        subtitle = server.ip
    )
}