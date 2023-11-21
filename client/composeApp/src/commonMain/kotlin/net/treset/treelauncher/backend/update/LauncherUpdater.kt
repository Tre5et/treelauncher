package net.treset.treelauncher.backend.update

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mc_version_loader.json.JsonUtils
import net.treset.treelauncher.backend.util.file.LauncherFile
import java.io.IOException

class LauncherUpdater {
    private val updateService: UpdateService = UpdateService()
    private var update: Update? = null
    private var readyToUpdate = false

    @Throws(IOException::class)
    fun getUpdate(): Update {
        return update?.let {
            it
        }?: fetchUpdate()
    }

    @Throws(IOException::class)
    fun fetchUpdate(): Update {
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
                    LOGGER.debug("Reverting file: $file")
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
        LOGGER.info("Starting updater...")
        val pb = ProcessBuilder(
            LauncherFile.of(System.getProperty("java.home"), "bin", "java").path,
            "-jar",
            "app/updater.jar"
        )
        if (restart) {
            pb.command().add("-gui")
            if (LauncherFile("TreeLauncher.exe").isFile()) {
                LOGGER.info { "Restarting TreeLauncher.exe after update" }
                pb.command().add("-rTreeLauncher.exe")
            } else {
                LOGGER.warn("TreeLauncher.exe not found to restart, searching alternative file...")
                val files: Array<LauncherFile> = LauncherFile(".").listFiles()
                for (file in files) {
                    if (file.getName().endsWith(".exe")) {
                        LOGGER.info { ("Restarting alternative file " + file.getName()).toString() + " after update" }
                        pb.command().add("-r" + file.getName())
                        break
                    }
                }
            }
        }
        pb.start()
    }

    companion object {
        private val LOGGER = KotlinLogging.logger{}
    }
}
