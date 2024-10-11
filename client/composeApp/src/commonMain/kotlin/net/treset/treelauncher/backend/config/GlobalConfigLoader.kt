package net.treset.treelauncher.backend.config

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mcdl.json.GenericJsonParsable
import net.treset.mcdl.json.SerializationException
import net.treset.treelauncher.backend.data.manifest.LauncherManifestType
import net.treset.treelauncher.backend.util.file.LauncherFile
import java.io.File
import java.io.IOException
import java.nio.file.StandardCopyOption

class GlobalConfigLoader {
    @Throws(IllegalStateException::class, IOException::class)
    fun loadConfig(): Config {
        if (!file.exists()) {
            LOGGER.info { "No config found, creating default" }

            val path = "${System.getenv("LOCALAPPDATA")}/treelauncher-data" + if(System.getenv("debug") == "true") "-debug" else ""
            file.write(
                "path=$path"
            )
        }
        val contents: String = file.readString()
        val lines = contents.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var path: String? = null
        var updateUrl: String? = null
        for (line in lines) {
            if (line.startsWith("path=")) {
                path = line.substring(5).replace("\r", "").replace("\n", "")
            } else if (line.startsWith("update_url=")) {
                updateUrl = line.substring(11).replace("\r", "").replace("\n", "")
            }
        }
        check(!(path.isNullOrBlank())) { "Invalid config: path=$path" }
        LOGGER.info { "Loaded config: path=$path" }
        val config = Config(path, updateUrl)
        setAppConfig(config)
        return config
    }

    @Throws(IOException::class)
    fun updatePath(path: File, removeOld: Boolean) {
        val dstDir: LauncherFile = LauncherFile.of(path)
        LOGGER.info { "Updating path: path=${dstDir.absolutePath}, removeOld=$removeOld" }
        if (!dstDir.isDirectory()) {
            throw IOException("Path is not a directory")
        }
        if (appConfig().baseDir.isChildOf(dstDir)) {
            throw IOException("Current Directory is child of Selected Directory")
        }
        if (dstDir.isChildOf(appConfig().baseDir)) {
            throw IOException("Selected Directory is child of Current Directory")
        }
        val contents: String = file.readString()
        val lines = contents.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val newContents = StringBuilder()
        for (line in lines) {
            if (line.startsWith("path=")) {
                newContents.append("path=").append(dstDir.absolutePath).append("/").append("\n")
            } else {
                newContents.append(line).append("\n")
            }
        }
        if (dstDir.isDirEmpty) {
            LOGGER.debug { "Copying files to new directory" }
            appConfig().baseDir.copyTo(dstDir, StandardCopyOption.REPLACE_EXISTING)
        } else {
            if (!hasMainManifest(dstDir)) {
                throw IOException("Directory is not empty and doesn't contain manifest file")
            }
            LOGGER.debug { "Not copying files to new directory because it is not empty" }
        }
        LOGGER.debug { "Updating config" }
        val oldPath: LauncherFile = appConfig().baseDir
        appConfig().baseDir = dstDir
        file.write(newContents.toString())
        if (removeOld) {
            LOGGER.debug { "Removing old directory" }
            oldPath.remove()
        }
    }

    fun hasMainManifest(path: LauncherFile): Boolean {
        val contents: String = try {
            LauncherFile.of(path, appConfig().manifestFileName).readString()
        } catch (e: IOException) {
            return false
        }
        if (contents.isBlank()) {
            return false
        }
        try {
            LauncherManifest.fromJson(contents)
            return true
        } catch (e: SerializationException) {
            return false
        }
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
        private val file: LauncherFile = LauncherFile.of("app", "treelauncher.conf")
    }
}

private class LauncherManifest(
    val type: LauncherManifestType,
): GenericJsonParsable() {

    companion object {
        @Throws(SerializationException::class)
        fun fromJson(json: String): LauncherManifest {
            val launcherManifest = fromJson(json, LauncherManifest::class.java)
            if(launcherManifest.type != LauncherManifestType.LAUNCHER) {
                throw SerializationException("Invalid type: ${launcherManifest.type}")
            }
            return launcherManifest
        }
    }
}
