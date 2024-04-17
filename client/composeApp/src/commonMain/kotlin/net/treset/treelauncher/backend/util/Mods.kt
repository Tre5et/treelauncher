package net.treset.treelauncher.backend.util

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toPainter
import net.treset.mc_version_loader.launcher.LauncherMod
import net.treset.mc_version_loader.mods.ModData
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import javax.imageio.ImageIO

fun LauncherMod.isSame(other: ModData): Boolean =
    this.name == other.name
    || this.downloads.any {thisD ->
        other.projectIds.any {
            thisD.id == it
        }
    }

enum class ModProviderStatus {
    CURRENT,
    AVAILABLE,
    UNAVAILABLE
}

@Throws(IOException::class)
fun loadNetworkImage(link: String): Painter? {
    try {
        val url = URL(link)
        val connection = url.openConnection() as HttpURLConnection
        connection.connect()

        val inputStream = connection.inputStream
        val bufferedImage = ImageIO.read(inputStream)

        return bufferedImage?.toPainter()
    } catch(e: MalformedURLException) {
        throw IOException(e)
    }
}