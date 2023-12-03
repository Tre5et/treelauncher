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
    private val instance: InstanceData,
    private val files: LauncherFiles,
    private val minecraftUser: User,
    private val quickPlayData: QuickPlayData?,
    private val exitCallbacks: Array<(String?) -> Unit>
) {
    private var resourceManager: ResourceManager? = null
        private set
    var gameListener: GameListener? = null


    constructor(
        instance: InstanceData,
        files: LauncherFiles,
        minecraftUser: User,
        quickPlayData: QuickPlayData?
    ) : this(instance, files, minecraftUser, quickPlayData, arrayOf())

    constructor(
        instance: InstanceData,
        files: LauncherFiles,
        minecraftUser: User,
        exitCallbacks: Array<(String?) -> Unit>
    ) : this(instance, files, minecraftUser, null, exitCallbacks)

    constructor(instance: InstanceData, files: LauncherFiles, minecraftUser: User) : this(
        instance,
        files,
        minecraftUser,
        null,
        arrayOf()
    )

    @Throws(GameLaunchException::class)
    fun launch(cleanupActiveInstance: Boolean, doneCallback: (Exception?) -> Unit) {
        try {
            files.reloadAll()
        } catch (e: FileLoadException) {
            throw GameLaunchException("Unable to launch game: file reload failed", e)
        }
        if (files.launcherDetails.activeInstance != null && files.launcherDetails.activeInstance.isNotBlank()) {
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
        files.launcherDetails.activeInstance = instance.instance.first.id
        try {
            LauncherFile.of(files.mainManifest.directory, files.mainManifest.details).write(files.launcherDetails)
        } catch (e: IOException) {
            throw GameLaunchException("Unable to launch game: unable to write launcher details", e)
        }
        Thread(Runnable {
            try {
                resourceManager!!.prepareResources()
                resourceManager!!.setLastPlayedTime()
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

    @Throws(GameLaunchException::class)
    private fun finishLaunch() {
        val pb = ProcessBuilder().redirectOutput(ProcessBuilder.Redirect.PIPE)
        val commandBuilder = CommandBuilder(pb, instance, minecraftUser, quickPlayData)
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
            resourceManager?.let { resourceManager ->
                GameListener(p, resourceManager, exitCallbacks).let {
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

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}
