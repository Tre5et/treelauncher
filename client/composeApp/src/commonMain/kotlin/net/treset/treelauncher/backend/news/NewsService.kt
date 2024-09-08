package net.treset.treelauncher.backend.news

import net.treset.mcdl.json.SerializationException
import net.treset.treelauncher.backend.util.HttpService
import net.treset.treelauncher.localization.language
import net.treset.treelauncher.localization.strings
import java.io.IOException

class NewsService(url: String) : HttpService(url) {
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
