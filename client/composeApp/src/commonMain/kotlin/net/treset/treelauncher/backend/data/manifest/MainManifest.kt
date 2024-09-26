package net.treset.treelauncher.backend.data.manifest

import net.treset.mcdl.json.GenericJsonParsable
import net.treset.mcdl.json.SerializationException
import net.treset.treelauncher.backend.util.file.LauncherFile
import java.io.IOException

class MainManifest(
    val type: LauncherManifestType,
    var activeInstance: String?,
    var assetsDir: String,
    var gameDataDir: String,
    var instancesDir: String,
    var javasDir: String,
    var librariesDir: String,
    var modsDir: String,
    var optionsDir: String,
    var resourcepacksDir: String,
    var savesDir: String,
    var versionDir: String,
    @Transient var file: LauncherFile
): GenericJsonParsable() {
    val directory: LauncherFile
        get() = LauncherFile.of(file.parentFile)

    @Throws(IOException::class)
    fun write() {
        file.write(this)
    }

    companion object {
        @Throws(SerializationException::class)
        fun fromJson(
            json: String
        ): MainManifest {
            val mainManifest = fromJson(json, MainManifest::class.java)
            if(mainManifest.type != LauncherManifestType.LAUNCHER) {
                throw SerializationException("Expected type ${LauncherManifestType.LAUNCHER}, got ${mainManifest.type}")
            }
            return mainManifest
        }

        @Throws(IOException::class)
        fun readFile(
            file: LauncherFile
        ): MainManifest {
            val json = file.readString()
            val manifest = try {
                fromJson(json)
            } catch (e: SerializationException) {
                throw IOException("Failed to parse component manifest: $file", e)
            }
            manifest.file = file
            return manifest
        }
    }
}