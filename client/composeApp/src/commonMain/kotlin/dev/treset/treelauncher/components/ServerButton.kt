package dev.treset.treelauncher.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import dev.treset.mcdl.saves.Server
import dev.treset.treelauncher.generic.CompactSelectorButton
import dev.treset.treelauncher.generic.ImageSelectorButton
import dev.treset.treelauncher.util.DetailsListDisplay

@Composable
fun ServerButton(
    server: Server,
    selected: Boolean,
    display: DetailsListDisplay,
    onClick: () -> Unit
) {
    when(display) {
        DetailsListDisplay.FULL -> ImageSelectorButton(
            selected = selected,
            onClick = onClick,
            image = server.image?.toComposeImageBitmap() ?: useResource("img/default_save.png") { loadImageBitmap(it) },
            title = server.name,
            subtitle = server.ip
        )

        DetailsListDisplay.COMPACT -> CompactSelectorButton(
            selected = selected,
            onClick = onClick,
            title = server.name,
            image = server.image?.toComposeImageBitmap() ?: useResource("img/default_save.png") { loadImageBitmap(it) },
            subtitle = server.ip
        )

        DetailsListDisplay.MINIMAL -> CompactSelectorButton(
            selected = selected,
            onClick = onClick,
            title = server.name
        )
    }

}