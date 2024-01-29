package net.treset.treelauncher

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mc_version_loader.mods.MinecraftMods
import net.treset.treelauncher.backend.config.*
import net.treset.treelauncher.backend.data.LauncherFiles
import net.treset.treelauncher.backend.update.updater
import net.treset.treelauncher.backend.util.FileInitializer
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.components.Options
import net.treset.treelauncher.components.Resourcepacks
import net.treset.treelauncher.components.Saves
import net.treset.treelauncher.components.mods.Mods
import net.treset.treelauncher.creation.Create
import net.treset.treelauncher.generic.PopupData
import net.treset.treelauncher.generic.PopupOverlay
import net.treset.treelauncher.instances.Instances
import net.treset.treelauncher.localization.language
import net.treset.treelauncher.login.LoginScreen
import net.treset.treelauncher.navigation.NavigationContainer
import net.treset.treelauncher.navigation.NavigationState
import net.treset.treelauncher.settings.Settings
import net.treset.treelauncher.style.colors
import net.treset.treelauncher.style.typography
import net.treset.treelauncher.util.getNewsPopup
import java.io.File
import java.io.IOException
import kotlin.system.exitProcess

data class AppContext(
    val files: LauncherFiles
)

@Composable
fun App(
    launcherApp: LauncherApp
) {
    var popupData: PopupData? by remember { mutableStateOf(null) }

    app = remember {
        launcherApp.apply {
            setPopup = { popupData = it }
        }
    }

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
                                NavigationState.ADD -> Create(appContext, navContext)
                                NavigationState.SAVES -> Saves(appContext, loginContext)
                                NavigationState.RESSOURCE_PACKS -> Resourcepacks(appContext)
                                NavigationState.OPTIONS -> Options(appContext)
                                NavigationState.MODS -> Mods(appContext)
                                NavigationState.SETTINGS -> Settings(loginContext)
                            }
                        }
                    }
                }

                popupData?.let {
                    PopupOverlay(it)
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

    var setPopup: (PopupData?) -> Unit = {  }

    fun showNews(
        displayOther: Boolean = true,
        acknowledgeImportant: Boolean = true,
        displayAcknowledged: Boolean = true
    ) {
        setPopup(
            getNewsPopup(
                close = { setPopup(null) },
                displayOther = displayOther,
                acknowledgeImportant = acknowledgeImportant,
                displayAcknowledged = displayAcknowledged
            )
        )
    }

    fun exit(
        restart: Boolean = false,
        force: Boolean = false
    ) {
        if(force) {
            exitApplication()
        }
        //TODO: handle all the cases

        updater().startUpdater(restart)

        appSettings().save()
        exitApplication()
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

expect fun getUpdaterFile(): File
