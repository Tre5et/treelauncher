package dev.treset.treelauncher.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.treset.treelauncher.backend.config.appSettings
import dev.treset.treelauncher.generic.PopupData
import dev.treset.treelauncher.generic.PopupOverlay
import dev.treset.treelauncher.generic.Text
import dev.treset.treelauncher.generic.TextBox
import dev.treset.treelauncher.localization.strings
import dev.treset.treelauncher.style.disabledContainer
import dev.treset.treelauncher.style.disabledContent

@Composable
fun Sync() {
    var popupContent: PopupData? by remember { mutableStateOf(null) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.disabledContainer())
            .padding(12.dp)
            .fillMaxWidth()
    ) {
        Text(
            strings().settings.sync.title(),
            style = MaterialTheme.typography.titleSmall,
            color = LocalContentColor.current.disabledContent()
        )

        var tfUrl by remember { mutableStateOf(appSettings().syncUrl ?: "") }
        var tfPort by remember { mutableStateOf(appSettings().syncPort ?: "") }
        var tfKey by remember { mutableStateOf(appSettings().syncKey ?: "") }
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "http://",
                color = LocalContentColor.current.disabledContent()
            )
            TextBox(
                tfUrl,
                {
                    tfUrl = it
                },
                placeholder = strings().settings.sync.url(),
                enabled = false
            )
            Text(
                ":",
                color = LocalContentColor.current.disabledContent()
            )
            TextBox(
                tfPort,
                {
                    tfPort = it
                },
                placeholder = strings().settings.sync.port(),
                enabled = false
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                strings().settings.sync.key(),
                color = LocalContentColor.current.disabledContent()
            )
            TextBox(
                tfKey,
                {
                    tfKey = it
                },
                placeholder = strings().settings.sync.keyPlaceholder(),
                enabled = false
            )
        }

        /*Button(
            onClick = {
                try {
                    SyncService(
                        tfUrl,
                        tfPort,
                        tfKey
                    ).testConnection()

                    popupContent = PopupData(
                        type = PopupType.SUCCESS,
                        titleRow = { Text(strings().settings.sync.success()) },
                        buttonRow = {
                            Button(
                                onClick = { popupContent = null }
                            ) {
                                Text(strings().settings.sync.close())
                            }
                        }
                    )
                    appSettings().syncUrl = tfUrl
                    appSettings().syncPort = tfPort
                    appSettings().syncKey = tfKey
                } catch (e: Exception) {
                    popupContent = PopupData(
                        type = PopupType.ERROR,
                        titleRow = { Text(strings().settings.sync.failure()) },
                        buttonRow = {
                            Button(
                                onClick = { popupContent = null }
                            ) {
                                Text(strings().settings.sync.close())
                            }
                        }
                    )
                    appSettings().syncUrl = null
                    appSettings().syncPort = null
                    appSettings().syncKey = null
                }
            },
            enabled = false
        ) {
            Text(
                strings().settings.sync.test()
            )
        }*/
    }

    popupContent?.let {
        PopupOverlay(it)
    }
}