package net.treset.treelauncher

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mc_version_loader.mods.MinecraftMods
import net.treset.treelauncher.backend.config.*
import net.treset.treelauncher.backend.data.LauncherFiles
import net.treset.treelauncher.backend.news.news
import net.treset.treelauncher.backend.update.updater
import net.treset.treelauncher.backend.util.FileInitializer
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.components.Options
import net.treset.treelauncher.components.Resourcepacks
import net.treset.treelauncher.components.Saves
import net.treset.treelauncher.components.mods.Mods
import net.treset.treelauncher.creation.Create
import net.treset.treelauncher.generic.Button
import net.treset.treelauncher.generic.PopupData
import net.treset.treelauncher.generic.PopupOverlay
import net.treset.treelauncher.generic.Text
import net.treset.treelauncher.instances.Instances
import net.treset.treelauncher.localization.language
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.login.LoginScreen
import net.treset.treelauncher.navigation.NavigationContainer
import net.treset.treelauncher.navigation.NavigationState
import net.treset.treelauncher.settings.Settings
import net.treset.treelauncher.style.colors
import net.treset.treelauncher.style.typography
import net.treset.treelauncher.util.FixFilesPopup
import net.treset.treelauncher.util.allContainedIn
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

    var exceptions: List<Exception> by remember { mutableStateOf(listOf()) }
    var fatalExceptions: List<Exception> by remember { mutableStateOf(listOf()) }

    app = remember {
        launcherApp.apply {
            setPopup = { popupData = it }
            onError = {
                LOGGER.warn(it) { "An error occurred!" }
                exceptions = exceptions + it
            }
            onSevereError = {
                LOGGER.error(it) { "A severe error occurred!" }
                fatalExceptions = fatalExceptions + it
            }
        }
    }

    val launcherFiles = remember { LauncherFiles() }

    val appContext = remember(launcherFiles) {
        AppContext(
            launcherFiles
        )
    }

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
                        LaunchedEffect(Unit) {
                            try {
                                news().let { nws ->
                                    if (nws.important?.map { it.id }
                                            ?.allContainedIn(appSettings().acknowledgedNews) == false) {
                                        popupData = getNewsPopup(
                                            close = { popupData = null },
                                            displayOther = false,
                                            displayAcknowledged = false
                                        )
                                    }
                                }
                            } catch(e: IOException) {
                                LOGGER.warn(e) { "Unable to load news" }
                            }
                        }

                        FixFilesPopup(appContext)

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

                exceptions.forEach { e ->
                    AlertDialog(
                        onDismissRequest = {},
                        title = { Text(strings().error.title()) },
                        text = {
                            Text(
                                strings().error.message(e),
                                textAlign = TextAlign.Start
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.inversePrimary,
                        textContentColor = MaterialTheme.colorScheme.onPrimary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        confirmButton = {
                            Button(
                                onClick = { exceptions = exceptions.filter { it != e } },
                            ) {
                                Text(strings().error.close())
                            }
                        }
                    )
                }

                fatalExceptions.forEach { e ->
                    AlertDialog(
                        onDismissRequest = {},
                        title = { Text(strings().error.severeTitle()) },
                        text = {
                            Text(
                                strings().error.severeMessage(e),
                                textAlign = TextAlign.Start
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        confirmButton = {
                            Button(
                                onClick = { app().exit(force = true) },
                                color = MaterialTheme.colorScheme.error
                            ) {
                                Text(strings().error.severeClose())
                            }
                        }
                    )
                }
            }
        }
    }
}

private lateinit var app: LauncherApp
fun app() = app

class LauncherApp(
    val exitApplication: () -> Unit,
) {
    init {
        try {
            GlobalConfigLoader().loadConfig()
        } catch (e: IllegalStateException) {
            LOGGER.error(e) { "Failed to load config!" }
            exitProcess(-1)
        } catch (e: IOException) {
            LOGGER.error(e) { "Failed to load config!" }
            exitProcess(-1)
        }

        MinecraftMods.setModrinthUserAgent(appConfig().modrinthUserAgent)
        MinecraftMods.setCurseforgeApiKey(appConfig().curseforgeApiKey)

        try {
            if (!appConfig().baseDir.exists() || !GlobalConfigLoader().hasMainManifest(appConfig().baseDir)) {
                FileInitializer(appConfig().baseDir).create()
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

    fun error(e: Exception) = onError(e)

    fun severeError(e: Exception) = onSevereError(e)

    var setPopup: (PopupData?) -> Unit = {}
    var onError: (Exception) -> Unit = {}
    var onSevereError: (Exception) -> Unit = {}

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
                displayAcknowledged = displayAcknowledged,
            )
        )
    }

    fun exit(
        restart: Boolean = false,
        force: Boolean = false
    ) {
        if(!force) {
            try {
                updater().startUpdater(restart)
            } catch (e: IOException) {
                LOGGER.error(e) { "Failed to start updater!" }
            }
        }

        try {
            appSettings().save()
        } catch (e: IOException) {
            LOGGER.error(e) { "Failed to save settings!" }
        }

        exitApplication()
    }

    @Throws(IOException::class)
    private fun loadSettings() {
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

private val LOGGER = KotlinLogging.logger {  }

expect fun getUpdaterFile(): File
