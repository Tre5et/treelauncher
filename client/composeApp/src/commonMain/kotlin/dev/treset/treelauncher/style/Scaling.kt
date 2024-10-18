package dev.treset.treelauncher.style

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import dev.treset.treelauncher.backend.config.AppSettings

@Composable
fun ScalingProvider(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalDensity provides Density(
            density = LocalDensity.current.density * AppSettings.displayScale.value / 1000f,
            fontScale = LocalDensity.current.fontScale * AppSettings.fontScale.value / 1000f
        ),
    ) {
        content()
    }
}