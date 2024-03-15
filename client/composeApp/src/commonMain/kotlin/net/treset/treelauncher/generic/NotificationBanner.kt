package net.treset.treelauncher.generic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import net.treset.treelauncher.style.contrast
import net.treset.treelauncher.style.inverted

@Composable
fun NotificationBanner(
    visible: Boolean,
    color: Color? = null,
    onDismissed: () -> Unit,
    onClick: ((NotificationContext) -> Unit)? = null,
    content: @Composable RowScope.(NotificationContext) -> Unit
) {
    val localColor = LocalContentColor.current
    val contentColor = remember(color) {
        color?.let {
            if(color.contrast(localColor) < 4.5f && color.contrast(localColor.inverted()) > 4.5f) {
                localColor.inverted()
            } else {
                localColor
            }
        }
    }

    var dismiss by remember { mutableStateOf(false) }

    val context = NotificationContext {
        dismiss = true
    }

    AnimatedVisibility(
        visible = visible && !dismiss,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        DisposableEffect(Unit) {
            onDispose {
                onDismissed()
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color ?: MaterialTheme.colorScheme.primary)
                .clickable { onClick?.invoke(context) }
                .pointerHoverIcon(if(onClick != null) PointerIcon.Hand else PointerIcon.Default)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProvideTextStyle(
                MaterialTheme.typography.titleSmall
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides (contentColor ?: MaterialTheme.colorScheme.onPrimary)
                ) {
                    content(context)
                }
            }
        }
    }
}

@Composable
fun NotificationBanner(
    visible: Boolean,
    onDismissed: () -> Unit = {},
    data: NotificationData
) {
    NotificationBanner(
        visible = visible,
        color = data.color,
        onClick = data.onClick,
        onDismissed = onDismissed
    ) {
        data.content(this, it)
    }
}

data class NotificationContext(
    val dismiss: () -> Unit
)

data class NotificationData(
    val color: Color? = null,
    val onClick: ((NotificationContext) -> Unit)? = null,
    val content: @Composable RowScope.(NotificationContext) -> Unit
)