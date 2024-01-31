package net.treset.treelauncher.backend.update

import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.util.HttpService
import net.treset.treelauncher.localization.language
import net.treset.treelauncher.localization.strings
import java.io.IOException

class UpdateService : HttpService(appConfig().updateUrl) {
    @Throws(IOException::class)
    fun update(): Update {
        val result: Pair<HttpStatusCode, ByteArray> = get(
            "update",
            strings().launcher.version(),
            language().appLanguage.locale
        )
        val response = String(result.second)
        return try {
            Update.fromJson(response)
        } catch (e: Exception) {
            throw IOException("Failed to parse the response from the server.\nError: $e")
        }
    }

    @Throws(IOException::class)
    fun file(version: String, file: String): ByteArray {
        val result: Pair<HttpStatusCode, ByteArray> = get("file", version, file)
        return result.second
    }
}
