package net.treset.treelauncher.backend.util

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.treelauncher.backend.data.manifest.LauncherManifestType
import net.treset.treelauncher.backend.data.manifest.MainManifest
import net.treset.treelauncher.backend.data.manifest.Manifest
import net.treset.treelauncher.backend.data.manifest.ParentManifest
import net.treset.treelauncher.backend.util.file.LauncherFile
import java.io.IOException

class FileInitializer(val directory: LauncherFile) {
    private val dirs: Array<LauncherFile>
    private val files: Array<Manifest>

    init {
        require(directory.isDirectory() || directory.mkdirs()) { "Cannot create directory" }
        dirs = arrayOf(
            LauncherFile.of(directory, "game_data"),
            LauncherFile.of(directory, "assets"),
            LauncherFile.of(directory, "libraries"),
            LauncherFile.of(directory, "instance_components"),
            LauncherFile.of(directory, "saves_components"),
            LauncherFile.of(directory, "resourcepack_components"),
            LauncherFile.of(directory, "options_components"),
            LauncherFile.of(directory, "mods_components"),
            LauncherFile.of(directory, "version_components"),
            LauncherFile.of(directory, "java_components")
        )
        files = arrayOf(
            MainManifest(
                LauncherManifestType.LAUNCHER,
                activeInstance = null,
                assetsDir = "assets",
                librariesDir = "libraries",
                gameDataDir = "game_data",
                instancesDir = "instances",
                savesDir = "save_components",
                resourcepacksDir = "resourcepack_components",
                optionsDir = "option_components",
                modsDir = "mod_components",
                versionDir = "version_components",
                javasDir = "java_components",
                file = LauncherFile.of(directory, "manifest.json")
            ),
            InitializingManifest(LauncherManifestType.INSTANCES, "instance", "instances", "manifest.json"),
            InitializingManifest(LauncherManifestType.SAVES, "saves", "saves_components", "manifest.json"),
            InitializingManifest(LauncherManifestType.RESOURCEPACKS, "resourcepacks", "resourcepack_components", "manifest.json"),
            InitializingManifest(LauncherManifestType.OPTIONS, "options", "options_components", "manifest.json"),
            InitializingManifest(LauncherManifestType.MODS, "mods", "mods_components", "manifest.json"),
            InitializingManifest(LauncherManifestType.VERSIONS, "version", "version_components", "manifest.json"),
            InitializingManifest(LauncherManifestType.JAVAS, "java", "java_components", "manifest.json")
        )
    }

    @Throws(IOException::class)
    fun create() {
        LOGGER.info { "Creating default files: directory=$directory" }
        if (!directory.isDirectory || !directory.isDirEmpty ) {
            throw IOException("Directory is not empty")
        }
        for (dir in dirs) {
            dir.createDir()
        }
        for (file in files) {
            file.write()
        }
    }

    inner class InitializingManifest(
        type: LauncherManifestType, prefix: String, vararg path: String
    ) : ParentManifest(type, prefix, mutableListOf(), LauncherFile.of(directory, *path))

    companion object {
        private val LOGGER = KotlinLogging.logger{}
    }
}
