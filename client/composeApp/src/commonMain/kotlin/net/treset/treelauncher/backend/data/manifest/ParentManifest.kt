package net.treset.treelauncher.backend.data.manifest

import dev.treset.mcdl.json.SerializationException
import net.treset.treelauncher.backend.util.file.LauncherFile
import java.io.IOException

open class ParentManifest(
    val type: LauncherManifestType,
    var prefix: String,
    var components: MutableList<String>,
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
        inline fun <reified T: ParentManifest> fromJson(
            json: String?,
            expectedType: LauncherManifestType
        ): T {
            val parentManifest = fromJson(json, T::class.java)
            if(parentManifest.type != expectedType) {
                throw SerializationException("Expected type $expectedType, got ${parentManifest.type}")
            }
            return parentManifest
        }

        @Throws(IOException::class)
        inline fun <reified T: ParentManifest> readFile(
            file: LauncherFile,
            expectedType: LauncherManifestType
        ): T {
            val json = file.readString()
            val manifest = try {
                fromJson<T>(json, expectedType)
            } catch (e: SerializationException) {
                throw IOException("Failed to parse component manifest: $file", e)
            }
            manifest.file = file
            return manifest
        }
    }
}