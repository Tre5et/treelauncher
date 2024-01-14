package net.treset.treelauncher.style

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import net.treset.treelauncher.backend.config.appSettings
import net.treset.treelauncher.localization.strings
import org.jetbrains.jewel.intui.window.styling.dark
import org.jetbrains.jewel.intui.window.styling.light
import org.jetbrains.jewel.window.styling.TitleBarColors
import org.jetbrains.jewel.window.styling.TitleBarStyle

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
    error = Color.Red,
    inversePrimary = Color.Yellow,
    tertiary = Color(0xFF43454A),
    onBackground = Color.White
)

private fun light() = lightColorScheme(
    primary = Color(0xFF00E000),
    onPrimary = Color.Black,
    error = Color(0xFFD00000),
    inversePrimary = Color(0xFFD0A000),
    tertiary = Color(0xFFB4B6BB),
    onBackground = Color.Black
)

@Composable
private fun darkTitleBar() = TitleBarStyle.dark(
    colors = TitleBarColors.dark(
        backgroundColor = dark().background,
        inactiveBackground = dark().background,
        borderColor = dark().tertiary
    )
)

@Composable
private fun lightTitleBar() = TitleBarStyle.light(
    colors = TitleBarColors.light(
        backgroundColor = light().background,
        inactiveBackground = light().background,
        borderColor = light().tertiary
    )
)

private var currentTheme: Theme = appSettings().theme

fun theme() = currentTheme
fun setTheme(theme: Theme) {
    currentTheme = theme
    appSettings().theme = theme
}

@Composable
fun colors() = if(theme().isDark()) {
    dark()
} else {
    light()
}

@Composable
fun titleBar() = if(theme().isDark()) {
    darkTitleBar()
} else {
    lightTitleBar()
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
