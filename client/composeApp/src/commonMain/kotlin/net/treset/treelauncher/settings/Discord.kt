package net.treset.treelauncher.settings

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
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.config.appSettings
import net.treset.treelauncher.backend.discord.DiscordIntegration
import net.treset.treelauncher.generic.Text
import net.treset.treelauncher.generic.TitledSwitch
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.GgSansFont
import net.treset.treelauncher.style.disabledContainer
import net.treset.treelauncher.style.disabledContent

@Composable
fun Discord() {
    var enabled by remember { mutableStateOf(appSettings().discordIntegration) }
    var instance by remember { mutableStateOf(appSettings().discordShowInstance) }
    var version by remember { mutableStateOf(appSettings().discordShowVersion) }
    var modLoader by remember { mutableStateOf(appSettings().discordShowModLoader) }
    var time by remember { mutableStateOf(appSettings().discordShowTime) }
    var watermark by remember { mutableStateOf(appSettings().discordShowWatermark) }

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

        TitledSwitch(
            title = strings().settings.discord.title(),
            checked = enabled,
            onCheckedChange = {
                enabled = it
                appSettings().discordIntegration = it
            },
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.offset(y = (-1).dp),
            enabled = !disabled,
        )

        AnimatedVisibility(
            visible = enabled,
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
                        strings().settings.discord.instance(),
                        checked = instance,
                        onCheckedChange = {
                            instance = it
                            appSettings().discordShowInstance = it
                        },
                        enabled = !disabled,
                        rowModifier = Modifier.fillMaxWidth()
                    )

                    TitledSwitch(
                        strings().settings.discord.version(),
                        checked = version,
                        onCheckedChange = {
                            version = it
                            appSettings().discordShowVersion = version
                        },
                        enabled = !disabled,
                        rowModifier = Modifier.fillMaxWidth()
                    )

                    TitledSwitch(
                        strings().settings.discord.modLoader(),
                        checked = modLoader,
                        onCheckedChange = {
                            modLoader = it
                            appSettings().discordShowModLoader = it
                        },
                        enabled = !disabled,
                        rowModifier = Modifier.fillMaxWidth()
                    )

                    TitledSwitch(
                        strings().settings.discord.time(),
                        checked = time,
                        onCheckedChange = {
                            time = it
                            appSettings().discordShowTime = it
                        },
                        enabled = !disabled,
                        rowModifier = Modifier.fillMaxWidth()
                    )

                    TitledSwitch(
                        strings().settings.discord.watermark(),
                        checked = watermark,
                        onCheckedChange = {
                            watermark = it
                            appSettings().discordShowWatermark = it
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
                        .fillMaxWidth()
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

                        val details = remember(instance, version, modLoader, watermark) { DiscordIntegration.constructDetailsString("MyInstance", "1.20.5", "Fabric") }

                        if(details.isNotBlank()) {
                            Text(
                                details,
                                style = textStyle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Start,
                            )
                        }
                        if(time) {
                            Text(
                                "01:36 elapsed",
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