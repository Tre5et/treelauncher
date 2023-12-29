package net.treset.treelauncher.generic

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import net.treset.treelauncher.style.hovered
import net.treset.treelauncher.style.inverted
import net.treset.treelauncher.style.pressed

@Composable
fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    highlighted: Boolean = false,
    interactionTint: Color = MaterialTheme.colorScheme.primary,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    val foregroundColor by animateColorAsState(
        if(highlighted) {
            if (selected) {
                LocalContentColor.current.inverted()
            } else if (interactionSource.collectIsHoveredAsState().value) {
                interactionTint.pressed()
            } else {
                interactionTint
            }
        } else {
            if (interactionSource.collectIsPressedAsState().value) {
                interactionTint.pressed()
            } else if (selected) {
                LocalContentColor.current.inverted()
            } else if (interactionSource.collectIsHoveredAsState().value) {
                interactionTint.hovered()
            } else {
                LocalContentColor.current
            }
        }
    )

    val backgroundColor by animateColorAsState(
        if(selected) interactionTint else Color.Transparent,
        label = "BackgroundColor"
    )

    var newModifier = modifier
        .clip(RoundedCornerShape(8.dp))
        .background(backgroundColor)
        .padding(4.dp)

    if(enabled) {
        newModifier = newModifier
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    }

    Box (
        modifier = newModifier,
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(
            LocalContentColor provides foregroundColor
        ) {
            content()
        }
    }
}