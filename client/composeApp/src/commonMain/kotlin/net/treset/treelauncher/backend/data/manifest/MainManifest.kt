package net.treset.treelauncher.backend.data.manifest

import dev.treset.mcdl.json.SerializationException
import net.treset.treelauncher.backend.util.file.LauncherFile
import java.io.IOException

class MainManifest(
    var activeInstance: String?,
    var assetsDir: String,
    var librariesDir: String,
    var gameDataDir: String,
    var instancesDir: String,
    var savesDir: String,
    var resourcepacksDir: String,
    var optionsDir: String,
    var modsDir: String,
    var versionDir: String,
    var javasDir: String,
    val type: LauncherManifestType = LauncherManifestType.LAUNCHER,
    @Transient var file: LauncherFile
): Manifest() {
    val directory: LauncherFile
        get() = LauncherFile.of(file.parentFile)

    @Throws(IOException::class)
    override fun write() {
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