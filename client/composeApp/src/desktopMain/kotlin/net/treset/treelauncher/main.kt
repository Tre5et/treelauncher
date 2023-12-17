package net.treset.treelauncher

import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.datlag.kcef.KCEF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.treset.treelauncher.localization.strings
import java.io.File
import kotlin.math.roundToInt

fun main() = application {
    Window(
        onCloseRequest = { if(onClose()) exitApplication() },
        title = strings().launcher.name()
    ) {
        var restartRequired by remember { mutableStateOf(false) }
        var downloading by remember { mutableStateOf(0F) }
        var initialized by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                KCEF.init(builder = {
                    installDir(File("kcef-bundle"))
                    progress {
                        onDownloading {
                            downloading = it
                        }
                        onInitialized {
                            initialized = true
                        }
                    }
                }, onError = {
                    it?.printStackTrace()
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
                KCEF.disposeBlocking()
            }
        }
    }
}

actual fun getPlatformName() = "Desktop"