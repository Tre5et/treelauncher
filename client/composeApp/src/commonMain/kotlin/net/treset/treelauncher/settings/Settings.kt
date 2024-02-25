package net.treset.treelauncher.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.darkrockstudios.libraries.mpfilepicker.DirectoryPicker
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.app
import net.treset.treelauncher.backend.auth.userAuth
import net.treset.treelauncher.backend.config.GlobalConfigLoader
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.config.appSettings
import net.treset.treelauncher.backend.sync.SyncService
import net.treset.treelauncher.backend.update.updater
import net.treset.treelauncher.backend.util.exception.FileLoadException
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.backend.util.string.openInBrowser
import net.treset.treelauncher.generic.*
import net.treset.treelauncher.localization.Language
import net.treset.treelauncher.localization.language
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.login.LoginContext
import net.treset.treelauncher.style.*
import net.treset.treelauncher.util.onUpdate
import java.awt.image.BufferedImage
import java.io.IOException


@Composable
fun Settings(
    appContext: AppContext,
    loginContext: LoginContext
) {
    val coroutineScope = rememberCoroutineScope()

    var userImage: BufferedImage? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        userImage = userAuth().getUserIcon()
    }

    var language by remember { mutableStateOf(language().appLanguage) }

    var theme by remember { mutableStateOf(theme()) }

    var showCleanup by remember { mutableStateOf(false) }

    var popupContent: PopupData? by remember { mutableStateOf(null) }

    val update = remember {
        try {
            updater().getUpdate()
        } catch(e: IOException) {
            app().error(e)
            null
        }
    }

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
                selected = language
            )

            var restart by remember { mutableStateOf(false) }
            TitledComboBox(
                strings().settings.theme(),
                items = Theme.entries,
                onSelected = {
                    theme = it
                    setTheme(it)
                    restart = true
                },
                selected = theme
            )

            if(restart) {
                Text(
                    strings().settings.restartRequired(),
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

            var tfValue by remember { mutableStateOf(appConfig().baseDir.absolutePath) }
            var showDirPicker by remember { mutableStateOf(false) }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextBox(
                    tfValue,
                    {
                        tfValue = it
                    }
                )

                IconButton(
                    onClick = {
                        showDirPicker = true
                    },
                    tooltip = strings().settings.path.select()
                ) {
                    Icon(
                        icons().folder,
                        "Choose Folder"
                    )
                }
            }

            DirectoryPicker(
                show = showDirPicker,
                initialDirectory = if(LauncherFile.of(tfValue).isDirectory()) tfValue else appConfig().baseDir.absolutePath,
                onFileSelected = {
                    it?.let { tfValue = it }
                    showDirPicker = false
                },
            )

            var cbState by remember { mutableStateOf(true) }
            TitledCheckBox(
                title = strings().settings.path.remove(),
                checked = cbState,
                onCheckedChange = {
                    cbState = it
                }
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

        Button(
            onClick = {
                showCleanup = true
            },
            color = MaterialTheme.colorScheme.inversePrimary
        ) {
            Text(
                strings().settings.cleanup.button()
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
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
                },
                enabled = false
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
                highlighted = true,
                tooltip = strings().settings.logout()
            ) {
                Icon(
                    icons().logout,
                    "Logout",
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
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
            }


            IconButton(
                onClick = {
                    onUpdate(coroutineScope) { popupContent = it }
                },
                highlighted = update?.latest == false,
                tooltip = strings().settings.update.tooltip()
            ) {
                Icon(
                    icons().update,
                    "Download Update"
                )
            }

            if(update?.latest == false) {
                Text(
                    strings().settings.update.available()
                )
            }

            IconButton(
                onClick = {
                    "https://github.com/Tre5et/treelauncher".openInBrowser()
                },
                tooltip = strings().settings.sourceTooltip()
            ) {
                Icon(
                    icons().gitHub,
                    "Link to Github Project",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    if(showCleanup) {
        var includeLibraries by remember { mutableStateOf(true) }
        var state: Int by remember { mutableStateOf(0) }

        when(state) {
            0 -> PopupOverlay(
                titleRow = { Text(strings().settings.cleanup.title()) },
                content = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            strings().settings.cleanup.message(),
                        )

                        TitledCheckBox(
                            title = strings().settings.cleanup.libraries(),
                            checked = includeLibraries,
                            onCheckedChange = {
                                includeLibraries = it
                            }
                        )
                    }
                },
                buttonRow = {
                    Button(
                        onClick = {
                            showCleanup = false
                        },
                        color = MaterialTheme.colorScheme.error
                    ) {
                        Text(strings().settings.cleanup.cancel())
                    }
                    Button(
                        onClick = {
                            Thread {
                                try {
                                    appContext.files.cleanupVersions(includeLibraries)
                                    state = 2
                                } catch(e: FileLoadException) {
                                    app().error(e)
                                    state = 3
                                }
                            }.start()
                            state = 1
                        }
                    ) {
                        Text(strings().settings.cleanup.confirm())
                    }
                }
            )
            1 -> PopupOverlay(
                titleRow = { Text(strings().settings.cleanup.deleting()) }
            )
            2 -> PopupOverlay(
                type = PopupType.SUCCESS,
                titleRow = { Text(strings().settings.cleanup.success()) },
                buttonRow = {
                    Button(
                        onClick = {
                            showCleanup = false
                        }
                    ) {
                        Text(strings().settings.cleanup.close())
                    }
                }
            )
            3 -> PopupOverlay(
                type = PopupType.ERROR,
                titleRow = { Text(strings().settings.cleanup.failureTitle()) },
                content = { Text(strings().settings.cleanup.failureMessage()) },
                buttonRow = {
                    Button(
                        onClick = {
                            showCleanup = false
                        }
                    ) {
                        Text(strings().settings.cleanup.close())
                    }
                }
            )
        }
    }

    popupContent?.let {
        PopupOverlay(it)
    }
}

