package net.treset.treelauncher

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mc_version_loader.fabric.FabricLoader
import net.treset.mc_version_loader.forge.MinecraftForge
import net.treset.mc_version_loader.minecraft.MinecraftGame
import net.treset.mc_version_loader.mods.MinecraftMods
import net.treset.mc_version_loader.util.FileUtil
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
import net.treset.treelauncher.navigation.LocalNavigationState
import net.treset.treelauncher.navigation.NavigationContainer
import net.treset.treelauncher.navigation.NavigationState
import net.treset.treelauncher.settings.Settings
import net.treset.treelauncher.style.*
import net.treset.treelauncher.util.FixFilesPopup
import net.treset.treelauncher.util.allContainedIn
import net.treset.treelauncher.util.getNewsPopup
import java.io.File
import java.io.IOException
import kotlin.system.exitProcess

data class AppContextData(
    val files: LauncherFiles,
    val setTheme: (Theme) -> Unit,
    val setAccentColor: (AccentColor) -> Unit,
    val setCustomColor: (Color) -> Unit
)

lateinit var AppContext: AppContextData

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

    var theme by remember { mutableStateOf(appSettings().theme) }
    val themeDark = theme.isDark()
    var accentColor by remember { mutableStateOf(appSettings().accentColor) }
    var customColor by remember { mutableStateOf(appSettings().customColor) }
    val colors: ColorScheme by remember(themeDark, accentColor, customColor) { mutableStateOf(if(themeDark) darkColors(accentColor) else lightColors(accentColor)) }

    val launcherFiles = remember { LauncherFiles() }

    AppContext = remember(launcherFiles) {
        AppContextData(
            files = launcherFiles,
            setTheme = {
                theme = it
                app.setTheme(it)
                appSettings().theme = it
            },
            setAccentColor = {
                accentColor = it
                appSettings().accentColor = it
            },
            setCustomColor = {
                customColor = it
                appSettings().customColor = it
            }
        )
    }

    MaterialTheme(
        colorScheme = colors,
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
                    LoginScreen {
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
                            } catch (e: IOException) {
                                LOGGER.warn(e) { "Unable to load news" }
                            }
                        }

                        FixFilesPopup()

                        NavigationContainer {
                            when(LocalNavigationState.current) {
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
    val setTheme: (Theme) -> Unit
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

        configureVersionLoader()

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

        setTheme(appSettings().theme)

        language().appLanguage = appSettings().language
    }

    private fun configureVersionLoader() {
        MinecraftMods.setModrinthUserAgent(appConfig().modrinthUserAgent)
        MinecraftMods.setCurseforgeApiKey(appConfig().curseforgeApiKey)

        MinecraftGame.useVersionCache(true)
        FabricLoader.useVersionCache(true)
        MinecraftForge.useVersionCache(true)
        FileUtil.useWebRequestCache(true)
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
