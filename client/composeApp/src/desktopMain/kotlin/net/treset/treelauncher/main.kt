package net.treset.treelauncher

import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.multiplatform.webview.web.Cef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.treset.treelauncher.localization.strings
import java.awt.Toolkit
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

fun main() = application {
    Window(
        onCloseRequest = { if(onClose()) exitApplication() },
        title = strings().launcher.name(),
        state = rememberWindowState(
            position = WindowPosition(Alignment.Center),
            size = DpSize(min(1600.dp, Toolkit.getDefaultToolkit().screenSize.width.dp - 100.dp), min(900.dp, Toolkit.getDefaultToolkit().screenSize.height.dp - 100.dp))
        )
    ) {
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