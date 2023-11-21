package net.treset.treelauncher.backend.util

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mc_version_loader.json.JsonParsable
import net.treset.mc_version_loader.launcher.LauncherDetails
import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.treelauncher.backend.util.file.LauncherFile
import java.io.File
import java.io.IOException

class FileInitializer(directory: LauncherFile) {
    private val directory: LauncherFile
    private val dirs: Array<LauncherFile>
    private val files: Array<InitializingManifest>

    init {
        require(directory.isDirectory() || directory.mkdirs()) { "Cannot create directory" }
        this.directory = directory
        dirs = arrayOf(
            LauncherFile.of(directory, "game_data"),
            LauncherFile.of(directory, "instance_data"),
            LauncherFile.of(directory, "java_data"),
            LauncherFile.of(directory, "options_data"),
            LauncherFile.of(directory, "resourcepack_data"),
            LauncherFile.of(directory, "version_data"),
            LauncherFile.of(directory, "libraries"),
            LauncherFile.of(directory, "assets")
        )
        files = arrayOf(
            InitializingManifest(
                LauncherManifest("launcher", null, null, "launcher.json", null, null, null, null),
                "manifest.json"
            ),
            InitializingManifest(
                LauncherDetails(
                    null,
                    "assets",
                    "game_data",
                    "game",
                    "instance_component",
                    "instance_data",
                    "instances",
                    "java_component",
                    "java_data",
                    "javas",
                    "libraries",
                    "mods_component",
                    "mods",
                    "options_component",
                    "options_data",
                    "options",
                    "resourcepack_component",
                    "resourcepack_data",
                    "resourcepacks",
                    "saves_component",
                    "saves",
                    "version_component",
                    "version_data",
                    "versions"
                ), "launcher.json"
            ),
            InitializingManifest("game", null, arrayOf("mods.json", "saves.json"), "game_data", "manifest.json"),
            InitializingManifest("mods", "mods", "game_data", "mods.json"),
            InitializingManifest("saves", "saves", "game_data", "saves.json"),
            InitializingManifest("instances", "instance", "instance_data", "manifest.json"),
            InitializingManifest("javas", "java", "java_data", "manifest.json"),
            InitializingManifest("options", "options", "options_data", "manifest.json"),
            InitializingManifest("resourcepacks", "resourcepacks", "resourcepack_data", "manifest.json"),
            InitializingManifest("versions", "version", "version_data", "manifest.json")
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
            file.make()
        }

        // this is a terrible hack for the packaging
        removeUpdaterFromClasspath()
    }

    private fun removeUpdaterFromClasspath() {
        val cfg: LauncherFile = LauncherFile.of("app", "TreeLauncher.cfg")
        if (cfg.exists()) {
            try {
                var content: String = cfg.readString()
                content = content.replace("app.classpath=\$APPDIR\\updater.jar", "")
                cfg.write(content)
            } catch (e: IOException) {
                LOGGER.error(e) { "Failed to remove updater classpath" }
            }
        }
    }

    inner class InitializingManifest(val file: LauncherFile, val manifest: JsonParsable) {
        constructor(manifest: JsonParsable, vararg path: String) : this(LauncherFile.of(directory, *path), manifest)
        constructor(type: String?, prefix: String?, components: Array<String?>?, vararg path: String) : this(
            LauncherManifest(type, null, null, null, prefix, null, null, components?.toList()),
            *path
        )

        constructor(type: String?, prefix: String?, vararg path: String) : this(type, prefix, arrayOf(), *path)

        @Throws(IOException::class)
        fun make() {
            file.write(manifest)
        }
    }

    companion object {
        private val LOGGER = KotlinLogging.logger{}
    }
}
