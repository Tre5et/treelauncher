package net.treset.treelauncher.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toComposeImageBitmap
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
        image = save.image.toComposeImageBitmap(),
        title = save.name,
        subtitle = save.fileName
    )
}