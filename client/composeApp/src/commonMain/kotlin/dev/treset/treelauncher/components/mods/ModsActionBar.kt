package dev.treset.treelauncher.components.mods

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import dev.treset.treelauncher.backend.config.AppSettings
import dev.treset.treelauncher.backend.data.canMoveDown
import dev.treset.treelauncher.backend.data.copyOrder
import dev.treset.treelauncher.backend.data.moveApplicableDirection
import dev.treset.treelauncher.backend.util.assignFrom
import dev.treset.treelauncher.generic.IconButton
import dev.treset.treelauncher.generic.Text
import dev.treset.treelauncher.generic.TitledCheckBox
import dev.treset.treelauncher.generic.TooltipProvider
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.icons
import dev.treset.treelauncher.style.inverted

@Composable
fun SharedModsData.ModsActionBar() {
    if(!settingsOpen.value && !showSearch.value && editingMod.value == null) {
        var updateExpanded by remember { mutableStateOf(false) }
        val updateRotation by animateFloatAsState(if(updateExpanded) 180f else 0f)

        var settingsExpanded by remember { mutableStateOf(false) }
        val settingsRotation by animateFloatAsState(if(settingsExpanded) 90f else 0f)

        IconButton(
            onClick = {
                showSearch.value = true
            },
            icon = icons().add,
            size = 32.dp,
            tooltip = Strings.manager.mods.add()
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(end = 10.dp)
        ) {
            IconButton(
                onClick = {
                    updateExpanded = !updateExpanded
                },
                icon = icons().expand,
                size = 24.dp,
                tooltip = Strings.manager.mods.update.settings(),
                modifier = Modifier
                    .offset(x = 20.dp)
                    .rotate(updateRotation)
            )

            Box(
                contentAlignment = Alignment.Center,
            ) {
                IconButton(
                    onClick = {
                        checkUpdates.value++
                    },
                    tooltip = Strings.manager.mods.update.tooltip(),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            icons().update,
                            "Update",
                            modifier = Modifier.size(32.dp)
                        )
                        if(component.autoUpdate.value) {
                            Icon(
                                icons().auto,
                                "Auto",
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .size(18.dp)
                                    .offset(y = 4.dp, x = (-4).dp)
                            )
                        }
                    }
                }
            }

            DropdownMenu(
                expanded = updateExpanded,
                onDismissRequest = { updateExpanded = false },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(end = 8.dp)
            ) {
                ProvideTextStyle(
                    MaterialTheme.typography.bodyMedium
                ) {

                    TitledCheckBox(
                        Strings.manager.mods.update.auto(),
                        component.autoUpdate.value,
                        {
                            component.autoUpdate.value = it
                            AppSettings.modsDefaultAutoUpdate.value = it
                        }
                    )

                    if(component.autoUpdate.value) {
                        TitledCheckBox(
                            Strings.manager.mods.update.enable(),
                            component.enableOnUpdate.value,
                            {
                                component.enableOnUpdate.value = it
                                AppSettings.modsDefaultEnableOnUpdate.value = it
                            }
                        )
                    }

                    TitledCheckBox(
                        Strings.manager.mods.update.disable(),
                        component.disableOnNoVersion.value,
                        {
                            component.disableOnNoVersion.value = it
                            AppSettings.modsDefaultDisableOnNoVersion.value = it
                        }
                    )
                }
            }
        }

        Box(
            contentAlignment = Alignment.Center,
        ) {
            IconButton(
                onClick = {
                    settingsExpanded = !settingsExpanded
                },
                icon = icons().settings,
                size = 24.dp,
                tooltip = Strings.manager.mods.settings.tooltip(),
                modifier = Modifier.rotate(settingsRotation)
            )

            DropdownMenu(
                expanded = settingsExpanded,
                onDismissRequest = { settingsExpanded = false },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        Strings.manager.mods.settings.providers(),
                        style = MaterialTheme.typography.titleSmall,
                    )

                    TooltipProvider(
                        tooltip = Strings.manager.mods.settings.help(),
                        delay = 0
                    ) {
                        Icon(
                            imageVector = icons().help,
                            "Help",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                ProvideTextStyle(
                    MaterialTheme.typography.bodyMedium
                ) {
                    component.providers.forEach {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    component.providers.moveApplicableDirection(it)
                                    AppSettings.modsDefaultProviders.assignFrom(component.providers.copyOrder())
                                },
                                icon = icons().down,
                                size = 20.dp,
                                tooltip = Strings.manager.mods.settings.order(component.providers.canMoveDown(it)),
                                modifier = Modifier.rotate(if(component.providers.canMoveDown(it)) 0f else 180f)
                            )

                            IconButton(
                                onClick = {
                                    it.enabled.value = !it.enabled.value
                                },
                                icon = if(it.enabled.value) icons().minus else icons().plus,
                                size = 20.dp,
                                tooltip = Strings.manager.mods.settings.state(it.enabled.value),
                                enabled = !it.enabled.value || component.providers.find { it.enabled.value } != null
                            )

                            Text(
                                Strings.manager.mods.settings.modProvider(it.provider),
                                style = LocalTextStyle.current.let { style ->
                                    if(!it.enabled.value) {
                                        style.copy(
                                            color = LocalTextStyle.current.color.copy(alpha = 0.8f).inverted(),
                                            fontStyle = FontStyle.Italic,
                                            textDecoration = TextDecoration.LineThrough
                                        )
                                    } else style
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}