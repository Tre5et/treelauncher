package dev.treset.treelauncher.backend.data.manifest

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import dev.treset.mcdl.resourcepacks.Resourcepack
import dev.treset.mcdl.resourcepacks.Texturepack
import dev.treset.treelauncher.backend.config.appConfig
import dev.treset.treelauncher.backend.launching.resources.ResourcepacksDisplayData
import dev.treset.treelauncher.backend.util.assignFrom
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.backend.util.serialization.MutableDataState
import dev.treset.treelauncher.backend.util.serialization.MutableDataStateList
import dev.treset.treelauncher.util.ListDisplay
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.io.IOException

@Serializable
class ResourcepackComponent(
    override val id: MutableDataState<String>,
    override val name: MutableDataState<String>,
    @Transient override val file: MutableDataState<LauncherFile> = mutableStateOf(LauncherFile.of("")),
    override val includedFiles: MutableDataStateList<String> = appConfig().resourcepacksDefaultIncludedFiles.toMutableStateList(),
    override val lastUsed: MutableDataState<String> = mutableStateOf(""),
    override val active: MutableDataState<Boolean> = mutableStateOf(false),
    override val listDisplay: MutableDataState<ListDisplay?> = mutableStateOf(null)
): Component() {
    override val type = LauncherManifestType.RESOURCEPACKS_COMPONENT
    @Transient override var expectedType = LauncherManifestType.RESOURCEPACKS_COMPONENT

    constructor(
        id: String,
        name: String,
        file: LauncherFile,
        active: Boolean = false,
        lastUsed: String = "",
        includedFiles: List<String> = appConfig().resourcepacksDefaultIncludedFiles,
        listDisplay: ListDisplay? = null
    ): this(
        mutableStateOf(id),
        mutableStateOf(name),
        mutableStateOf(file),
        includedFiles.toMutableStateList(),
        mutableStateOf(lastUsed),
        mutableStateOf(active),
        mutableStateOf(listDisplay)
    )

    fun ResourcepacksDisplayData.reload(gameDataDir: LauncherFile) {
        loadResourcepacks(gameDataDir)
        loadTexturepacks(gameDataDir)
        onAddResourcepack.value = { f -> this.onAddResourcepack(f, gameDataDir) }
        onAddTexturepack.value = { f -> this.onAddTexturepack(f, gameDataDir) }
    }


    private fun ResourcepacksDisplayData.loadResourcepacks(gameDataDir: LauncherFile) {
        val resourcepacksDirectory = LauncherFile.of(getContentDirectory(gameDataDir), "resourcepacks")

        resourcepacks.assignFrom(
            resourcepacksDirectory.listFiles().associate {
                try {
                    Resourcepack.get(it)
                } catch (e: Exception) {
                    null
                }.let { rp -> rp to it }
            }
        )
    }

    private fun ResourcepacksDisplayData.loadTexturepacks(gameDataDir: LauncherFile) {
        val texturepacksDirectory = LauncherFile.of(getContentDirectory(gameDataDir), "texturepacks")

        texturepacks.assignFrom(
            texturepacksDirectory.listFiles().associate {
                try {
                    Texturepack.get(it)
                } catch (e: Exception) {
                    null
                }.let { tp -> tp to it }
            }
        )
    }

    @Throws(IOException::class)
    private fun ResourcepacksDisplayData.onAddResourcepack(source: List<LauncherFile>, gameDataDir: LauncherFile) {
        val directory = LauncherFile.of(getContentDirectory(gameDataDir), "resourcepacks")
        for(file in source) {
            file.copyUniqueTo(directory)
        }
        this.loadResourcepacks(gameDataDir)
    }

    @Throws(IOException::class)
    private fun ResourcepacksDisplayData.onAddTexturepack(source: List<LauncherFile>, gameDataDir: LauncherFile) {
        val directory = LauncherFile.of(getContentDirectory(gameDataDir), "texturepacks")
        for (file in source) {
            file.copyUniqueTo(directory)
        }
        this.loadTexturepacks(gameDataDir)
    }
}