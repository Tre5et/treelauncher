package dev.treset.treelauncher.style

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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

    CompositionLocalProvider(LocalColors provides colors) {
        MaterialTheme(
            colorScheme = colors.material,
            shapes = shapes,
            typography = typography
        ) {
            ProvideTextStyle(
                MaterialTheme.typography.bodyMedium
            ) {
                content()
            }
        }
    }
}