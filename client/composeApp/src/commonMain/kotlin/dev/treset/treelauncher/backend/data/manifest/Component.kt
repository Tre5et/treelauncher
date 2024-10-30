package dev.treset.treelauncher.backend.data.manifest

import dev.treset.mcdl.format.FormatUtils
import dev.treset.treelauncher.backend.launching.resources.ComponentResourceProvider
import dev.treset.treelauncher.backend.launching.resources.GenericResourceProvider
import dev.treset.treelauncher.backend.util.copyTo
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.backend.util.serialization.MutableDataState
import dev.treset.treelauncher.backend.util.serialization.MutableDataStateList
import dev.treset.treelauncher.util.ListDisplay
import kotlinx.serialization.Serializable
import java.io.IOException
import java.time.LocalDateTime

@Serializable
sealed class Component: Manifest() {
    abstract val id: MutableDataState<String>
    abstract val name: MutableDataState<String>
    abstract val includedFiles: MutableDataStateList<String>
    abstract val lastUsed: MutableDataState<String>
    abstract val active: MutableDataState<Boolean>
    abstract val listDisplay: MutableDataState<ListDisplay?>

    var lastUsedTime: LocalDateTime
        get() = try {
            FormatUtils.parseLocalDateTime(lastUsed.value)
        } catch (e: IllegalArgumentException) {
            LocalDateTime.MIN
        }
        set(lastPlayed) {
            lastUsed.value = FormatUtils.formatLocalDateTime(lastPlayed)
        }

    open fun getResourceProvider(gameDataDir: LauncherFile): ComponentResourceProvider<out Component> {
        return GenericResourceProvider(this, gameDataDir)
    }

    open fun copyTo(other: Component) {
        copyData(other)
        other.name.value = name.value
        other.id.value = id.value
        other.file.value = file.value
    }

    open fun copyData(other: Component) {
        includedFiles.copyTo(other.includedFiles)
        other.lastUsed.value = lastUsed.value
        other.active.value = active.value
    }

    @Throws(IOException::class)
    open fun delete(parent: ParentManifest) {
        parent.components.remove(id.value)
        parent.write()
        directory.remove()
    }

    fun getContentDirectory(gameDataDir: LauncherFile): LauncherFile {
        return if(active.value) {
            gameDataDir
        } else {
            directory
        }
    }
}