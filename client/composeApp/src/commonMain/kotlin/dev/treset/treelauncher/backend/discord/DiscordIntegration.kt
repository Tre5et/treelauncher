package dev.treset.treelauncher.backend.discord

import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import de.jcm.discordgamesdk.Core
import de.jcm.discordgamesdk.CreateParams
import de.jcm.discordgamesdk.activity.Activity
import de.jcm.discordgamesdk.activity.ActivityType
import dev.treset.treelauncher.backend.config.AppSettings
import dev.treset.treelauncher.backend.data.manifest.InstanceComponent
import io.github.oshai.kotlinlogging.KotlinLogging
import dev.treset.treelauncher.localization.Strings
import java.time.Instant

class DiscordIntegration {
    var core: Core? = null
    init {
        Thread {
            startCore()
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

    fun activateActivity(instance: InstanceComponent) {
        if(!AppSettings.discordIntegration.value) {
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
            if (AppSettings.discordShowModLoader.value) {
                activity.assets().smallImage = instance.versionComponents.value[0].versionType.value
                activity.assets().smallText = instance.versionComponents.value[0].versionType.value
            }

            if (AppSettings.discordShowTime.value) {
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

    private fun constructDetailsString(instance: InstanceComponent): String {
        return Strings.settings.discord.details(
            instance.name.value,
            instance.versionComponents.value[0].versionNumber.value,
            instance.versionComponents.value[0].versionType.value.capitalize(Locale.current)
        )
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
            core = null
        } catch (e: Exception) {
            LOGGER.debug(e) { "Failed to close discord core" }
        }
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {  }
    }
}