package net.treset.treelauncher.style

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import net.treset.treelauncher.backend.config.appSettings

var setDisplayScale: (Float) -> Unit = {}
var setFontScale: (Float) -> Unit = {}

@Composable
fun ScalingProvider(
    content: @Composable () -> Unit
) {
    var displayScale by remember { mutableStateOf(appSettings().displayScale) }
    var fontScale by remember { mutableStateOf(appSettings().fontScale) }

    setDisplayScale = {
        displayScale = it
    }

    setFontScale = {
        fontScale = it
    }

    CompositionLocalProvider(
        LocalDensity provides Density(
            density = LocalDensity.current.density * displayScale,
            fontScale = LocalDensity.current.fontScale * fontScale
        ),
    ) {
        content()
    }
}