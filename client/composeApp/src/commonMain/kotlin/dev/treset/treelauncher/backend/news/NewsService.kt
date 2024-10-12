package dev.treset.treelauncher.backend.news

import dev.treset.mcdl.json.SerializationException
import dev.treset.treelauncher.backend.util.HttpService
import dev.treset.treelauncher.localization.language
import dev.treset.treelauncher.localization.strings
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
