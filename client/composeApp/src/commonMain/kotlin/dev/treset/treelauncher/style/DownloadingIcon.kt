package dev.treset.treelauncher.style

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
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import treelauncher.composeapp.generated.resources.Res
import treelauncher.composeapp.generated.resources.downloading_inside
import treelauncher.composeapp.generated.resources.downloading_outside

@Composable
fun DownloadingIcon(
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    val inside = painterResource(Res.drawable.downloading_inside)
    val outside = painterResource(Res.drawable.downloading_outside)

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