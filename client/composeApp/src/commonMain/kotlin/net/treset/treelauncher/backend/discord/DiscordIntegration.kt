package net.treset.treelauncher.backend.discord

import de.jcm.discordgamesdk.Core
import de.jcm.discordgamesdk.CreateParams
import de.jcm.discordgamesdk.activity.Activity
import de.jcm.discordgamesdk.activity.ActivityType
import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.treelauncher.backend.config.appSettings
import net.treset.treelauncher.backend.data.InstanceData
import net.treset.treelauncher.localization.strings
import java.time.Instant

class DiscordIntegration {
    var core: Core? = null
    init {
        startCore()
        Thread {
            while(true) {
                core?.runCallbacks()
                try {
                    Thread.sleep(16)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }.start()
    }

    private fun startCore() {
        val params = CreateParams()
        params.clientID = 1241748395115155486
        params.flags = CreateParams.getDefaultFlags()

        try {
            this.core = Core(params)
            LOGGER.debug { "Started discord core" }
        } catch (e: Exception) {
            LOGGER.debug(e) { "Failed to start discord core. Is discord running?" }
        }
    }

    fun activateActivity(instance: InstanceData) {
        if(!appSettings().discordIntegration) {
            return
        }

        if(core == null) {
            startCore()
        }

        core?.let { core ->
            val activity = Activity()
            activity.type = ActivityType.PLAYING
            activity.details = constructDetailsString(instance)

            activity.assets().largeImage = "pack"
            if (appSettings().discordShowModLoader) {
                activity.assets().smallImage = instance.versionComponents[0].second.versionType
                activity.assets().smallText = instance.versionComponents[0].second.versionType
            }

            if (appSettings().discordShowTime) {
                activity.timestamps().start = Instant.now()
            }
            try {
                core.activityManager().updateActivity(activity)
                LOGGER.debug { "Started discord activity" }
            } catch (e: Exception) {
                LOGGER.debug(e) { "Failed to update discord activity. Restarting core..." }
                startCore()
            }
        }
    }

    private fun constructDetailsString(instance: InstanceData): String {
        return strings().settings.discord.details(instance.instance.first.name, instance.versionComponents[0].second.versionNumber, instance.versionComponents[0].second.versionType)
    }

    fun clearActivity() {
        core?.let { core ->
            try {
                core.activityManager().clearActivity { result ->
                    println(result.toString())
                }
                LOGGER.debug { "Cleared discord activity" }
            } catch (e: Exception) {
                LOGGER.debug(e) { "Failed to clear discord activity. Restarting core..." }
                startCore()
            }
        }
    }

    fun close() {
        try {
            core?.close()
            LOGGER.debug { "Closed discord core" }
        } catch (e: Exception) {
            LOGGER.debug(e) { "Failed to close discord core" }
        }
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {  }
    }
}