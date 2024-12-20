package dev.treset.treelauncher.backend.launching

import io.github.oshai.kotlinlogging.KotlinLogging
import dev.treset.mcdl.auth.data.UserData
import dev.treset.treelauncher.backend.data.LauncherFiles
import dev.treset.treelauncher.backend.data.manifest.InstanceComponent
import dev.treset.treelauncher.backend.util.QuickPlayData
import dev.treset.treelauncher.backend.util.exception.GameCommandException
import dev.treset.treelauncher.backend.util.exception.GameLaunchException
import dev.treset.treelauncher.backend.util.exception.GameResourceException
import java.io.IOException

class GameLauncher(
    val instance: InstanceComponent,
    val files: LauncherFiles,
    val offline: Boolean,
    val minecraftUser: UserData?,
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
            files.reload()
        } catch (e: IOException) {
            throw GameLaunchException("Unable to launch game: file reload failed", e)
        }
        if (!files.mainManifest.activeInstance.value.isNullOrBlank()) {
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
        LOGGER.debug { "Cleaning up old instance resources: id=${files.mainManifest.activeInstance}" }
        var found = false
        for (instance in files.instanceComponents) {
            if (instance.id == files.mainManifest.activeInstance) {
                val resourceManager = ResourceManager(instance)
                try {
                    resourceManager.cleanupResources()
                } catch (e: IOException) {
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
            resourceManager?.cleanupResources() ?: throw GameLaunchException("Unable to abort launch correctly: no resource manager")
        } catch (e: IOException) {
            throw GameLaunchException("Unable to abort launch correctly: failed to cleanup game files")
        }
        files.mainManifest.activeInstance.value = null
        try {
            files.mainManifest.write()
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
            resourceManager?.cleanupResources()
            onExited(error)
        } catch (e: IOException) {
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
