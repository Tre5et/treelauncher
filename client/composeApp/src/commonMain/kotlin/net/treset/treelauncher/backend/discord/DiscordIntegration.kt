package net.treset.treelauncher.backend.discord

import de.jcm.discordgamesdk.Core
import de.jcm.discordgamesdk.CreateParams
import de.jcm.discordgamesdk.activity.Activity
import de.jcm.discordgamesdk.activity.ActivityType
import net.treset.treelauncher.backend.config.appSettings
import net.treset.treelauncher.backend.data.InstanceData
import net.treset.treelauncher.localization.strings
import java.time.Instant

class DiscordIntegration {
    val core: Core
    init {
        val params = CreateParams()
        params.clientID = 1241748395115155486
        params.flags = CreateParams.getDefaultFlags()

        this.core = Core(params)

        Thread {
            while(true) {
                core.runCallbacks()
                try {
                    Thread.sleep(16)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }.start()
    }

    fun activateActivity(instance: InstanceData) {
        if(!appSettings().discordIntegration) {
            return
        }
        val activity = Activity()
        activity.type = ActivityType.PLAYING
        activity.details = constructDetailsString(instance)

        activity.assets().largeImage = "pack"
        if(appSettings().discordShowModLoader) {
            activity.assets().smallImage = instance.versionComponents[0].second.versionType
            activity.assets().smallText = instance.versionComponents[0].second.versionType
        }

        if(appSettings().discordShowTime) {
            activity.timestamps().start = Instant.now()
        }

        core.activityManager().updateActivity(activity)
    }

    private fun constructDetailsString(instance: InstanceData): String {
        return strings().settings.discord.details(instance.instance.first.name, instance.versionComponents[0].second.versionNumber, instance.versionComponents[0].second.versionType)
    }

    fun clearActivity() {
        core.activityManager().clearActivity { result ->
            println(result.toString())
        }
    }

    fun close() {
        core.close()
    }
}