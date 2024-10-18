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
import dev.treset.treelauncher.backend.config.AppSettings
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

    val dark = AppSettings.theme.value.isDark()

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
                            AppSettings.theme.value = Theme.DARK
                        },
                        tooltip = Theme.DARK.displayName(),
                        icon = icons().darkMode,
                        selected = AppSettings.theme.value == Theme.DARK,
                        modifier = Modifier.clip(RoundedCornerShape(100.dp))
                    )

                    IconButton(
                        onClick = {
                            AppSettings.theme.value = Theme.LIGHT
                        },
                        tooltip = Theme.LIGHT.displayName(),
                        icon = icons().lightMode,
                        selected = AppSettings.theme.value == Theme.LIGHT,
                        modifier = Modifier.clip(RoundedCornerShape(100.dp))
                    )

                    IconButton(
                        onClick = {
                            AppSettings.theme.value = Theme.SYSTEM
                        },
                        tooltip = Theme.SYSTEM.displayName(),
                        icon = icons().systemMode,
                        selected = AppSettings.theme.value == Theme.SYSTEM,
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
                                    colorPicker = ColorPickerData(AppSettings.customColor.value) {
                                        AppSettings.customColor.value = it
                                        AppSettings.accentColor.value = AccentColor.CUSTOM
                                    }
                                },
                                tooltip = it.displayName()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(AppSettings.customColor.value)
                                ) {
                                    if (AppSettings.accentColor.value == it) {
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
                                    AppSettings.accentColor.value = it
                                },
                                tooltip = it.displayName()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(it.primary(dark))
                                ) {
                                    if (AppSettings.accentColor.value == it) {
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
                val colors = remember(AppSettings.theme.value, AppSettings.darkColors.value, AppSettings.lightColors.value) {
                    if (dark) AppSettings.darkColors else AppSettings.lightColors
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
                                colors.value.background ?: MaterialTheme.colorScheme.background,
                                strings().settings.appearance.backgroundTooltip(),
                                {
                                    colors.value = colors.value.copy(background = it)
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
                                colors.value.secondaryContainer ?: MaterialTheme.colorScheme.secondaryContainer,
                                strings().settings.appearance.containerTooltip(),
                                {
                                    colors.value = colors.value.copy(secondaryContainer = it)
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
                                colors.value.contentColors?.light ?: MaterialTheme.colorScheme.contentColors.light,
                                strings().settings.appearance.textLight(),
                                {
                                    val contentColors = colors.value.contentColors?.copy(light = it) ?: ContentColors(light = it)
                                    colors.value = colors.value.copy(contentColors = contentColors)
                                },
                                {
                                    colorPicker = it
                                }
                            )
                            CustomColorSelector(
                                colors.value.contentColors?.dark ?: MaterialTheme.colorScheme.contentColors.dark,
                                strings().settings.appearance.textDark(),
                                {
                                    val contentColors = colors.value.contentColors?.copy(dark = it) ?: ContentColors(dark = it)
                                    colors.value = colors.value.copy(contentColors = contentColors)
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
                            colors.value = UserColors()
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
            val currentScalingIndex = remember(AppSettings.displayScale.value) {
                var nearest = 7
                var distance = Int.MAX_VALUE
                displayScales.forEachIndexed { index, displayScale ->
                    if (abs(AppSettings.displayScale.value - displayScale) < distance) {
                        distance =AppSettings.displayScale.value - displayScale
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
                            AppSettings.displayScale.value = displayScales[currentScalingIndex - 1]
                        }
                    },
                    icon = icons().minus,
                    tooltip = strings().settings.appearance.decrement(),
                    enabled = currentScalingIndex > 0
                )
                Text(strings().settings.appearance.scaling(AppSettings.displayScale.value))
                IconButton(
                    onClick = {
                        if (currentScalingIndex < displayScales.size - 1) {
                            AppSettings.displayScale.value = displayScales[currentScalingIndex + 1]
                        }
                    },
                    icon = icons().plus,
                    tooltip = strings().settings.appearance.increment(),
                    enabled = currentScalingIndex < displayScales.size - 1
                )
            }

            if (AppSettings.displayScale.value < 1000 || AppSettings.displayScale.value > 1300) {
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
                        if (AppSettings.displayScale.value < 1000)
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