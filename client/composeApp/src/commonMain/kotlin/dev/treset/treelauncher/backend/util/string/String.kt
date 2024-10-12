package dev.treset.treelauncher.backend.util.string

import io.github.oshai.kotlinlogging.KotlinLogging
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.net.URL


fun String.openInBrowser() {
    val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
        try {
            desktop.browse(URL(this).toURI())
        } catch (e: Exception) {
            LOGGER.warn(e) { "Unable to open URL in Browser" }
        }
    }
}

fun String.copyToClipboard() {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    clipboard.setContents(StringSelection(this), null)
}

private val LOGGER = KotlinLogging.logger {}