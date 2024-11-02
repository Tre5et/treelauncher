package dev.treset.treelauncher.components.saves

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toComposeImageBitmap
import dev.treset.mcdl.saves.Server
import dev.treset.treelauncher.generic.CompactSelectorButton
import dev.treset.treelauncher.generic.ImageSelectorButton
import dev.treset.treelauncher.util.ListDisplay
import org.jetbrains.compose.resources.imageResource
import treelauncher.composeapp.generated.resources.Res
import treelauncher.composeapp.generated.resources.default_save

@Composable
fun ServerButton(
    server: Server,
    selected: Boolean,
    display: ListDisplay,
    onClick: () -> Unit
) {
    when(display) {
        ListDisplay.FULL -> ImageSelectorButton(
            selected = selected,
            onClick = onClick,
            image = server.image?.toComposeImageBitmap() ?: imageResource(Res.drawable.default_save),
            title = server.name,
            subtitle = server.ip
        )

        ListDisplay.COMPACT -> CompactSelectorButton(
            selected = selected,
            onClick = onClick,
            title = server.name,
            image = server.image?.toComposeImageBitmap() ?: imageResource(Res.drawable.default_save),
            subtitle = server.ip
        )

        ListDisplay.MINIMAL -> CompactSelectorButton(
            selected = selected,
            onClick = onClick,
            title = server.name
        )
    }

}