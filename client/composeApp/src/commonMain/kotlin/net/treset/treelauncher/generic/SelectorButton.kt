package net.treset.treelauncher.generic

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.treelauncher.style.disabledContainer
import net.treset.treelauncher.style.disabledContent

@Composable
fun SelectorButton(
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val backgroundColor by animateColorAsState(
        if(!enabled) MaterialTheme.colorScheme.secondaryContainer.disabledContainer()
        else if(selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.secondaryContainer
    )

    val contentColor by animateColorAsState(
        if(!enabled) MaterialTheme.colorScheme.onSecondaryContainer.disabledContent()
        else if (selected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSecondaryContainer
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .fillMaxWidth()
            .background(backgroundColor)
            .pointerHoverIcon(if(enabled) PointerIcon.Hand else PointerIcon.Default)
            .clickable(enabled = enabled) { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(
            LocalContentColor provides contentColor
        ) {
            content()
        }
    }
}

@Composable
fun SelectorButton(
    title: String,
    component: LauncherManifest? = null,
    icon: ImageVector,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    SelectorButton(
        selected = selected,
        onClick = onClick,
        enabled = enabled
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(48.dp)
            )

            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium
                )
                component?.let {
                    Text(it.name)
                }
            }
        }
    }
}

@Composable
fun ComponentButton(
    component: LauncherManifest,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    SelectorButton(
        selected = selected,
        onClick = onClick,
        enabled = enabled
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                component.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(component.id)
        }
    }
}

@Composable
fun ImageSelectorButton(
    selected: Boolean,
    onClick: () -> Unit,
    image: ImageBitmap?,
    title: String?,
    subtitle: String? = null,
    enabled: Boolean = true,
    overlayContent: @Composable BoxScope.() -> Unit = {}
) {
    SelectorButton(
        selected = selected,
        onClick = onClick,
        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(LocalContentColor.current)
                    .padding(4.dp)
                    .size(72.dp)
            ) {
                image?.let {
                    Image(
                        it,
                        "Icon",
                        modifier = Modifier.size(72.dp)
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.Center
            ) {
                title?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                subtitle?.let {
                    Text(it)
                }
            }
        }

        overlayContent()
    }
}