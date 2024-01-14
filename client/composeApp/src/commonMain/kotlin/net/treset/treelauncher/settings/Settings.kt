package net.treset.treelauncher.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import net.treset.treelauncher.backend.auth.userAuth
import net.treset.treelauncher.backend.config.GlobalConfigLoader
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.config.appSettings
import net.treset.treelauncher.backend.sync.SyncService
import net.treset.treelauncher.backend.update.updater
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.backend.util.string.UrlString
import net.treset.treelauncher.generic.*
import net.treset.treelauncher.localization.Language
import net.treset.treelauncher.localization.language
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.login.LoginContext
import net.treset.treelauncher.style.Theme
import net.treset.treelauncher.style.icons
import net.treset.treelauncher.style.setTheme
import net.treset.treelauncher.style.theme
import java.awt.image.BufferedImage
import java.io.IOException


@Composable
fun Settings(
    loginContext: LoginContext
) {
    var userImage: BufferedImage? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        userImage = userAuth().getUserIcon()
    }

    var language by remember { mutableStateOf(language().appLanguage) }

    var popupContent: PopupData? by remember { mutableStateOf(null) }

    TitledColumn(
        title = strings().settings.title(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                strings().settings.appearance(),
                style = MaterialTheme.typography.titleSmall
            )

            TitledComboBox(
                strings().settings.language(),
                items = Language.entries,
                onSelected = {
                    language = it
                    language().appLanguage = it
                },
                defaultSelected = language
            )

            var restart by remember { mutableStateOf(false) }
            TitledComboBox(
                strings().settings.theme(),
                items = Theme.entries,
                onSelected = {
                    setTheme(it)
                    restart = true
                },
                defaultSelected = theme()
            )

            if(restart) {
                Text(
                    strings().settings.restratRequired(),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                strings().settings.path.title(),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            var tfValue by remember { mutableStateOf(appConfig().BASE_DIR.absolutePath) }
            TextBox(
                tfValue,
                {
                    tfValue = it
                },
                showClear = false
            )

            var cbState by remember { mutableStateOf(true) }
            TitledCheckBox(
                cbState,
                onCheckedChange = {
                    cbState = it
                },
                text = strings().settings.path.remove()
            )

            Button(
                onClick = {
                    val dir = LauncherFile.of(tfValue)

                    if(!dir.isDirectory()) {
                        popupContent = PopupData(
                            type = PopupType.ERROR,
                            titleRow = { Text(strings().settings.path.invalid()) },
                            buttonRow = {
                                Button(
                                    onClick = { popupContent = null }
                                ) {
                                    Text(strings().settings.path.close())
                                }
                            }
                        )
                    } else {
                        popupContent = PopupData(
                            titleRow = { Text(strings().settings.path.changing()) }
                        )

                        Thread {
                            try {
                                GlobalConfigLoader().updatePath(dir, cbState)

                                popupContent = PopupData(
                                    type = PopupType.SUCCESS,
                                    titleRow = { Text(strings().settings.path.success())},
                                    buttonRow = {
                                        Button(
                                            onClick = { popupContent = null }
                                        ) {
                                            Text(strings().settings.path.close())
                                        }
                                    }
                                )
                            } catch(e: IOException) {
                                popupContent = PopupData(
                                    type = PopupType.ERROR,
                                    titleRow = { Text(strings().settings.path.errorTitle()) },
                                    content = { Text(strings().settings.path.errorMessage(e)) },
                                    buttonRow = {
                                        Button(
                                            onClick = { popupContent = null }
                                        ) {
                                            Text(strings().settings.path.close())
                                        }
                                    }
                                )
                            }
                        }.start()
                    }
                }
            ) {
                Text(
                    strings().settings.path.apply()
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                strings().settings.sync.title(),
                style = MaterialTheme.typography.titleSmall,
            )

            var tfUrl by remember { mutableStateOf(appSettings().syncUrl ?: "") }
            var tfPort by remember { mutableStateOf(appSettings().syncPort ?: "") }
            var tfKey by remember { mutableStateOf(appSettings().syncKey ?: "") }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "http://"
                )
                TextBox(
                    tfUrl,
                    {
                        tfUrl = it
                    },
                    placeholder = strings().settings.sync.url()
                )
                Text(":")
                TextBox(
                    tfPort,
                    {
                        tfPort = it
                    },
                    placeholder = strings().settings.sync.port()
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    strings().settings.sync.key()
                )
                TextBox(
                    tfKey,
                    {
                        tfKey = it
                    },
                    placeholder = strings().settings.sync.keyPlaceholder()
                )
            }

            Button(
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
                    } catch(e: Exception) {
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
                }
            ) {
                Text(
                    strings().settings.sync.test()
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                strings().settings.user()
            )
            Text(
                userAuth().minecraftUser?.name ?: "UNKNOWN",
                style = MaterialTheme.typography.titleMedium
            )
            userImage?.let {
                Image(
                    it.toComposeImageBitmap(),
                    contentDescription = "Profile Image",
                    contentScale = FixedScale(LocalDensity.current.density * 8f),
                    filterQuality = FilterQuality.None,
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                )
            }
            Text(
                userAuth().minecraftUser?.uuid ?: "UNKNOWN UUID",
                style = MaterialTheme.typography.labelSmall
            )
            IconButton(
                onClick = loginContext.logout,
                interactionTint = MaterialTheme.colorScheme.error,
                highlighted = true
            ) {
                Icon(
                    icons().logout,
                    "Logout",
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                strings().launcher.name(),
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                strings().settings.version()
            )

            if(updater().getUpdate().latest == false) {
                IconButton(
                    onClick = {
                        //TODO: Update
                    },
                    highlighted = true
                ) {
                    Icon(
                        icons().update,
                        "Download Update"
                    )
                }
                Text(
                    strings().settings.update.available()
                )
            }

            IconButton(
                onClick = {
                    UrlString.of("https://github.com/Tre5et/treelauncher").openInBrowser()
                }
            ) {
                Icon(
                    icons().gitHub(),
                    "Link to Github Project",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    popupContent?.let {
        PopupOverlay(it)
    }
}