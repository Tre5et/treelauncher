package net.treset.treelauncher.generic

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import net.treset.treelauncher.style.disabledContent
import net.treset.treelauncher.style.hovered

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    highlighted: Boolean = false,
    tooltip: String? = null,
    interactionTint: Color = MaterialTheme.colorScheme.primary,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    val pressed by interactionSource.collectIsPressedAsState()
    val hovered by interactionSource.collectIsHoveredAsState()
    val nativeFocused by interactionSource.collectIsFocusedAsState()
    //Prevent focus on click
    val focused by remember(nativeFocused) { mutableStateOf(if(pressed) false else nativeFocused) }

    val foregroundColor by animateColorAsState(
        if(!enabled) {
            LocalContentColor.current.disabledContent()
        } else if(selected || focused || (highlighted && pressed)) {
            MaterialTheme.colorScheme.onPrimary
        } else if(hovered) {
            interactionTint.hovered()
        } else if (highlighted) {
            interactionTint
        } else {
            LocalContentColor.current
        }
    )

    val backgroundColor by animateColorAsState(
        if(!enabled) {
            Color.Transparent
        } else if(selected) {
            interactionTint
        } else if(focused) {
            interactionTint.hovered()
        } else {
            Color.Transparent
        }
    )

    var newModifier = modifier
        .clip(RoundedCornerShape(8.dp))
        .background(backgroundColor)
        .padding(4.dp)

    if (enabled) {
        newModifier = newModifier
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(interactionSource = interactionSource, indication = null, onClick = {
                onClick()
            })
    }

    CompositionLocalProvider(
        LocalContentColor provides foregroundColor
    ) {
        tooltip?.let {
            //TODO: update to compose 1.6 to work with hover
            PlainTooltipBox(
                tooltip = { Text(it) }
            ) {
                Box(
                    modifier = newModifier.tooltipAnchor(),
                    contentAlignment = Alignment.Center
                ) {
                    content()
                }
            }
        } ?: Box(
            modifier = newModifier,
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}