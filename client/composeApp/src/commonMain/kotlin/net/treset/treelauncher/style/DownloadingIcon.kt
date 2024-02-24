package net.treset.treelauncher.style

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun DownloadingIcon(
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    val inside = painterResource("icons/downloading/inside.svg")
    val outside = painterResource("icons/downloading/outside.svg")

    val infiniteTransition = rememberInfiniteTransition(label = "infinite transition")
    val rotate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(
                800,
                easing = EaseInOutCubic
            ),
            RepeatMode.Restart
        ),
        label = "scale"
    )

    Box(
        modifier = modifier.size(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            inside,
            contentDescription,
            tint = tint
        )

        Icon(
            outside,
            contentDescription,
            modifier = modifier.rotate(rotate),
            tint = tint
        )
    }
}