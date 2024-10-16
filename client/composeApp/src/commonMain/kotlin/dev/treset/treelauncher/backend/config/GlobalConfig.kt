package dev.treset.treelauncher.backend.config

import dev.treset.mcdl.json.GenericJsonParsable
import dev.treset.mcdl.json.SerializationException
import dev.treset.treelauncher.backend.util.file.LauncherFile

class GlobalConfig(
    var path: String,
) : GenericJsonParsable() {
    companion object {
        @Throws(SerializationException::class)
        fun fromJson(json: String): GlobalConfig = fromJson(json, GlobalConfig::class.java)

        @Throws(IllegalStateException::class)
        fun validateDataPath(
            path: LauncherFile,
            prevPath: LauncherFile? = null,
            allowLauncherData: Boolean = true
        ) {
            if (!path.isDirectory() && !path.mkdirs()) {
                throw IllegalStateException("Path is not a directory")
            }
            prevPath?.let {
                if (it.isChildOf(path)) {
                    throw IllegalStateException("Current Directory is child of Selected Directory")
                }
                if (path.isChildOf(it)) {
                    throw IllegalStateException("Selected Directory is child of Current Directory")
                }
            }
            if(!path.isDirEmpty) {
                if(!isLauncherDataPath(path)) {
                    throw IllegalStateException("Directory is not empty and doesn't contain manifest file")
                } else if(!allowLauncherData) {
                    throw IllegalStateException("Directory is already a launcher directory")
                }
            }
        }

        fun isLauncherDataPath(directory: LauncherFile): Boolean {
            if(!directory.isDirectory || directory.isDirEmpty) {
                return false
            }

            val files = directory.listFiles()
            return files.firstOrNull { it.name == "manifest.json" } != null
        }
    }
}