package net.treset.treelauncher.style

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import net.treset.treelauncher.backend.config.appSettings
import net.treset.treelauncher.localization.strings

enum class Theme(val isDark: @Composable () -> Boolean, val displayName: () -> String) {
    DARK({ true }, { strings().theme.dark() }),
    LIGHT({ false }, { strings().theme.light() }),
    SYSTEM({ isSystemInDarkTheme() }, { strings().theme.system() });

    override fun toString(): String {
        return this.displayName()
    }
}

private fun dark() = darkColorScheme(
    primary = Color.Green,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF006600),
    onPrimaryContainer = Color.White,
    error = Color.Red,
    inversePrimary = Color.Yellow
)

private fun light() = lightColorScheme(
    primary = Color.Green,
    error = Color.Red,
    inversePrimary = Color.Yellow
)

private var currentTheme: Theme = appSettings().theme
fun theme() = currentTheme
fun setTheme(theme: Theme) {
    currentTheme = theme
    appSettings().theme = theme
}

@Composable
fun colors(): ColorScheme = if(theme().isDark()) {
        dark()
    } else {
        light()
    }


fun Color.hovered(isDark: Boolean): Color = this.copy(
        alpha = alpha,
        red = red.toHover(isDark),
        green = green.toHover(isDark),
        blue = blue.toHover(isDark)
    )

@Composable
fun Color.hovered(): Color = hovered(theme().isDark())

private fun Float.toHover(isDark: Boolean): Float {
    return if(isDark) {
        this + (1 - this) / 1.5f
    } else {
        this - this / 1.5f
    }
}

fun Color.pressed(isDark: Boolean): Color = this.copy(
        alpha = alpha,
        red = red.toPressed(isDark),
        green = green.toPressed(isDark),
        blue = blue.toPressed(isDark)
    )

@Composable
fun Color.pressed(): Color = pressed(theme().isDark())

private fun Float.toPressed(isDark: Boolean): Float {
    return if(isDark) {
        this + (1 - this) / 3f
    } else {
        this - this / 3f
    }
}
