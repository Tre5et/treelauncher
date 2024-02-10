package net.treset.treelauncher.style

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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
    error = Color(0xFFE20505),
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
        this + (1 - this) / 2.2f
    } else {
        this - this / 2.2f
    }
}

fun Color.disabledContent(): Color = this.copy(alpha = 0.38f)

fun Color.disabledContainer(): Color = this.copy(alpha = 0.12f)

fun Color.contrast(other: Color): Float {
    val l1 = this.luminance()
    val l2 = other.luminance()
    return if(l1 > l2) {
        (l1 + 0.05f) / (l2 + 0.05f)
    } else {
        (l2 + 0.05f) / (l1 + 0.05f)
    }
}

fun Color.inverted(): Color = Color(
    red = red.inverted(),
    green = green.inverted(),
    blue = blue.inverted(),
    alpha = this.alpha
)

private fun Float.inverted() = 1f - this