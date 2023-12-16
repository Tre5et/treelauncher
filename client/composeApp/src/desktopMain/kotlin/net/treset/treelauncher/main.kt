package net.treset.treelauncher

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import net.treset.treelauncher.localization.strings

fun main() = application {
    Window(
        onCloseRequest = { if(onClose()) exitApplication() },
        title = strings().launcher.name()
    ) {
        App()
    }
}