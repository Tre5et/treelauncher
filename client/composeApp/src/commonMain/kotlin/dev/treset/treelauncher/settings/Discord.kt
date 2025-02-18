package dev.treset.treelauncher.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.config.AppSettings
import dev.treset.treelauncher.generic.Text
import dev.treset.treelauncher.generic.TitledSwitch
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.*
import org.jetbrains.compose.resources.imageResource
import treelauncher.composeapp.generated.resources.Res
import treelauncher.composeapp.generated.resources.minecraft_logo

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
                title = Strings.settings.discord.title(),
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
                            Strings.settings.discord.instanceToggle(),
                            checked = AppSettings.discordShowInstance.value,
                            onCheckedChange = {
                                AppSettings.discordShowInstance.value = it
                            },
                            enabled = !disabled,
                            rowModifier = Modifier.fillMaxWidth()
                        )

                        TitledSwitch(
                            Strings.settings.discord.versionToggle(),
                            checked = AppSettings.discordShowVersion.value,
                            onCheckedChange = {
                                AppSettings.discordShowVersion.value = it
                            },
                            enabled = !disabled,
                            rowModifier = Modifier.fillMaxWidth()
                        )

                        TitledSwitch(
                            Strings.settings.discord.modLoaderToggle(),
                            checked = AppSettings.discordShowModLoader.value,
                            onCheckedChange = {
                                AppSettings.discordShowModLoader.value = it
                            },
                            enabled = !disabled,
                            rowModifier = Modifier.fillMaxWidth()
                        )

                        TitledSwitch(
                            Strings.settings.discord.timeToggle(),
                            checked = AppSettings.discordShowTime.value,
                            onCheckedChange = {
                                AppSettings.discordShowTime.value = it
                            },
                            enabled = !disabled,
                            rowModifier = Modifier.fillMaxWidth()
                        )

                        TitledSwitch(
                            Strings.settings.discord.watermarkToggle(),
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
                            .background(ColorScheme.extensions.discordBlack)
                            .padding(12.dp)
                    ) {
                        Image(
                            imageResource(Res.drawable.minecraft_logo),
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

                            val details = remember(AppSettings.discordShowInstance.value, AppSettings.discordShowVersion.value, AppSettings.discordShowModLoader.value, AppSettings.discordShowWatermark.value) {
                                Strings.settings.discord.details(
                                    Strings.settings.discord.instanceExample(),
                                    Strings.settings.discord.versionExample(),
                                    Strings.settings.discord.modLoaderExample()
                                )
                            }

                            if (details.isNotBlank()) {
                                Text(
                                    details,
                                    style = TextStyle(
                                        fontFamily = GgSansFont,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 14.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Start,
                                )
                            }
                            if (AppSettings.discordShowTime.value) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(
                                        Icons.discordActivity,
                                        "Activity icon",
                                        tint = ColorScheme.extensions.discordGreen,
                                        modifier = Modifier.size(14.dp).offset(y = (-2).dp)
                                    )
                                    Text(
                                        Strings.settings.discord.timeExample(),
                                        style = TextStyle(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Start,
                                        color = ColorScheme.extensions.discordGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}