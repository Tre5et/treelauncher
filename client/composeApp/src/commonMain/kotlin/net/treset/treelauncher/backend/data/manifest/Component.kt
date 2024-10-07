package net.treset.treelauncher.backend.data.manifest

import net.treset.mcdl.format.FormatUtils
import net.treset.mcdl.json.GenericJsonParsable
import net.treset.mcdl.json.SerializationException
import net.treset.treelauncher.backend.launching.resources.ComponentResourceProvider
import net.treset.treelauncher.backend.launching.resources.GenericResourceProvider
import net.treset.treelauncher.backend.util.file.LauncherFile
import java.io.IOException
import java.time.LocalDateTime

open class Component(
    val type: LauncherManifestType,
    var id: String,
    var name: String,
    var includedFiles: Array<String>,
    var lastUsed: String = "",
    var active: Boolean = false,
    @Transient var file: LauncherFile
): GenericJsonParsable() {
    var lastUsedTime: LocalDateTime
        get() = try {
            FormatUtils.parseLocalDateTime(lastUsed)
        } catch (e: IllegalArgumentException) {
            LocalDateTime.MIN
        }
        set(lastPlayed) {
            lastUsed = FormatUtils.formatLocalDateTime(lastPlayed)
        }

    val directory: LauncherFile
        get() = LauncherFile.of(file.parentFile)

    open fun getResourceProvider(gameDataDir: LauncherFile): ComponentResourceProvider<out Component> {
        return GenericResourceProvider(this, gameDataDir)
    }

    open fun copyTo(other: Component) {
        copyData(other)
        other.name = name
        other.id = id
    }

    open fun copyData(other: Component) {
        other.includedFiles = includedFiles
        other.lastUsed = lastUsed
        other.active = active
    }

    @Throws(IOException::class)
    open fun delete(parent: ParentManifest) {
        parent.components.remove(id)
        parent.write()
        directory.remove()
    }

    @Throws(IOException::class)
    fun write() {
        file.write(toJson())
    }

    fun getContentDirectory(gameDataDir: LauncherFile): LauncherFile {
        return if(active) {
            gameDataDir
        } else {
            directory
        }
    }

    companion object {
        @Throws(SerializationException::class)
        inline fun <reified T: Component> fromJson(
            json: String?,
            expectedType: LauncherManifestType
        ): T {
            val componentManifest = fromJson(json, T::class.java)
            if(componentManifest.type != expectedType) {
                throw SerializationException("Expected type $expectedType, got ${componentManifest.type}")
            }
            return componentManifest
        }

        @Throws(IOException::class)
        inline fun <reified T: Component> readFile(
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