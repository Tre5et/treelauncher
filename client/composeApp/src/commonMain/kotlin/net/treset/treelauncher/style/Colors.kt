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

enum class AccentColor(val primary: (dark: Boolean) -> Color, val onPrimary: (dark: Boolean) -> Color, val displayName: () -> String) {
    GREEN(
        {if(it) Color.Green else Color(0xFF00E000)},
        { Color.Black },
        { strings().theme.green() }
    ),
    BLUE(
        {if(it) Color.Blue else Color(0xFF0000E0)},
        { Color.White },
        { strings().theme.blue() }
    ),
    ORANGE(
        {if(it) Color(0xFFE0A000) else Color(0xFFD0A000)},
        { Color.Black },
        { strings().theme.orange() }
    ),
    MAGENTA(
        {if(it) Color.Magenta else Color(0xFFE000E0)},
        { Color.Black },
        { strings().theme.magenta() }
    ),
    CUSTOM(
        { appSettings().customColor },
        { if(appSettings().customColor.contrast(Color.Black) > appSettings().customColor.contrast(Color.White)) Color.Black else Color.White },
        { strings().theme.custom() }
    );

    override fun toString(): String {
        return this.displayName()
    }
}

fun darkColors() = darkColors(AccentColor.GREEN)

fun lightColors() = lightColors(AccentColor.GREEN)

fun darkColors(primary: AccentColor) = darkColorScheme(
    primary = primary.primary(true),
    onPrimary = primary.onPrimary(true),
    error = Color(0xFFE20505),
    inversePrimary = Color(0xFFEBDC02),
    tertiary = Color(0xFF43454A),
    onBackground = Color.White
)

fun lightColors(primary: AccentColor) = lightColorScheme(
    primary = primary.primary(false),
    onPrimary = primary.onPrimary(false),
    error = Color(0xFFD00000),
    inversePrimary = Color(0xFFD0A000),
    tertiary = Color(0xFFB4B6BB),
    onBackground = Color.Black
)

@Composable
fun darkTitleBar() = TitleBarStyle.dark(
    colors = TitleBarColors.dark(
        backgroundColor = darkColors().background,
        inactiveBackground = darkColors().background,
        borderColor = darkColors().tertiary
    )
)

@Composable
fun lightTitleBar() = TitleBarStyle.light(
    colors = TitleBarColors.light(
        backgroundColor = lightColors().background,
        inactiveBackground = lightColors().background,
        borderColor = lightColors().tertiary
    )
)

fun Color.hovered(): Color = this.copy(
        alpha = alpha,
        red = red.toHover(),
        green = green.toHover(),
        blue = blue.toHover()
    )

private fun Float.toHover(): Float = this + (1f - this) / 3f

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