package net.treset.treelauncher

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mc_version_loader.mods.MinecraftMods
import net.treset.treelauncher.backend.config.*
import net.treset.treelauncher.backend.data.LauncherFiles
import net.treset.treelauncher.backend.util.FileInitializer
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.components.Options
import net.treset.treelauncher.components.Resourcepacks
import net.treset.treelauncher.components.Saves
import net.treset.treelauncher.instances.Instances
import net.treset.treelauncher.localization.language
import net.treset.treelauncher.login.LoginScreen
import net.treset.treelauncher.navigation.NavigationContainer
import net.treset.treelauncher.navigation.NavigationState
import net.treset.treelauncher.settings.Settings
import net.treset.treelauncher.style.colors
import net.treset.treelauncher.style.typography
import java.io.IOException
import kotlin.system.exitProcess

data class AppContext(
    val files: LauncherFiles
)

@Composable
fun App() {
    app = LauncherApp()

    val launcherFiles = remember { LauncherFiles() }
    launcherFiles.reloadAll()

    val appContext = AppContext(launcherFiles)

    MaterialTheme(
        colorScheme = colors(),
        typography = typography()
    ) {
        ProvideTextStyle(
            MaterialTheme.typography.bodyMedium
        ) {

            Scaffold {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LoginScreen { loginContext ->
                        NavigationContainer(loginContext) { navContext ->
                            when (navContext.navigationState) {
                                NavigationState.INSTANCES -> Instances(appContext, loginContext)
                                NavigationState.SAVES -> Saves(appContext, loginContext)
                                NavigationState.RESSOURCE_PACKS -> Resourcepacks(appContext)
                                NavigationState.OPTIONS -> Options(appContext)
                                NavigationState.SETTINGS -> Settings(loginContext)

                                else -> Text("TODO")
                            }
                        }
                    }
                }
            }
        }
    }
}

fun onClose(): Boolean {
    //TODO: Prevent close
    //TODO: Sync
    //TODO: Update
    appSettings().save()

    return true
}

private lateinit var app: LauncherApp
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
