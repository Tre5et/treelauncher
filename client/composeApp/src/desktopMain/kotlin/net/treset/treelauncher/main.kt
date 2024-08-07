package net.treset.treelauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.datlag.kcef.KCEF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.treset.mc_version_loader.util.OsUtil
import net.treset.treelauncher.backend.config.appSettings
import net.treset.treelauncher.generic.IconButton
import net.treset.treelauncher.generic.Text
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.*
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.intui.standalone.theme.darkThemeDefinition
import org.jetbrains.jewel.intui.standalone.theme.lightThemeDefinition
import org.jetbrains.jewel.intui.window.decoratedWindow
import org.jetbrains.jewel.ui.ComponentStyling
import org.jetbrains.jewel.window.DecoratedWindow
import org.jetbrains.jewel.window.TitleBar
import org.jetbrains.jewel.window.newFullscreenControls
import java.awt.Toolkit
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt


fun main() = application {
    var theme by remember { mutableStateOf(Theme.SYSTEM) }

    val app = remember {
        LauncherApp(
            ::exitApplication
        ) {
            theme = it
        }
    }

    val titleColors = if(theme.isDark()) JewelTheme.darkThemeDefinition() else JewelTheme.lightThemeDefinition()
    val styling = ComponentStyling.decoratedWindow(
        titleBarStyle = if(theme.isDark()) darkTitleBar() else lightTitleBar()
    )
    val colors = if(theme.isDark()) darkColors() else lightColors()

    IntUiTheme(
        theme = titleColors,
        styling = styling
    ) {
        DecoratedWindow(
            onCloseRequest = { app().exit() },
            title = strings().launcher.name(),
            state = rememberWindowState(
                position = WindowPosition(Alignment.Center),
                size = DpSize(min(1600.dp, Toolkit.getDefaultToolkit().screenSize.width.dp - 100.dp), min(900.dp, Toolkit.getDefaultToolkit().screenSize.height.dp - 100.dp))
            ),
            icon = BitmapPainter(useResource("icon_default.png", ::loadImageBitmap)),
        ) {
            TitleBar(Modifier.newFullscreenControls()) {
                CompositionLocalProvider(
                    LocalContentColor provides colors.material.onBackground
                ) {
                    Box(
                        modifier = Modifier.offset(x = (-9).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            strings().launcher.name(),
                            style = typography().titleSmall,
                            modifier = Modifier.offset(y = 2.dp)
                        )

                        IconButton(
                            onClick = {
                                AppContext.openNews()
                            },
                            icon = icons().news,
                            tooltip = strings().news.tooltip(),
                            modifier = Modifier
                                .offset(x = 82.dp, y = 1.dp)
                        )
                    }
                }
            }

            var restartRequired by remember { mutableStateOf(false) }
            var downloading by remember { mutableStateOf(0F) }
            var initialized by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    KCEF.init(
                        builder = {
                            installDir(File("kcef", "bundle"))
                            progress {
                                onDownloading {
                                    downloading = max(it, 0F)
                                }
                                onInitialized {
                                    initialized = true
                                }
                            }
                        }, onError = {
                            it?.printStackTrace()
                        }, onRestartRequired = {
                            restartRequired = true
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.material.background)
            ) {
                if (restartRequired) {
                    Text(
                        text = strings().launcher.status.restartRequired(),
                        color = colors.material.onBackground
                    )
                } else if(downloading > 0) {
                    Text(
                        text = strings().launcher.status.preparing(downloading.roundToInt()),
                        color = colors.material.onBackground
                    )
                } else {
                    App(app)
                }
            }

            DisposableEffect(Unit) {
                onDispose {
                    KCEF.disposeBlocking()
                }
            }
        }
    }
}

actual fun getUpdaterProcess(updaterArgs: String): ProcessBuilder {
    val updaterFile = File("resources/windows/updater.exe").let {
        if(it.isFile) it else File("app/resources/updater")
    }

    val commandBuilder = StringBuilder()
    commandBuilder.append(updaterFile.absolutePath)
    return if(OsUtil.isOsName("windows")) {
        ProcessBuilder("cmd.exe", "/c", "start", "cmd", if(appSettings().isDebug) "/k" else "/c",
            "$commandBuilder $updaterArgs"
        )
    } else {
        //TODO
        ProcessBuilder("UNIMPLEMENTED")
    }
}