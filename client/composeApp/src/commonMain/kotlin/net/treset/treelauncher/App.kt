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
import net.treset.treelauncher.backend.data.InstanceData
import net.treset.treelauncher.backend.data.LauncherFiles
import net.treset.treelauncher.backend.update.updater
import net.treset.treelauncher.backend.util.FileInitializer
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.components.Options
import net.treset.treelauncher.components.Resourcepacks
import net.treset.treelauncher.components.Saves
import net.treset.treelauncher.components.mods.Mods
import net.treset.treelauncher.creation.Create
import net.treset.treelauncher.generic.*
import net.treset.treelauncher.generic.Text
import net.treset.treelauncher.instances.Instances
import net.treset.treelauncher.localization.language
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.login.LoginScreen
import net.treset.treelauncher.navigation.NavigationContainer
import net.treset.treelauncher.navigation.NavigationContext
import net.treset.treelauncher.navigation.NavigationState
import net.treset.treelauncher.settings.Settings
import net.treset.treelauncher.style.*
import net.treset.treelauncher.util.FixFiles
import net.treset.treelauncher.util.News
import java.io.File
import java.io.IOException
import kotlin.system.exitProcess

data class AppContextData(
    val files: LauncherFiles,
    val runningInstance: InstanceData?,
    val setRunningInstance: (InstanceData?) -> Unit,
    val setTheme: (Theme) -> Unit,
    val setAccentColor: (AccentColor) -> Unit,
    val setCustomColor: (Color) -> Unit,
    val globalPopup: PopupData?,
    val addNotification: (NotificationData) -> Unit,
    val dismissNotification: (NotificationData) -> Unit,
    val setGlobalPopup: (PopupData?) -> Unit,
    val openNews: () -> Unit,
    val error: (Exception) -> Unit,
    val severeError: (Exception) -> Unit
)

lateinit var AppContext: AppContextData

val LocalAppContext = staticCompositionLocalOf<AppContextData> {
    error("No NavigationState provided")
}

@Composable
fun App(
    launcherApp: LauncherApp
) {
    var popupData: PopupData? by remember { mutableStateOf(null) }

    var fatalExceptions: List<Exception> by remember { mutableStateOf(listOf()) }

    app = launcherApp

    var theme by remember { mutableStateOf(appSettings().theme) }
    val themeDark = theme.isDark()
    var accentColor by remember { mutableStateOf(appSettings().accentColor) }
    var customColor by remember { mutableStateOf(appSettings().customColor) }
    val colors by remember(themeDark, accentColor, customColor) { mutableStateOf(if(themeDark) darkColors(accentColor) else lightColors(accentColor)) }

    var runningInstance: InstanceData? by remember { mutableStateOf(null) }

    val launcherFiles = remember { LauncherFiles() }

    var notifications: List<NotificationBannerData> by remember { mutableStateOf(listOf()) }
    var notificationsChanged by remember { mutableStateOf(0) }

    var openNews by remember { mutableStateOf(0) }

    AppContext = remember(launcherFiles, runningInstance, popupData) {
        AppContextData(
            files = launcherFiles,
            runningInstance = runningInstance,
            setRunningInstance = {
                runningInstance = it
            },
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
            },
            globalPopup = popupData,
            setGlobalPopup = { popupData = it },
            addNotification = {
                notifications += NotificationBannerData(
                    visible = false,
                    data = it
                )
            },
            dismissNotification = {toRemove ->
                notifications.firstOrNull { it.data == toRemove }?.visible = false
                notificationsChanged++
            },
            openNews = {
                openNews++
            },
            error = {e ->
                LOGGER.warn(e) { "An error occurred!" }
                AppContext.addNotification(
                    NotificationData(
                        color = colors.warning,
                        onClick = {
                            it.dismiss()
                        },
                        content = {
                            Text(
                                strings().error.notification(e),
                                softWrap = true
                            )
                        }
                    )
                )
            },
            severeError = {
                LOGGER.error(it) { "A severe error occurred!" }
                fatalExceptions = fatalExceptions + it
            }
        )
    }

    LauncherTheme(
        colors = colors,
        typography = typography()
    ) {
        CompositionLocalProvider(
            LocalAppContext provides AppContext
        ) {
            ProvideTextStyle(
                MaterialTheme.typography.bodyMedium
            ) {
                Scaffold {
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        notificationsChanged.let {
                            for(notification in notifications) {
                                LaunchedEffect(Unit) {
                                    notification.visible = true
                                    notificationsChanged++
                                }
                                NotificationBanner(
                                    visible = notification.visible,
                                    onDismissed = { notifications -= notification },
                                    data = notification.data
                                )
                            }
                        }

                        LoginScreen {
                            News(openNews)
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

                    popupData?.let {
                        PopupOverlay(it)
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

    fun exit(
        restart: Boolean = false,
        force: Boolean = false
    ) {
        if((AppContext.runningInstance != null || AppContext.globalPopup != null) && !force) {
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

internal data class NotificationBannerData(
    var visible: Boolean,
    val data: NotificationData,
)

private val LOGGER = KotlinLogging.logger {  }

expect fun getUpdaterFile(): File
