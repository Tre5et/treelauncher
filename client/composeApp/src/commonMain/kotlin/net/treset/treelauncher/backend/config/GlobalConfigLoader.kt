package net.treset.treelauncher.backend.config

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mc_version_loader.json.SerializationException
import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.mc_version_loader.launcher.LauncherManifestType
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.localization.Language
import java.io.File
import java.io.IOException
import java.nio.file.StandardCopyOption

class GlobalConfigLoader {
    @Throws(IllegalStateException::class, IOException::class)
    fun loadConfig(): Config {
        if (!file.exists()) {
            LOGGER.info { "No config found, creating default" }
            file.write(
                "path=data${System.lineSeparator()}update_url=http://update.treelauncher.net:8732"
            )
        }
        val contents: String = file.readString()
        val lines = contents.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var path: String? = null
        var debug = false
        var updateUrl: String? = null
        for (line in lines) {
            if (line.startsWith("path=")) {
                path = line.substring(5).replace("\r", "").replace("\n", "")
            } else if (line.startsWith("debug=")) {
                debug = line.substring(6).toBoolean()
            } else if (line.startsWith("update_url=")) {
                updateUrl = line.substring(11).replace("\r", "").replace("\n", "")
            }
        }
        check(!(path.isNullOrBlank() || updateUrl.isNullOrBlank())) { "Invalid config: path=$path, updateUrl=$updateUrl" }
        LOGGER.info { "Loaded config: path=$path, debug=$debug" }
        return Config(path, debug, updateUrl)
    }

    @Throws(IOException::class)
    fun updatePath(path: File, removeOld: Boolean) {
        val dstDir: LauncherFile = LauncherFile.of(path)
        LOGGER.info("Updating path: path=${dstDir.absolutePath}, removeOld=$removeOld")
        if (!dstDir.isDirectory()) {
            throw IOException("Path is not a directory")
        }
        if (appConfig().BASE_DIR.isChildOf(dstDir)) {
            throw IOException("Path is a child of the current directory")
        }
        val contents: String = file.readString()
        val lines = contents.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val newContents = StringBuilder()
        for (line in lines) {
            if (line.startsWith("path=")) {
                newContents.append("path=").append(dstDir.absolutePath).append("/").append("\n")
                break
            } else {
                newContents.append(line).append("\n")
            }
        }
        if (dstDir.isDirEmpty) {
            LOGGER.debug { "Copying files to new directory" }
            appConfig().BASE_DIR.copyTo(dstDir, StandardCopyOption.REPLACE_EXISTING)
        } else {
            if (!hasMainMainfest(dstDir)) {
                throw IOException("Directory is not empty and doesn't contain manifest file")
            }
            LOGGER.debug { "Not copying files to new directory because it is not empty" }
        }
        LOGGER.debug { "Updating config" }
        val oldPath: LauncherFile = appConfig().BASE_DIR
        appConfig().BASE_DIR = dstDir
        file.write(newContents.toString())
        if (removeOld) {
            LOGGER.debug { "Removing old directory" }
            oldPath.remove()
        }
    }

    fun hasMainMainfest(path: LauncherFile): Boolean {
        val contents: String = try {
            LauncherFile.of(path, appConfig().MANIFEST_FILE_NAME).readString()
        } catch (e: IOException) {
            return false
        }
        if (contents.isBlank()) {
            return false
        }
        val manifest: LauncherManifest = try {
            LauncherManifest.fromJson(contents)
        } catch (e: SerializationException) {
            return false
        }
        return manifest.type == LauncherManifestType.LAUNCHER
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
        private val file: LauncherFile = LauncherFile.of("app", "launcher.conf")
    }
}
