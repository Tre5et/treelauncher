package net.treset.treelauncher.backend.launching

import io.github.oshai.kotlinlogging.KotlinLogging
import net.hycrafthd.minecraft_authenticator.login.User
import net.treset.treelauncher.backend.data.InstanceData
import net.treset.treelauncher.backend.data.LauncherFiles
import net.treset.treelauncher.backend.util.QuickPlayData
import net.treset.treelauncher.backend.util.exception.FileLoadException
import net.treset.treelauncher.backend.util.exception.GameCommandException
import net.treset.treelauncher.backend.util.exception.GameLaunchException
import net.treset.treelauncher.backend.util.exception.GameResourceException
import net.treset.treelauncher.backend.util.file.LauncherFile
import java.io.IOException

class GameLauncher(
    val instance: InstanceData,
    val files: LauncherFiles,
    val offline: Boolean,
    val minecraftUser: User?,
    val quickPlayData: QuickPlayData? = null,
    var onExit: (String?) -> Unit = { _ -> },
    var onResourceCleanupFailed:  (Exception, (retry: Boolean) -> Unit) -> Unit  = { _, _ -> },
    var onExited: (String?) -> Unit = { _ -> },
) {
    private var resourceManager: ResourceManager? = null
    var gameListener: GameListener? = null

    @Throws(GameLaunchException::class)
    fun launch(cleanupActiveInstance: Boolean, doneCallback: (Exception?) -> Unit) {
        try {
            files.reloadAll()
        } catch (e: FileLoadException) {
            throw GameLaunchException("Unable to launch game: file reload failed", e)
        }
        if (!files.launcherDetails.activeInstance.isNullOrBlank()) {
            if (!cleanupActiveInstance) {
                throw GameLaunchException("Unable to launch game: active instance already exists")
            }
            try {
                cleanUpOldInstance()
            } catch (e: GameLaunchException) {
                throw GameLaunchException("Unable to launch game: unable to clean up old instance", e)
            }
        }
        resourceManager = ResourceManager(instance)
        Thread(Runnable {
            try {
                resourceManager!!.setLastPlayedTime()
                resourceManager!!.prepareResources()
            } catch (e: GameResourceException) {
                val gle = GameLaunchException("Unable to launch game: unable to prepare resources", e)
                try {
                    abortLaunch()
                } catch (ex: GameLaunchException) {
                    doneCallback(GameLaunchException("Unable to launch game: unable to cleanup after fail", gle))
                }
                doneCallback(gle)
                return@Runnable
            } catch (e: IOException) {
                val gle = GameLaunchException("Unable to launch game: unable to prepare resources", e)
                try {
                    abortLaunch()
                } catch (ex: GameLaunchException) {
                    doneCallback(GameLaunchException("Unable to launch game: unable to cleanup after fail", gle))
                }
                doneCallback(gle)
                return@Runnable
            }
            try {
                finishLaunch()
                doneCallback(null)
            } catch (e: GameLaunchException) {
                doneCallback(e)
            }
        }).start()
    }

    fun stop() {
        gameListener?.stop()
    }

    @Throws(GameLaunchException::class)
    private fun finishLaunch() {
        val pb = ProcessBuilder().redirectOutput(ProcessBuilder.Redirect.PIPE)
        pb.inheritIO()
        val commandBuilder = CommandBuilder(pb, instance, offline, minecraftUser, quickPlayData)
        try {
            commandBuilder.makeStartCommand()
        } catch (e: GameCommandException) {
            abortLaunch()
            throw GameLaunchException("Unable to launch game: unable to set start command", e)
        }
        LOGGER.info { "Starting game" }
        LOGGER.debug { "command=" + pb.command() }
        try {
            val p = pb.start()
            resourceManager?.let { _ ->
                GameListener(p, this::cleanupResources).let {
                    gameListener = it
                    it.start()
                }
            }?: throw GameLaunchException("Unable to launch game: unable to create game listener: no resource manager")
        } catch (e: IOException) {
            abortLaunch()
            throw GameLaunchException("Unable to launch game: unable to execute command", e)
        }
    }

    @Throws(GameLaunchException::class)
    private fun cleanUpOldInstance() {
        LOGGER.debug { "Cleaning up old instance resources: id=${files.launcherDetails.activeInstance}" }
        var found = false
        for (i in files.instanceComponents) {
            if (i.first.id == files.launcherDetails.activeInstance) {
                val instanceData: InstanceData = try {
                    InstanceData.of(i, files)
                } catch (e: FileLoadException) {
                    throw GameLaunchException("Unable to cleanup old instance: unable to load instance data", e)
                }

                val resourceManager = ResourceManager(instanceData)
                try {
                    resourceManager.cleanupGameFiles()
                } catch (e: GameResourceException) {
                    throw GameLaunchException("Unable to cleanup old instance: unable to cleanup resources", e)
                }

                found = true
                break
            }
        }
        if (!found) {
            throw GameLaunchException("Unable to cleanup old instance: instance not found")
        }
        LOGGER.info { "Old instance resources cleaned up" }
    }

    @Throws(GameLaunchException::class)
    private fun abortLaunch() {
        LOGGER.warn { "Aborting launch" }
        try {
            resourceManager?.cleanupGameFiles() ?: throw GameLaunchException("Unable to abort launch correctly: no resource manager")
        } catch (e: GameResourceException) {
            throw GameLaunchException("Unable to abort launch correctly: failed to cleanup game files")
        }
        files.launcherDetails.activeInstance = null
        try {
            LauncherFile.of(files.mainManifest.directory, files.mainManifest.details).write(files.launcherDetails)
        } catch (e: IOException) {
            throw GameLaunchException("Unable to abort launch correctly: failed to write launcher details")
        }
    }

    private fun cleanupResources(playDuration: Long, error: String?) {
        LOGGER.info { "Cleaning up resources" }
        onExit(error)
        try {
            resourceManager?.addPlayDuration(playDuration)
        } catch (e: IOException) {
            LOGGER.error(e) { "Unable to add play duration to statistics: duration=$playDuration" }
        }

        try {
            resourceManager?.cleanupGameFiles()
            onExited(error)
        } catch (e: GameResourceException) {
            onResourceCleanupFailed(e) { retry ->
                if(retry) {
                    cleanupResources(playDuration, error)
                } else {
                    onExited(error)
                }
            }
        }
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}
