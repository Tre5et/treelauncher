package net.treset.treelauncher.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.auth.userAuth
import net.treset.treelauncher.backend.config.GlobalConfigLoader
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.config.appSettings
import net.treset.treelauncher.backend.update.updater
import net.treset.treelauncher.backend.util.exception.FileLoadException
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.backend.util.string.openInBrowser
import net.treset.treelauncher.generic.*
import net.treset.treelauncher.localization.Language
import net.treset.treelauncher.localization.language
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.login.LoginContext
import net.treset.treelauncher.style.AccentColor
import net.treset.treelauncher.style.Theme
import net.treset.treelauncher.style.icons
import net.treset.treelauncher.util.onUpdate
import java.awt.image.BufferedImage
import java.io.IOException


@Composable
fun Settings() {
    val coroutineScope = rememberCoroutineScope()

    var userImage: BufferedImage? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        userImage = userAuth().getUserIcon()
    }

    var language by remember { mutableStateOf(language().appLanguage) }

    var theme by remember { mutableStateOf(appSettings().theme) }
    var accentColor by remember { mutableStateOf(appSettings().accentColor) }
    var customColor by remember { mutableStateOf(appSettings().customColor) }
    var showColorPicker by remember { mutableStateOf(false) }

    var showCleanup by remember { mutableStateOf(false) }

    var popupContent: PopupData? by remember { mutableStateOf(null) }

    val update = remember {
        try {
            updater().getUpdate()
        } catch(e: IOException) {
            AppContext.error(e)
            null
        }
    }

    TitledColumn(
        title = strings().settings.title(),
        modifier = Modifier.padding(top = 12.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(IntrinsicSize.Min)
        ) {
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
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(12.dp)
                    .fillMaxWidth()
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
                        icon = icons().folder,
                        tooltip = strings().settings.path.select()
                    )
                }

                DirectoryPicker(
                    show = showDirPicker,
                    initialDirectory = if (LauncherFile.of(tfValue)
                            .isDirectory()
                    ) tfValue else appConfig().baseDir.absolutePath,
                    onFileSelected = {
                        it?.let { tfValue = it }
                        showDirPicker = false
                    },
                )

                var cbState by remember { mutableStateOf(false) }
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

                        if (!dir.isDirectory()) {
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
                                        titleRow = { Text(strings().settings.path.success()) },
                                        buttonRow = {
                                            Button(
                                                onClick = { popupContent = null }
                                            ) {
                                                Text(strings().settings.path.close())
                                            }
                                        }
                                    )
                                } catch (e: IOException) {
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

            /*Column(
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
                }
            }*/

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(12.dp)
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
                    onClick = LoginContext.logout,
                    icon = icons().logout,
                    size = 32.dp,
                    interactionTint = MaterialTheme.colorScheme.error,
                    highlighted = true,
                    tooltip = strings().settings.logout()
                )
            }

            Button(
                onClick = {
                    showCleanup = true
                }
            ) {
                Text(
                    strings().settings.cleanup.button()
                )
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
                    Text(
                        strings().settings.version()
                    )
                }


                IconButton(
                    onClick = {
                        onUpdate(coroutineScope) { popupContent = it }
                    },
                    icon = icons().update,
                    highlighted = update?.id != null,
                    tooltip = strings().settings.update.tooltip()
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
        }
    }

    if(showColorPicker) {
        var color by remember { mutableStateOf(HsvColor.from(customColor)) }

        PopupOverlay(
            titleRow = { Text(strings().settings.theme.title()) },
            content = {
                ClassicColorPicker(
                    onColorChanged = {
                        color = it
                    },
                    color = color,
                    showAlphaBar = false,
                    modifier = Modifier.size(300.dp)
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
                                    AppContext.files.cleanupVersions(includeLibraries)
                                    state = 2
                                } catch(e: FileLoadException) {
                                    AppContext.error(e)
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

