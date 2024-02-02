package net.treset.treelauncher.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import net.treset.mc_version_loader.saves.Save
import net.treset.treelauncher.generic.ImageSelectorButton

@Composable
fun SaveButton(
    save: Save,
    selected: Boolean,
    onClick: () -> Unit
) {
    ImageSelectorButton(
        selected = selected,
        onClick = onClick,
        image = save.image?.toComposeImageBitmap() ?: useResource("img/default_save.png") { loadImageBitmap(it) },
        title = save.name,
        subtitle = save.fileName
    )
}