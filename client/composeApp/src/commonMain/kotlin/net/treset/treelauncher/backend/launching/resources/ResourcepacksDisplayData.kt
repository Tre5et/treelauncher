package net.treset.treelauncher.backend.launching.resources

import net.treset.mcdl.resourcepacks.Resourcepack
import net.treset.mcdl.resourcepacks.Texturepack
import net.treset.treelauncher.backend.util.file.LauncherFile
import java.io.IOException

data class ResourcepacksDisplayData(
    var resourcepacks: Map<Resourcepack, LauncherFile>,
    var texturepacks: Map<Texturepack, LauncherFile>,
    private val onAddResourcepack: ResourcepacksDisplayData.(source: List<LauncherFile>) -> Unit,
    private val onAddTexturepack: ResourcepacksDisplayData.(source: List<LauncherFile>) -> Unit
) {
    @Throws(IOException::class)
    fun addResourcepacks(source: List<LauncherFile>) {
        onAddResourcepack(source)
    }

    @Throws(IOException::class)
    fun addTexturepacks(source: List<LauncherFile>) {
        onAddTexturepack(source)
    }
}