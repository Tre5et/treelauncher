package dev.treset.treelauncher.generic

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
import dev.treset.treelauncher.style.contentColor

@Composable
fun NotificationBanner(
    visible: Boolean,
    color: Color = MaterialTheme.colorScheme.primary,
    onDismissed: () -> Unit,
    onClick: ((NotificationContext) -> Unit)? = null,
    content: @Composable RowScope.(NotificationContext) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val contentColor = colorScheme.contentColor(color)

    var dismiss by remember { mutableStateOf(false) }

    val context = remember(content) {
        NotificationContext {
            dismiss = true
        }
    }

    AnimatedVisibility(
        visible = visible && !dismiss,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        DisposableEffect(content) {
            onDispose {
                onDismissed()
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color)
                .clickable(enabled = onClick != null) { onClick?.invoke(context) }
                .pointerHoverIcon(if(onClick != null) PointerIcon.Hand else PointerIcon.Default)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProvideTextStyle(
                MaterialTheme.typography.titleSmall
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides contentColor
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
        color = data.color?: MaterialTheme.colorScheme.primary,
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