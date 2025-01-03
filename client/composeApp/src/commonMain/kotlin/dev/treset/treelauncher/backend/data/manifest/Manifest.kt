package dev.treset.treelauncher.backend.data.manifest

import androidx.compose.runtime.MutableState
import dev.treset.treelauncher.backend.util.file.LauncherFile
import kotlinx.serialization.Serializable
import java.io.IOException

@Serializable
sealed class Manifest {
    abstract val file: MutableState<LauncherFile>
    abstract var expectedType: LauncherManifestType
    abstract val type: LauncherManifestType

    val directory: LauncherFile
        get() = file.value.parentFile ?: LauncherFile.of("")

    @Throws(IOException::class)
    open fun write() {
        file.value.write(this)
    }

    companion object {
        @Throws(IOException::class)
        inline fun <reified T: Manifest> readFile(file: LauncherFile, expectedType: LauncherManifestType?): T {
            val content = file.readData<T>()
            content.file.value = file
            expectedType?.let {
                content.expectedType = it
            }
            if (content.type != content.expectedType) {
                throw IOException("Expected type $expectedType, but got ${content.type}")
            }
            return content
        }

        @Throws(IOException::class)
        inline fun <reified T: Manifest> readFile(file: LauncherFile): T {
            return readFile(file, null)
        }
    }
}
