package dev.treset.treelauncher.settings

import androidx.compose.runtime.*
import dev.treset.treelauncher.generic.PopupData

@Composable
fun Sync() {
    var popupContent: PopupData? by remember { mutableStateOf(null) }

    /*Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.disabledContainer())
            .padding(12.dp)
            .fillMaxWidth()
    ) {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            Text(
                Strings.settings.sync.title(),
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
                    placeholder = Strings.settings.sync.url(),
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
                    placeholder = Strings.settings.sync.port(),
                    enabled = false
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    Strings.settings.sync.key(),
                    color = LocalContentColor.current.disabledContent()
                )
                TextBox(
                    tfKey,
                    {
                        tfKey = it
                    },
                    placeholder = Strings.settings.sync.keyPlaceholder(),
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
                        titleRow = { Text(Strings.settings.sync.success()) },
                        buttonRow = {
                            Button(
                                onClick = { popupContent = null }
                            ) {
                                Text(Strings.settings.sync.close())
                            }
                        }
                    )
                    appSettings().syncUrl = tfUrl
                    appSettings().syncPort = tfPort
                    appSettings().syncKey = tfKey
                } catch (e: Exception) {
                    popupContent = PopupData(
                        type = PopupType.ERROR,
                        titleRow = { Text(Strings.settings.sync.failure()) },
                        buttonRow = {
                            Button(
                                onClick = { popupContent = null }
                            ) {
                                Text(Strings.settings.sync.close())
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
                Strings.settings.sync.test()
            )
        }*/
        }
    }

    popupContent?.let {
        PopupOverlay(it)
    }*/
}