package dev.treset.treelauncher.style

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import dev.treset.treelauncher.backend.config.AppSettings
import dev.treset.treelauncher.localization.Strings

@Composable
fun LauncherTheme(
    colors: Colors,
    shapes: Shapes = MaterialTheme.shapes,
    typography: Typography = MaterialTheme.typography,
    content: @Composable () -> Unit
) {
    LaunchedEffect(AppSettings.language.value) {
        Strings = AppSettings.language.value.strings
    }

    CompositionLocalProvider(
        LocalDensity provides Density(
            density = LocalDensity.current.density * AppSettings.displayScale.value / 1000f,
            fontScale = LocalDensity.current.fontScale * AppSettings.fontScale.value / 1000f
        ),
    ) {
        MaterialTheme(
            colorScheme = colors.material,
            shapes = shapes,
            typography = typography
        ) {
            ProvideTextStyle(
                MaterialTheme.typography.bodyMedium
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                    )
                    content()
                }
            }
        }
    }
}