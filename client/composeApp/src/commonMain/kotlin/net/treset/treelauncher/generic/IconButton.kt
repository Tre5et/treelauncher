package net.treset.treelauncher.generic

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import net.treset.treelauncher.style.hovered
import net.treset.treelauncher.style.pressed

@Composable
fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
    enabled: Boolean = true,
    selected: Boolean = false,
    highlighted: Boolean = false,
    interactionTint: Color = MaterialTheme.colorScheme.primary,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    androidx.compose.material3.IconButton (
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
    ) {
        val color = if(highlighted) {
                if (selected) {
                    interactionTint
                } else if (interactionSource.collectIsHoveredAsState().value) {
                    interactionTint.pressed()
                } else {
                    interactionTint
                }
            } else {
                if (interactionSource.collectIsPressedAsState().value) {
                    interactionTint.pressed()
                } else if (selected) {
                    interactionTint
                } else if (interactionSource.collectIsHoveredAsState().value) {
                    interactionTint.hovered()
                } else {
                    LocalContentColor.current
                }
            }

        CompositionLocalProvider(
            LocalContentColor provides color
        ) {
            content()
        }
    }
}