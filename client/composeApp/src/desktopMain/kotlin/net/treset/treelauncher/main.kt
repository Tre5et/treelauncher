package net.treset.treelauncher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.multiplatform.webview.web.Cef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.typography
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.intui.standalone.theme.darkThemeDefinition
import org.jetbrains.jewel.intui.window.decoratedWindow
import org.jetbrains.jewel.intui.window.styling.dark
import org.jetbrains.jewel.ui.ComponentStyling
import org.jetbrains.jewel.window.DecoratedWindow
import org.jetbrains.jewel.window.TitleBar
import org.jetbrains.jewel.window.newFullscreenControls
import org.jetbrains.jewel.window.styling.TitleBarColors
import org.jetbrains.jewel.window.styling.TitleBarStyle
import java.awt.Toolkit
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt


fun main() = application {

    IntUiTheme(
        theme = JewelTheme.darkThemeDefinition(),
        styling = ComponentStyling.decoratedWindow(
            titleBarStyle = TitleBarStyle.dark(
                    colors = TitleBarColors.dark(
                        backgroundColor = Color(red = 28, green = 27, blue = 31),
                        inactiveBackground = Color(red = 28, green = 27, blue = 31),
                        borderColor = Color(0xFF43454A)
                    )
                )
        )
    ) {
        DecoratedWindow(
            onCloseRequest = { if(onClose()) exitApplication() },
            title = strings().launcher.name(),
            state = rememberWindowState(
                position = WindowPosition(Alignment.Center),
                size = DpSize(min(1600.dp, Toolkit.getDefaultToolkit().screenSize.width.dp - 100.dp), min(900.dp, Toolkit.getDefaultToolkit().screenSize.height.dp - 100.dp))
            ),
            icon = BitmapPainter(useResource("icon.png", ::loadImageBitmap)),
        ) {
            TitleBar(Modifier.newFullscreenControls()) {
                CompositionLocalProvider(
                    LocalContentColor provides Color.White
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            strings().launcher.name(),
                            style = typography().titleSmall
                        )
                    }
                }
            }

            var restartRequired by remember { mutableStateOf(false) }
            var downloading by remember { mutableStateOf(0F) }
            var initialized by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    Cef.init(builder = {
                        installDir = File("jcef", "bundle")
                    }, initProgress = {
                        onDownloading {
                            downloading = max(it, 0F)
                        }
                        onInitialized {
                            initialized = true
                        }
                    }, onError = {
                        it.printStackTrace()
                    }, onRestartRequired = {
                        restartRequired = true
                    })
                }
            }

            if (restartRequired) {
                Text(text = strings().launcher.status.restartRequired())
            } else {
                if (initialized) {
                    App()
                } else if(downloading > 0) {
                    Text(text = strings().launcher.status.preparing(downloading.roundToInt()))
                }
            }

            DisposableEffect(Unit) {
                onDispose {
                    Cef.dispose()
                }
            }
        }
    }
}