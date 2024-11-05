package dev.treset.treelauncher.backend.update

import dev.treset.treelauncher.backend.config.AppSettings
import dev.treset.treelauncher.backend.config.appConfig
import dev.treset.treelauncher.backend.util.HttpService
import dev.treset.treelauncher.localization.Strings
import java.io.IOException

class UpdateService(url: String) : HttpService(url) {
    @Throws(IOException::class)
    fun update(): Update {
        val result: Pair<HttpStatusCode, ByteArray> = get(
            "update",
            appConfig().launcherVersion.toString(),
            AppSettings.language.value.locale
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
