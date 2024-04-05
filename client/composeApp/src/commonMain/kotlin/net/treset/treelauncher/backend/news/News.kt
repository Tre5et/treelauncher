package net.treset.treelauncher.backend.news

import net.treset.mc_version_loader.json.GenericJsonParsable
import net.treset.mc_version_loader.json.SerializationException
import net.treset.treelauncher.backend.config.appSettings
import java.io.IOException

class News(
    var important: List<NewsElement>?,
    var other: List<NewsElement>?
) : GenericJsonParsable() {
    class NewsElement(
        var title: String,
        var content: String?,
        var id: String
    )

    companion object {
        @Throws(SerializationException::class)
        fun fromJson(json: String?): News {
            return fromJson(json, News::class.java)
        }
    }
}

private var news: News? = null

@Throws(IOException::class)
fun news(): News {
    val newsService = NewsService(appSettings().updateUrl)
    return newsService.news().also {
        news = it
    }
}
