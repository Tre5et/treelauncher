package net.treset.treelauncher.backend.util.string

import java.awt.Desktop
import java.io.IOException
import java.net.URISyntaxException
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class UrlString(val original: String, private val operation: UrlOperation) : FormatString() {
    enum class UrlOperation {
        ENCODE,
        DECODE,
        NONE
    }

    @Throws(FormatException::class)
    override fun get(): String {
        return when (operation) {
            UrlOperation.ENCODE -> URLEncoder.encode(original, StandardCharsets.UTF_8)
            UrlOperation.DECODE -> try {
                    URLDecoder.decode(original, StandardCharsets.UTF_8)
                } catch (e: IllegalArgumentException) {
                    throw FormatException("Failed to decode URL string: $original", e)
                }
            else -> original
        }
    }

    companion object {
        fun of(original: String): UrlString {
            return UrlString(original, UrlOperation.NONE)
        }

        fun encoded(original: String): UrlString {
            return UrlString(original, UrlOperation.ENCODE)
        }

        fun decoded(original: String): UrlString {
            return UrlString(original, UrlOperation.DECODE)
        }
    }
}

@Throws(IOException::class, URISyntaxException::class)
fun String.openInBrowser() {
    val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
        desktop.browse(URL(this).toURI())
    }
}
