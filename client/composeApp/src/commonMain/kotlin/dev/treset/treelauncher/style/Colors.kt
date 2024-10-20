package dev.treset.treelauncher.style

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import dev.treset.treelauncher.backend.config.AppSettings
import dev.treset.treelauncher.backend.util.serialization.ColorData
import dev.treset.treelauncher.localization.Strings
import kotlinx.serialization.Serializable
import org.jetbrains.jewel.intui.window.styling.dark
import org.jetbrains.jewel.intui.window.styling.light
import org.jetbrains.jewel.window.styling.TitleBarColors
import org.jetbrains.jewel.window.styling.TitleBarStyle

enum class Theme(val isDark: @Composable () -> Boolean, val displayName: () -> String) {
    DARK({ true }, { Strings.theme.dark() }),
    LIGHT({ false }, { Strings.theme.light() }),
    SYSTEM({ isSystemInDarkTheme() }, { Strings.theme.system() });

    override fun toString(): String {
        return this.displayName()
    }
}

enum class AccentColor(val primary: (dark: Boolean) -> Color, val displayName: () -> String) {
    GREEN(
        {if(it) Color.Green else Color(0xFF00E000)},
        { Strings.theme.green() }
    ),
    BLUE(
        {if(it) Color.Blue else Color(0xFF0000E0)},
        { Strings.theme.blue() }
    ),
    ORANGE(
        {if(it) Color(0xFFE0A000) else Color(0xFFD0A000)},
        { Strings.theme.orange() }
    ),
    MAGENTA(
        {if(it) Color.Magenta else Color(0xFFE000E0)},
        { Strings.theme.magenta() }
    ),
    CUSTOM(
        { AppSettings.customColor.value },
        { Strings.theme.custom() }
    );

    override fun toString(): String {
        return this.displayName()
    }
}

@Serializable
data class UserColors(
    val background: ColorData? = null,
    val secondary: ColorData? = null,
    val secondaryContainer: ColorData? = null,
    val info: ColorData? = null,
    val warning: ColorData? = null,
    val error: ColorData? = null,
    val contentColors: ContentColors? = null
) {
    fun toColors(
        accent: AccentColor,
        material: ColorScheme,
        extensions: ExtensionColors
    ): Colors {
        val contentColors = contentColors ?: ContentColors()

        val secondary = secondary ?: material.secondary
        val secondaryContainer = secondaryContainer ?: material.secondaryContainer
        val error = error ?: material.error
        val background = background ?: material.background

        return Colors(
            material = material.copy(
                primary = accent.primary(true),
                onPrimary = contentColors.on(accent.primary(true)),
                secondary = secondary,
                onSecondary = contentColors.on(secondary),
                secondaryContainer = secondaryContainer,
                onSecondaryContainer = contentColors.on(secondaryContainer),
                error = error,
                onError = contentColors.on(error),
                background = background,
                onBackground = contentColors.on(background),
            ),
            extensions = extensions.copy(
                info = info ?: extensions.info,
                onInfo = info?.let { contentColors.on(it) } ?: extensions.onInfo,
                warning = warning ?: extensions.warning,
                onWarning = warning?.let { contentColors.on(it) } ?: extensions.onWarning,
            ),
            contentColors = contentColors
        )
    }
}

@Serializable
data class ContentColors(
    val light: ColorData = Color.White,
    val dark: ColorData = Color.Black,
)

data class ExtensionColors(
    val warning: Color = Color.Yellow,
    val onWarning: Color = Color.Black,
    val info: Color = Color.Cyan,
    val onInfo: Color = Color.Black,
    val popupScrim: Color = Color.Black.copy(alpha = 0.8f),
)

data class Colors(
    val material: ColorScheme,
    val extensions: ExtensionColors,
    val contentColors: ContentColors
)

fun darkColors(
    accent: AccentColor = AccentColor.GREEN,
    userColors: UserColors = UserColors()
) = userColors.toColors(
    accent = accent,
    material = darkColorScheme(
        primary = accent.primary(true),
        secondaryContainer = Color(0xFF373342),
        error = Color(0xFFE20505),
        scrim = Color.Black.copy(alpha = 0.85f)
    ),
    extensions = ExtensionColors(
        warning = Color(0xFFEBDC02)
    )
)

fun lightColors(
    accent: AccentColor = AccentColor.GREEN,
    userColors: UserColors = UserColors()
) = userColors.toColors(
    accent = accent,
    material = lightColorScheme(
        primary = accent.primary(false),
        error = Color(0xFFD00000),
        scrim = Color.White.copy(alpha = 0.85f)
    ),
    extensions = ExtensionColors(
        warning = Color(0xFFD0A000)
    )
)

@Composable
fun darkTitleBar() = TitleBarStyle.dark(
    colors = TitleBarColors.dark(
        backgroundColor = darkColors().material.background,
        inactiveBackground = darkColors().material.background,
        borderColor = darkColors().material.background
    )
)

@Composable
fun lightTitleBar() = TitleBarStyle.light(
    colors = TitleBarColors.light(
        backgroundColor = lightColors().material.background,
        inactiveBackground = lightColors().material.background,
        borderColor = lightColors().material.background
    )
)

fun ContentColors.on(color: Color): Color {
    val darkContrast = color.contrast(light)
    return if(darkContrast < 4.5f && color.contrast(dark) > darkContrast) {
        dark
    } else {
        light
    }
}

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

val ColorScheme by mutableStateOf(lightColors())

fun Color.inverted(): Color = Color(
    red = 1f - red,
    green = 1f - green,
    blue = 1f - blue,
    alpha = alpha
)


val ColorScheme.warning: Color
    get() = ColorScheme.extensions.warning

val ColorScheme.onWarning: Color
    get() = ColorScheme.extensions.onWarning

val ColorScheme.info: Color
    get() = ColorScheme.extensions.info

val ColorScheme.onInfo: Color
    get() = ColorScheme.extensions.onInfo

val ColorScheme.contentColors: ContentColors
    get() = ColorScheme.contentColors

@Composable
fun ColorScheme.contentColor(backgroundColor: Color): Color =
    when(backgroundColor) {
        warning -> onWarning
        info -> onInfo
        else -> contentColors.on(backgroundColor)
    }