package net.treset.treelauncher.style

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class ColorMode(val isDark: @Composable () -> Boolean) {
    DARK({ true }),
    LIGHT({ false }),
    SYSTEM({ isSystemInDarkTheme() })
}

private fun dark() = darkColors(
    primary = Color.Green
)

private fun light() = lightColors(
    primary = Color.Green
)

private var color: ColorMode = ColorMode.SYSTEM
fun colorMode() = color
fun setColorMode(colorMode: ColorMode) { color = colorMode }

@Composable
fun colors() = if(colorMode().isDark()) {
        dark()
    } else {
        light()
    }