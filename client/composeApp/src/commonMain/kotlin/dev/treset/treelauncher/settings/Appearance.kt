package dev.treset.treelauncher.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.config.appSettings
import dev.treset.treelauncher.generic.*
import dev.treset.treelauncher.localization.Language
import dev.treset.treelauncher.localization.language
import dev.treset.treelauncher.localization.strings
import dev.treset.treelauncher.style.*
import dev.treset.treelauncher.util.HsvColor
import org.jetbrains.jewel.ui.util.toRgbaHexString
import kotlin.math.abs

class ColorPickerData(
    val color: Color,
    val onColorChanged: (Color) -> Unit
)

@Composable
fun Appearance() {
    var language by remember { mutableStateOf(language().appLanguage) }

    var theme by remember { mutableStateOf(appSettings().theme) }
    val dark = theme.isDark()
    var accentColor by remember { mutableStateOf(appSettings().accentColor) }
    var customColor by remember { mutableStateOf(appSettings().customColor) }
    var darkColors by remember { mutableStateOf(appSettings().darkColors) }
    var lightColors by remember { mutableStateOf(appSettings().lightColors) }

    var colorPicker: ColorPickerData? by remember { mutableStateOf(null) }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(12.dp)
            .fillMaxWidth()
    ) {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            Text(
                strings().settings.appearance.title(),
                style = MaterialTheme.typography.titleSmall
            )

            TitledComboBox(
                strings().settings.language(),
                items = Language.entries,
                onSelected = {
                    language = it
                    language().appLanguage = it
                },
                selected = language
            )

            var expanded by remember { mutableStateOf(false) }
            val rotation by animateFloatAsState(if (expanded) 0f else -90f)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp).offset(x = (-16).dp)
            ) {
                IconButton(
                    onClick = { expanded = !expanded },
                    icon = icons().expand,
                    modifier = Modifier.rotate(rotation),
                    tooltip = strings().settings.appearance.tooltipAdvanced(expanded)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .border(1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(100.dp))
                        .padding(5.dp)
                ) {
                    IconButton(
                        onClick = {
                            theme = Theme.DARK
                            AppContext.setTheme(Theme.DARK)
                        },
                        tooltip = Theme.DARK.displayName(),
                        icon = icons().darkMode,
                        selected = theme == Theme.DARK,
                        modifier = Modifier.clip(RoundedCornerShape(100.dp))
                    )

                    IconButton(
                        onClick = {
                            theme = Theme.LIGHT
                            AppContext.setTheme(Theme.LIGHT)
                        },
                        tooltip = Theme.LIGHT.displayName(),
                        icon = icons().lightMode,
                        selected = theme == Theme.LIGHT,
                        modifier = Modifier.clip(RoundedCornerShape(100.dp))
                    )

                    IconButton(
                        onClick = {
                            theme = Theme.SYSTEM
                            AppContext.setTheme(Theme.SYSTEM)
                        },
                        tooltip = Theme.SYSTEM.displayName(),
                        icon = icons().systemMode,
                        selected = theme == Theme.SYSTEM,
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                    )
                }

                Row(
                    modifier = Modifier
                        .border(1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(100.dp))
                        .padding(1.dp)
                ) {
                    AccentColor.entries.forEach {
                        if (it == AccentColor.CUSTOM) {
                            IconButton(
                                onClick = {
                                    colorPicker = ColorPickerData(customColor) {
                                        customColor = it
                                        accentColor = AccentColor.CUSTOM
                                        AppContext.setCustomColor(it)
                                        AppContext.setAccentColor(AccentColor.CUSTOM)
                                    }
                                },
                                tooltip = it.displayName()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(customColor)
                                ) {
                                    if (accentColor == it) {
                                        Icon(
                                            imageVector = icons().check,
                                            contentDescription = it.displayName(),
                                            modifier = Modifier
                                                .size(24.dp)
                                                .align(Alignment.Center),
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    } else {
                                        Icon(
                                            imageVector = icons().edit,
                                            contentDescription = it.displayName(),
                                            modifier = Modifier
                                                .size(20.dp)
                                                .align(Alignment.Center),
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    accentColor = it
                                    AppContext.setAccentColor(it)
                                },
                                tooltip = it.displayName()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(it.primary(theme.isDark()))
                                ) {
                                    if (accentColor == it) {
                                        Icon(
                                            imageVector = icons().check,
                                            contentDescription = it.displayName(),
                                            modifier = Modifier
                                                .size(24.dp)
                                                .align(Alignment.Center),
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                val colors = remember(theme, darkColors, lightColors) {
                    if (dark) darkColors else lightColors
                }
                val setColors: (UserColors) -> Unit = remember(theme, darkColors, lightColors) {
                    if (dark) {
                        {
                            darkColors = it
                            AppContext.setDarkColors(it)
                        }
                    } else {
                        {
                            lightColors = it
                            AppContext.setLightColors(it)
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp).offset(x = 16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(21.dp))
                            .padding(5.dp).padding(start = 7.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(strings().settings.appearance.background())
                            CustomColorSelector(
                                colors.background ?: MaterialTheme.colorScheme.background,
                                strings().settings.appearance.backgroundTooltip(),
                                {
                                    setColors(colors.copy(background = it))
                                },
                                {
                                    colorPicker = it
                                }
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(strings().settings.appearance.container())
                            CustomColorSelector(
                                colors.secondaryContainer ?: MaterialTheme.colorScheme.secondaryContainer,
                                strings().settings.appearance.containerTooltip(),
                                {
                                    setColors(colors.copy(secondaryContainer = it))
                                },
                                {
                                    colorPicker = it
                                }
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(21.dp))
                            .padding(5.dp)
                    ) {
                        Text(strings().settings.appearance.text())

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                        ) {
                            CustomColorSelector(
                                colors.contentColors?.light ?: MaterialTheme.colorScheme.contentColors.light,
                                strings().settings.appearance.textLight(),
                                {
                                    val contentColors = colors.contentColors?.copy(light = it) ?: ContentColors(light = it)
                                    setColors(colors.copy(contentColors = contentColors))
                                },
                                {
                                    colorPicker = it
                                }
                            )
                            CustomColorSelector(
                                colors.contentColors?.dark ?: MaterialTheme.colorScheme.contentColors.dark,
                                strings().settings.appearance.textDark(),
                                {
                                    val contentColors = colors.contentColors?.copy(dark = it) ?: ContentColors(dark = it)
                                    setColors(colors.copy(contentColors = contentColors))
                                },
                                {
                                    colorPicker = it
                                }
                            )
                        }
                    }

                    IconButton(
                        icon = icons().reset,
                        onClick = {
                            setColors(UserColors())
                        },
                        tooltip = strings().settings.appearance.reset()
                    )
                }
            }

            val displayScales = remember {
                arrayOf(
                    500,
                    550,
                    600,
                    650,
                    700,
                    750,
                    800,
                    850,
                    900,
                    950,
                    1000,
                    1050,
                    1100,
                    1200,
                    1300,
                    1500,
                    1750,
                    2000
                )
            }
            val currentScalingIndex = remember(appSettings().displayScale) {
                var nearest = 7
                var distance = Int.MAX_VALUE
                displayScales.forEachIndexed { index, displayScale ->
                    if (abs(appSettings().displayScale - displayScale) < distance) {
                        distance = appSettings().displayScale - displayScale
                        nearest = index
                    }
                }
                nearest
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(strings().settings.appearance.displayScale())
                IconButton(
                    onClick = {
                        if (currentScalingIndex > 0) {
                            appSettings().displayScale = displayScales[currentScalingIndex - 1]
                        }
                    },
                    icon = icons().minus,
                    tooltip = strings().settings.appearance.decrement(),
                    enabled = currentScalingIndex > 0
                )
                Text(strings().settings.appearance.scaling(appSettings().displayScale))
                IconButton(
                    onClick = {
                        if (currentScalingIndex < displayScales.size - 1) {
                            appSettings().displayScale = displayScales[currentScalingIndex + 1]
                        }
                    },
                    icon = icons().plus,
                    tooltip = strings().settings.appearance.increment(),
                    enabled = currentScalingIndex < displayScales.size - 1
                )
            }

            if (appSettings().displayScale < 1000 || appSettings().displayScale > 1300) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        icons().warning,
                        "",
                        tint = LocalContentColor.current.disabledContent(),
                        modifier = Modifier.size(18.dp).offset(y = (-1).dp)
                    )
                    Text(
                        if (appSettings().displayScale < 1000)
                            strings().settings.appearance.smallHint()
                        else
                            strings().settings.appearance.largeHint(),
                        style = MaterialTheme.typography.labelMedium,
                        color = LocalContentColor.current.disabledContent()
                    )
                }
            }

        }
    }

    colorPicker?.let {
        var color by remember { mutableStateOf(HsvColor.from(it.color)) }
        var colorString by remember(color) { mutableStateOf(color.toColor().toRgbaHexString().removeRange(0,1).let {
            if(it.length == 8) it.removeRange(6,8) else it
        }) }

        PopupOverlay(
            titleRow = { Text(strings().settings.theme.title()) },
            content = {
                ColorPicker(
                    onColorChanged = {
                        color = it
                    },
                    color = color,
                    modifier = Modifier.size(400.dp)
                )
                TextBox(
                    colorString,
                    {
                        colorString = it
                        if(it.length == 6) {
                            try {
                                color = HsvColor.from(Color(("ff${it}").toLong(16)))
                            } catch (exception: NumberFormatException) {
                                //ignore
                            }
                        }
                    },
                    prefix = {
                        Text(
                            "#",
                            modifier = Modifier.offset(y = 4.dp)
                        )
                    },
                    inputAcceptable = { it.length <= 6 && it.all { c -> c in '0'..'9' || c in 'a'..'f' || c in 'A'..'F' } }
                )
            },
            buttonRow = {
                Button(
                    onClick = {
                        colorPicker = null
                    },
                    color = MaterialTheme.colorScheme.error
                ) {
                    Text(strings().settings.theme.cancel())
                }
                Button(
                    onClick = {
                        it.onColorChanged(color.toColor())
                        colorPicker = null
                    },
                    color = color.toColor()
                ) {
                    Text(strings().settings.theme.confirm())
                }
            }
        )
    }
}

@Composable
fun CustomColorSelector(
    color: Color,
    tooltip: String,
    onColorChanged: (Color) -> Unit,
    openColorPicker: (ColorPickerData) -> Unit,
) {
    IconButton(
        onClick = {
            openColorPicker(ColorPickerData(color, onColorChanged))
        },
        tooltip = tooltip
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color)
                .border(1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(16.dp))
        )
    }
}