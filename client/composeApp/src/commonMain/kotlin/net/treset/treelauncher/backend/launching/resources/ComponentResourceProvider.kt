package net.treset.treelauncher.backend.launching.resources

import net.treset.treelauncher.backend.data.manifest.Component
import net.treset.treelauncher.backend.util.exception.GameResourceException
import net.treset.treelauncher.backend.util.file.LauncherFile

abstract class ComponentResourceProvider<T: Component>(
    val component: T,
    val gameDataDir: LauncherFile
) {
    @Throws(GameResourceException::class)
    abstract fun includeResources()

    @Throws(GameResourceException::class)
    abstract fun removeResources(files: MutableList<LauncherFile>, unexpected: Boolean = false)
}