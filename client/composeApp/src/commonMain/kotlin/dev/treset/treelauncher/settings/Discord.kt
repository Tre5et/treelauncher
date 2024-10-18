package dev.treset.treelauncher.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.config.AppSettings
import dev.treset.treelauncher.generic.Text
import dev.treset.treelauncher.generic.TitledSwitch
import dev.treset.treelauncher.localization.strings
import dev.treset.treelauncher.style.GgSansFont
import dev.treset.treelauncher.style.disabledContainer
import dev.treset.treelauncher.style.disabledContent

@Composable
fun Discord() {
    val disabled = remember(AppContext.runningInstance) { AppContext.runningInstance != null }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.let { if(disabled) it.disabledContainer() else it })
            .padding(12.dp)
            .fillMaxWidth()
    ) {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            TitledSwitch(
                title = strings().settings.discord.title(),
                checked = AppSettings.discordIntegration.value,
                onCheckedChange = {
                    AppSettings.discordIntegration.value = it
                },
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.offset(y = (-1).dp),
                enabled = !disabled,
            )

            AnimatedVisibility(
                visible = AppSettings.discordIntegration.value,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(IntrinsicSize.Max)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.width(IntrinsicSize.Max)
                    ) {
                        TitledSwitch(
                            strings().settings.discord.instanceToggle(),
                            checked = AppSettings.discordShowInstance.value,
                            onCheckedChange = {
                                AppSettings.discordShowInstance.value = it
                            },
                            enabled = !disabled,
                            rowModifier = Modifier.fillMaxWidth()
                        )

                        TitledSwitch(
                            strings().settings.discord.versionToggle(),
                            checked = AppSettings.discordShowVersion.value,
                            onCheckedChange = {
                                AppSettings.discordShowVersion.value = it
                            },
                            enabled = !disabled,
                            rowModifier = Modifier.fillMaxWidth()
                        )

                        TitledSwitch(
                            strings().settings.discord.modLoaderToggle(),
                            checked = AppSettings.discordShowModLoader.value,
                            onCheckedChange = {
                                AppSettings.discordShowModLoader.value = it
                            },
                            enabled = !disabled,
                            rowModifier = Modifier.fillMaxWidth()
                        )

                        TitledSwitch(
                            strings().settings.discord.timeToggle(),
                            checked = AppSettings.discordShowTime.value,
                            onCheckedChange = {
                                AppSettings.discordShowTime.value = it
                            },
                            enabled = !disabled,
                            rowModifier = Modifier.fillMaxWidth()
                        )

                        TitledSwitch(
                            strings().settings.discord.watermarkToggle(),
                            checked = AppSettings.discordShowWatermark.value,
                            onCheckedChange = {
                                AppSettings.discordShowWatermark.value = it
                            },
                            enabled = !disabled,
                            rowModifier = Modifier.fillMaxWidth()
                        )
                    }

                    VerticalDivider(
                        thickness = 2.dp,
                        color = LocalContentColor.current.disabledContent(),
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier
                            .width(295.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF111214))
                            .padding(12.dp)
                    ) {
                        Image(
                            useResource("img/minecraft_logo.png") { loadImageBitmap(it) },
                            "Minecraft logo",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(1.dp),
                            horizontalAlignment = Alignment.Start,
                        ) {
                            Text(
                                "Minecraft",
                                style = TextStyle(
                                    fontFamily = GgSansFont,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                ),
                                textAlign = TextAlign.Start,
                            )

                            val textStyle = TextStyle(
                                fontFamily = GgSansFont,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp
                            )

                            val details = remember(AppSettings.discordShowInstance.value, AppSettings.discordShowVersion.value, AppSettings.discordShowModLoader.value, AppSettings.discordShowWatermark.value) {
                                strings().settings.discord.details(
                                    strings().settings.discord.instanceExample(),
                                    strings().settings.discord.versionExample(),
                                    strings().settings.discord.modLoaderExample()
                                )
                            }

                            if (details.isNotBlank()) {
                                Text(
                                    details,
                                    style = textStyle,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Start,
                                )
                            }
                            if (AppSettings.discordShowTime.value) {
                                Text(
                                    strings().settings.discord.timeExample() + strings().settings.discord.timeSuffix(),
                                    style = textStyle,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Start,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}