package net.treset.treelauncher.backend.sync

import net.treset.mc_version_loader.json.SerializationException
import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.mc_version_loader.launcher.LauncherManifestType
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.config.appSettings
import net.treset.treelauncher.backend.util.HttpService
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.backend.util.string.UrlString
import java.io.IOException

class SyncService @JvmOverloads constructor(
    syncUrl: String = appSettings().syncUrl?: throw IllegalStateException("Sync url is null"),
    syncPort: String = appSettings().syncPort?: throw IllegalStateException("Sync port is null"),
    syncKey: String = appSettings().syncKey?: throw IllegalStateException("Sync key is null")
) : HttpService(
    "http://$syncUrl:$syncPort", arrayOf(Pair("Auth-Key", syncKey))
) {
    @Throws(IOException::class)
    fun testConnection() {
        get("test")
    }

    @Throws(IOException::class)
    fun getAvailable(type: String?): ComponentList {
        val result: Pair<HttpStatusCode, ByteArray> = get("list", type!!)
        val response: ComponentList = try {
            ComponentList.fromJson(String(result.second))
        } catch (e: Exception) {
            throw IOException("Failed to parse the response from the server.\nError: $e")
        }
        return response
    }

    @Throws(IOException::class)
    fun newComponent(type: String, id: String) {
        get("new", type, id)
    }

    @Throws(IOException::class)
    fun uploadFile(type: String, id: String, path: String, content: ByteArray) {
        post(content, "file", type, id, UrlString.encoded(path))
    }

    @Throws(IOException::class)
    fun complete(type: String, id: String): Int {
        val result: Pair<HttpStatusCode, ByteArray> = get("complete", type, id)
        return String(result.second).toInt()
    }

    @Throws(IOException::class)
    operator fun get(type: String, id: String, version: Int): GetResponse {
        val result: Pair<HttpStatusCode, ByteArray> = get("get", type, id, version)
        return try {
            GetResponse.fromJson(String(result.second))
        } catch (e: SerializationException) {
            throw IOException("Failed to parse the response from the server.\nError: $e")
        }
    }

    @Throws(IOException::class)
    fun downloadFile(type: String, id: String, path: String): ByteArray {
        val result: Pair<HttpStatusCode, ByteArray> = get("file", type, id, UrlString.encoded(path))
        return if (result.first === HttpStatusCode.NO_CONTENT) byteArrayOf() else result.second
    }

    companion object {
        fun isSyncing(manifest: LauncherManifest): Boolean {
            return LauncherFile.of(manifest.directory, appConfig().SYNC_FILENAME).isFile()
        }

        @Throws(IOException::class)
        fun convertType(type: LauncherManifestType): String {
            return when (type) {
                LauncherManifestType.RESOURCEPACKS_COMPONENT -> "resourcepacks"
                LauncherManifestType.MODS_COMPONENT -> "mods"
                LauncherManifestType.OPTIONS_COMPONENT -> "options"
                LauncherManifestType.SAVES_COMPONENT -> "saves"
                LauncherManifestType.INSTANCE_COMPONENT -> "instance"
                else -> throw IOException("Invalid type: $type")
            }
        }
    }
}
