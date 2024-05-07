package net.treset.treelauncher.settings

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.config.appSettings
import net.treset.treelauncher.generic.*
import net.treset.treelauncher.localization.Language
import net.treset.treelauncher.localization.language
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.AccentColor
import net.treset.treelauncher.style.Theme
import net.treset.treelauncher.style.disabledContent
import net.treset.treelauncher.style.icons
import net.treset.treelauncher.util.HsvColor
import org.jetbrains.jewel.ui.util.toRgbaHexString

@Composable
fun Appearance() {
    var language by remember { mutableStateOf(language().appLanguage) }

    var theme by remember { mutableStateOf(appSettings().theme) }
    var accentColor by remember { mutableStateOf(appSettings().accentColor) }
    var customColor by remember { mutableStateOf(appSettings().customColor) }

    var showColorPicker by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(12.dp)
            .fillMaxWidth()
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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp)
        ) {
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

            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
            )

            Row(
                modifier = Modifier
                    .border(1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(100.dp))
                    .padding(1.dp)
            ) {
                AccentColor.entries.forEach {
                    if(it == AccentColor.CUSTOM) {
                        IconButton(
                            onClick = {
                                showColorPicker = true
                            },
                            tooltip = it.displayName()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(customColor)
                            ) {
                                if(accentColor == it) {
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
                                        tint = it.onPrimary(theme.isDark())
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

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(strings().settings.appearance.displayScale())
            IconButton(
                onClick = {
                    (appSettings().displayScale - 0.1f).let {
                        if(it >= 0.5f) {
                            appSettings().displayScale = it
                        } else {
                            appSettings().displayScale = 0.5f
                        }
                    }
                },
                icon = icons().minus,
                tooltip = strings().settings.appearance.decrement(),
                enabled = appSettings().displayScale > 0.5f
            )
            Text(strings().settings.appearance.scaling(appSettings().displayScale))
            IconButton(
                onClick = {
                    (appSettings().displayScale + 0.1f).let {
                        if(it <= 2f) {
                            appSettings().displayScale = it
                        } else {
                            appSettings().displayScale = 2f
                        }
                    }
                },
                icon = icons().plus,
                tooltip = strings().settings.appearance.increment(),
                enabled = appSettings().displayScale < 2f
            )
        }

        if(appSettings().displayScale < 1f || appSettings().displayScale > 1.3f) {
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
                    if(appSettings().displayScale < 1f)
                        strings().settings.appearance.smallHint()
                    else
                        strings().settings.appearance.largeHint(),
                    style = MaterialTheme.typography.labelMedium,
                    color = LocalContentColor.current.disabledContent()
                )
            }
        }

        // Font scale seems too useless and complicated
    }

    if(showColorPicker) {
        var color by remember { mutableStateOf(HsvColor.from(customColor)) }
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
                        showColorPicker = false
                    },
                    color = MaterialTheme.colorScheme.error
                ) {
                    Text(strings().settings.theme.cancel())
                }
                Button(
                    onClick = {
                        customColor = color.toColor()
                        accentColor = AccentColor.CUSTOM
                        AppContext.setCustomColor(color.toColor())
                        AppContext.setAccentColor(AccentColor.CUSTOM)
                        showColorPicker = false
                    },
                    color = color.toColor()
                ) {
                    Text(strings().settings.theme.confirm())
                }
            }
        )
    }
}