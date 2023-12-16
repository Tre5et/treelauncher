package net.treset.treelauncher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mc_version_loader.mods.MinecraftMods
import net.treset.treelauncher.backend.config.*
import net.treset.treelauncher.backend.util.FileInitializer
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.localization.language
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import java.io.IOException
import kotlin.system.exitProcess

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {
    MaterialTheme {
        var greetingText by remember { mutableStateOf("Hello World!") }
        var showImage by remember { mutableStateOf(false) }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = {
                greetingText = "Compose: ${Greeting().greet()}"
                showImage = !showImage
            }) {
                Text(greetingText)
            }
            AnimatedVisibility(showImage) {
                Image(
                    painterResource("compose-multiplatform.xml"),
                    null
                )
            }
        }
    }
}

fun onClose(): Boolean {
    //TODO: Prevent close
    //TODO: Sync
    //TODO: Update
    return true
}

private var app = LauncherApp()
class LauncherApp {
    init {
        //TODO: Configure Logger
        //TODO: Close Behaviour: Prevent close, Sync, Update
        //TODO: Popups

        try {
            GlobalConfigLoader().loadConfig()
        } catch (e: IllegalStateException) {
            LOGGER.error(e) { "Failed to load config!" }
            exitProcess(-1)
        } catch (e: IOException) {
            LOGGER.error(e) { "Failed to load config!" }
            exitProcess(-1)
        }

        MinecraftMods.setModrinthUserAgent(appConfig().MODRINTH_USER_AGENT)
        MinecraftMods.setCurseforgeApiKey(appConfig().CURSEFORGE_API_KEY)

        try {
            if (!appConfig().BASE_DIR.exists() || !GlobalConfigLoader().hasMainMainfest(appConfig().BASE_DIR)) {
                FileInitializer(appConfig().BASE_DIR).create()
            }
        } catch (e: IOException) {
            LOGGER.error(e) { "Failed to initialize directory structure!" }
        }

        try {
            loadSettings()
        } catch (e: IOException) {
            LOGGER.error(e) { "Failed to load settings!" }
            exitProcess(-1)
        }

        language().appLanguage = appSettings().language
    }

    @Throws(IOException::class)
    private fun loadSettings() {
        val settingsFile = LauncherFile.of(appConfig().BASE_DIR, appConfig().SETTINGS_FILE_NAME)
        if (!settingsFile.exists()) {
            Settings.new(settingsFile)
        } else {
            Settings.load(settingsFile)
        }
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {  }
    }
}
