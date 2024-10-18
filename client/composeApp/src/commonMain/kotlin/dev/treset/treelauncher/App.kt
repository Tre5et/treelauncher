package dev.treset.treelauncher

import androidx.compose.material3.*
import androidx.compose.runtime.*
import dev.treset.mcdl.mods.ModsDL
import dev.treset.treelauncher.backend.config.*
import dev.treset.treelauncher.backend.update.updater
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.backend.util.serialization.modifySerializer
import dev.treset.treelauncher.components.Options
import dev.treset.treelauncher.components.Resourcepacks
import dev.treset.treelauncher.components.Saves
import dev.treset.treelauncher.components.mods.Mods
import dev.treset.treelauncher.creation.Create
import dev.treset.treelauncher.generic.*
import dev.treset.treelauncher.instances.Instances
import dev.treset.treelauncher.login.LoginScreen
import dev.treset.treelauncher.navigation.NavigationContainer
import dev.treset.treelauncher.navigation.NavigationContext
import dev.treset.treelauncher.navigation.NavigationState
import dev.treset.treelauncher.settings.Settings
import dev.treset.treelauncher.style.*
import dev.treset.treelauncher.util.DataPatcher
import dev.treset.treelauncher.util.FixFiles
import dev.treset.treelauncher.util.News
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.IOException
import kotlin.system.exitProcess

@Composable
fun App(
    launcherApp: LauncherApp
) {
    app = launcherApp

    val themeDark = AppSettings.theme.value.isDark()
    val colors by remember(themeDark, AppSettings.accentColor.value, AppSettings.customColor.value, AppSettings.darkColors.value, AppSettings.lightColors.value) {
        mutableStateOf(
            if(themeDark)
                darkColors(AppSettings.accentColor.value, AppSettings.darkColors.value)
            else
                lightColors(AppSettings.accentColor.value, AppSettings.lightColors.value)
        )
    }

    LauncherTheme(
        colors = colors,
        typography = typography()
    ) {
        Scaffold {
            DataPatcher { recheckData ->
                AppContext.recheckData = recheckData

                ContextProvider {

                    LoginScreen {

                        News()

                        FixFiles()

                        NavigationContainer {
                            when (NavigationContext.navigationState) {
                                NavigationState.INSTANCES -> Instances()
                                NavigationState.ADD -> Create()
                                NavigationState.SAVES -> Saves()
                                NavigationState.RESSOURCE_PACKS -> Resourcepacks()
                                NavigationState.OPTIONS -> Options()
                                NavigationState.MODS -> Mods()
                                NavigationState.SETTINGS -> Settings()
                            }
                        }
                    }
                }
            }
        }
    }
}

private lateinit var app: LauncherApp
fun app() = app

class LauncherApp(
    val exitApplication: () -> Unit
) {
    init {
        configureVersionLoader()

        modifySerializer()

        try {
            loadSettings()
        } catch (e: IOException) {
            LOGGER.error(e) { "Failed to load settings!" }
            exitProcess(-1)
        }
    }

    private fun configureVersionLoader() {
        ModsDL.setModrinthUserAgent(appConfig().modrinthUserAgent)
        ModsDL.setCurseforgeApiKey(appConfig().curseforgeApiKey)
    }

    fun exit(
        restart: Boolean = false,
        force: Boolean = false
    ) {
        if((AppContext.runningInstance != null || AppContext.popupData != null) && !force) {
            // Abort close; game is running or an important popup is open
            LOGGER.info{ "Close request denied: Important Action Running" }
            return
        }

        if(!force) {
            try {
                updater().startUpdater(restart)
            } catch (e: IOException) {
                LOGGER.error(e) { "Failed to start updater!" }
            }
        }

        try {
            AppSettings.save()
        } catch (e: IOException) {
            LOGGER.error(e) { "Failed to save settings!" }
        }

        AppContext.discord.close()

        exitApplication()
    }

    @Throws(IOException::class)
    fun loadSettings() {
        val settingsFile = LauncherFile.of(appConfig().baseDir, appConfig().settingsFile)
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

expect fun getUpdaterProcess(updaterArgs: String): ProcessBuilder

expect fun resetWindow()
