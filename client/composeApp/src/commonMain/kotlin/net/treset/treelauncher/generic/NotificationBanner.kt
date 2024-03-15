package net.treset.treelauncher.generic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import net.treset.treelauncher.style.contrast
import net.treset.treelauncher.style.inverted

@Composable
fun NotificationBanner(
    visible: Boolean,
    color: Color? = null,
    onDismissed: () -> Unit,
    content: @Composable RowScope.() -> Unit
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

    AnimatedVisibility(
        visible = visible,
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
                    content()
                }
            }
        }
    }
}

data class NotificationData(
    val color: Color? = null,
    val content: @Composable RowScope.() -> Unit
)