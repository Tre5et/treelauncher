package net.treset.treelauncher.backend.news

import net.treset.mc_version_loader.json.SerializationException
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.util.HttpService
import net.treset.treelauncher.localization.language
import net.treset.treelauncher.localization.strings
import java.io.IOException

class NewsService : HttpService(appConfig().updateUrl) {
    @Throws(IOException::class)
    fun news(): News {
        val result: Pair<HttpStatusCode, ByteArray> = get(
            "news",
            strings().launcher.version(),
            language().appLanguage.locale
        )
        val response = String(result.second)
        return try {
            News.fromJson(response)
        } catch (e: SerializationException) {
            throw IOException("Failed to parse the response from the server.\nError: $e")
        }
    }
}
