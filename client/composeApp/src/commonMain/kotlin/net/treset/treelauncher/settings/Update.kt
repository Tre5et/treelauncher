package net.treset.treelauncher.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.config.appSettings
import net.treset.treelauncher.backend.update.updater
import net.treset.treelauncher.backend.util.string.openInBrowser
import net.treset.treelauncher.generic.*
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.navigation.NavigationContext
import net.treset.treelauncher.navigation.NavigationState
import net.treset.treelauncher.style.icons
import net.treset.treelauncher.style.info
import net.treset.treelauncher.util.onUpdate
import java.io.IOException

@Composable
fun Update() {
    val coroutineScope = rememberCoroutineScope()

    var popupContent: PopupData? by remember { mutableStateOf(null) }

    var consecutiveClicks by remember { mutableStateOf(0) }
    var lastClick by remember { mutableStateOf(0L) }

    val update = remember {
        try {
            updater().getUpdate()
        } catch(e: IOException) {
            AppContext.errorIfOnline(e)
            null
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                strings().launcher.name(),
                style = MaterialTheme.typography.titleSmall
            )
            val notificationColor = MaterialTheme.colorScheme.info
            Text(
                strings().settings.version(),
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if(appSettings().isDebug) {
                        appSettings().isDebug = false
                        NavigationContext.navigateTo(NavigationState.INSTANCES)
                        AppContext.addNotification(
                            NotificationData(
                                color = notificationColor,
                                onClick = { it.dismiss() }
                            ) {
                                LaunchedEffect(Unit) {
                                    delay(3000)
                                    it.dismiss()
                                }
                                Text(
                                    strings().settings.debugNotification(false)
                                )
                            }
                        )
                    } else {
                        val now = System.currentTimeMillis()
                        if (now - lastClick < 500) {
                            consecutiveClicks++
                        } else {
                            consecutiveClicks = 0
                        }
                        lastClick = now
                        if (consecutiveClicks >= 5) {
                            appSettings().isDebug = true
                            NavigationContext.navigateTo(NavigationState.INSTANCES)
                            AppContext.addNotification(
                                NotificationData(
                                    color = notificationColor,
                                    onClick = { it.dismiss() }
                                ) {
                                    LaunchedEffect(Unit) {
                                        delay(3000)
                                        it.dismiss()
                                    }
                                    Text(
                                        strings().settings.debugNotification(true)
                                    )
                                }
                            )
                        }
                    }
                }
            )
        }


        IconButton(
            onClick = {
                onUpdate(coroutineScope) { popupContent = it }
            },
            icon = icons().update,
            highlighted = update?.id != null,
            tooltip = strings().settings.update.tooltip(),
            enabled = AppContext.runningInstance == null
        )

        if (update?.id != null) {
            Text(
                strings().settings.update.available()
            )
        }

        IconButton(
            onClick = {
                "https://github.com/Tre5et/treelauncher".openInBrowser()
            },
            painter = icons().gitHub,
            tooltip = strings().settings.sourceTooltip()
        )
    }

    popupContent?.let {
        PopupOverlay(it)
    }
}