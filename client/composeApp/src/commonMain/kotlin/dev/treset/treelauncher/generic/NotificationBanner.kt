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
    onDismissed: (NotificationData) -> Unit = {},
    data: NotificationData
) {
    val colorScheme = MaterialTheme.colorScheme
    val color = data.color ?: colorScheme.primary
    val contentColor = colorScheme.contentColor(color)

    var dismiss by remember(data) { mutableStateOf(false) }

    LaunchedEffect(data) {
        data.dismiss = {
            dismiss = true
        }
        data.visible = true
    }

    AnimatedVisibility(
        visible = data.visible && !dismiss,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        DisposableEffect(Unit) {
            onDispose {
                onDismissed(data)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color)
                .clickable(enabled = data.onClick != null) { data.onClick?.invoke(data) }
                .pointerHoverIcon(if(data.onClick != null) PointerIcon.Hand else PointerIcon.Default)
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
                    data.content(this, data)
                }
            }
        }
    }
}

data class NotificationData(
    val color: Color? = null,
    val onClick: ((NotificationData) -> Unit)? = null,
    val content: @Composable RowScope.(NotificationData) -> Unit
) {
    var visible by mutableStateOf(false)
    var dismiss: () -> Unit = {}
}