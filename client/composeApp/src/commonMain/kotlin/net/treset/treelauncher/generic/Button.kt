package net.treset.treelauncher.generic

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import net.treset.treelauncher.style.contentColor
import net.treset.treelauncher.style.hovered

@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    color: Color = MaterialTheme.colorScheme.primary,
    dynamicContentColor: Boolean = true,
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val pressed by interactionSource.collectIsPressedAsState()
    val hovered by interactionSource.collectIsHoveredAsState()
    val nativeFocused by interactionSource.collectIsFocusedAsState()
    //Prevent focus on click
    val focused by remember(nativeFocused) { mutableStateOf(if(pressed) false else nativeFocused) }

    val backgroundColor by animateColorAsState(
        if(hovered || focused) {
            color.hovered()
        } else {
            color
        }
    )

    val contentColor = MaterialTheme.colorScheme.onPrimary

    val colorScheme = MaterialTheme.colorScheme
    val foregroundColor =
        if(dynamicContentColor) {
            //Use unhovered color always
            colorScheme.contentColor(color)
        } else {
            contentColor
        }

    androidx.compose.material3.Button(
        onClick,
        modifier.pointerHoverIcon(if(enabled) PointerIcon.Hand else PointerIcon.Default),
        enabled,
        shape,
        ButtonDefaults.buttonColors(
            contentColor = foregroundColor,
            containerColor = backgroundColor,
        ),
        elevation,
        border,
        contentPadding,
        interactionSource,
        content
    )
}