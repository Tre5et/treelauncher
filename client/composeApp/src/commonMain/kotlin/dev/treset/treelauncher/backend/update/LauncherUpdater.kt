package dev.treset.treelauncher.backend.update

import io.github.oshai.kotlinlogging.KotlinLogging
import dev.treset.mcdl.json.JsonUtils
import dev.treset.treelauncher.backend.config.AppSettings
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.getUpdaterProcess
import java.io.IOException
import java.nio.file.Paths

class LauncherUpdater {
    private var updateService: UpdateService = UpdateService(AppSettings.updateUrl.value)
    private var update: Update? = null
    private var readyToUpdate = false

    @Throws(IOException::class)
    fun getUpdate(): Update {
        return update ?: fetchUpdate()
    }

    @Throws(IOException::class)
    fun fetchUpdate(): Update {
        updateService = UpdateService(AppSettings.updateUrl.value)
        updateService.update().let {
            update = it
            return it
        }
    }

    @Throws(IOException::class)
    fun executeUpdate(changeCallback: (Int, Int, String) -> Unit) {
        changeCallback(0, 0, "Checking for updates...")
        if (update == null) {
            fetchUpdate()
        }
        update!!.let {
            if (it.id == null) {
                throw IOException("No Update available")
            }
            val total = it.changes?.size?: throw IOException("No Update available")
            var current = 0
            changeCallback(current, total, "Downloading files...")
            val updaterChanges = mutableListOf<Update.Change>()
            val exceptions = mutableListOf<Exception>()
            val backedUpFiles = mutableListOf<LauncherFile>()
            for (change in it.changes?: listOf()) {
                changeCallback(++current, total, change.path)
                val targetFile: LauncherFile = LauncherFile.of(change.path)
                val updateFile: LauncherFile = LauncherFile.of(change.path + ".up")
                val backupFile: LauncherFile = LauncherFile.of(change.path + ".bak")
                when (change.mode) {
                    Update.Mode.FILE -> {
                        try {
                            LOGGER.debug { "Downloading file: " + change.path }
                            updateFile.write(updateService.file(it.id!!, change.path))
                        } catch (e: IOException) {
                            exceptions.add(e)
                            LOGGER.warn(e) { "Failed to download file: " + change.path }
                        }
                        if (change.updater) {
                            LOGGER.debug { "Delegating file to updater: " + change.path }
                            updaterChanges.add(change)
                        } else {
                            LOGGER.debug { "Moving file to target: " + change.path }
                            try {
                                if (targetFile.isFile()) {
                                    targetFile.moveTo(backupFile)
                                    backedUpFiles.add(backupFile)
                                }
                                updateFile.moveTo(targetFile)
                            } catch (e: IOException) {
                                exceptions.add(e)
                                LOGGER.warn(e) { "Failed to move file: " + change.path }
                            }
                        }
                    }

                    Update.Mode.DELETE -> if (change.updater) {
                        LOGGER.debug { "Delegating file to updater: " + change.path }
                        updaterChanges.add(change)
                    } else {
                        LOGGER.debug { "Deleting file: " + change.path }
                        try {
                            if (targetFile.isFile()) {
                                targetFile.moveTo(backupFile)
                                backedUpFiles.add(backupFile)
                            } else {
                                LOGGER.warn { "File to delete does not exist: $targetFile" }
                            }
                        } catch (e: IOException) {
                            exceptions.add(e)
                            LOGGER.warn(e) { "Failed to delete file: " + change.path }
                        }
                    }

                    else -> {
                        LOGGER.debug { "Delegating file to updater: " + change.path }
                        updaterChanges.add(change)
                    }
                }
            }
            if (exceptions.isNotEmpty()) {
                LOGGER.debug { "Update failed. Reverting changes" }
                changeCallback(0, 0, "Update Failed. Reverting changes...")
                for (file in backedUpFiles) {
                    LOGGER.debug { "Reverting file: $file" }
                    try {
                        file.moveTo(LauncherFile.of(file.path.substring(0, file.path.length - 4)))
                    } catch (e: IOException) {
                        throw IOException("Failed to revert changes", e)
                    }
                }
                throw IOException("Failed to update the launcher", exceptions[0])
            }
            LOGGER.debug { "Removing backed up files" }
            for (file in backedUpFiles) {
                try {
                    file.remove()
                } catch (ignored: IOException) {
                }
            }
            LOGGER.debug { "Writing updater file" }
            changeCallback(total, total, "Writing updater file...")
            val updaterFile: LauncherFile = LauncherFile.of("update.json")
            readyToUpdate = try {
                updaterFile.write(
                    JsonUtils.getGson().toJson(updaterChanges)
                )
                true
            } catch (e: IOException) {
                throw IOException("Failed to write updater file", e)
            }
        }
    }

    @Throws(IOException::class)
    fun startUpdater(restart: Boolean) {
        if (!readyToUpdate) {
            return
        }
        LOGGER.info { "Starting updater..." }
        val path = Paths.get("").toAbsolutePath()
        val commandBuilder = StringBuilder()
        commandBuilder.append(" -i").append(path).append("/update.json")
        commandBuilder.append(" -o").append(path).append("/updater.json")
        if (restart) {
            if (LauncherFile("TreeLauncher.exe").isFile()) {
                LOGGER.info { "Restarting TreeLauncher.exe after update" }
                commandBuilder.append(" -r${path};${path}/TreeLauncher.exe")
            } else {
                LOGGER.warn { "TreeLauncher.exe not found to restart, searching alternative file..." }
                val files: Array<LauncherFile> = LauncherFile(".").listFiles()
                for (file in files) {
                    if (file.getName().endsWith(".exe")) {
                        LOGGER.info { "Restarting alternative file ${file.getName()} after update" }
                        commandBuilder.append(" -r${path};${path}/${file.getName()}")
                        break
                    }
                }
            }
        }
        getUpdaterProcess(commandBuilder.toString()).start()
    }

    companion object {
        private val LOGGER = KotlinLogging.logger{}
    }
}

private val updater = LauncherUpdater()
fun updater() = updater