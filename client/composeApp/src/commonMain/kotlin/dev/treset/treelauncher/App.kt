package dev.treset.treelauncher

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import dev.treset.mcdl.mods.ModsDL
import dev.treset.treelauncher.backend.config.*
import dev.treset.treelauncher.backend.data.InstanceData
import dev.treset.treelauncher.backend.data.LauncherFiles
import dev.treset.treelauncher.backend.discord.DiscordIntegration
import dev.treset.treelauncher.backend.update.updater
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.backend.util.serialization.modifySerializer
import dev.treset.treelauncher.components.Options
import dev.treset.treelauncher.components.Resourcepacks
import dev.treset.treelauncher.components.Saves
import dev.treset.treelauncher.components.mods.Mods
import dev.treset.treelauncher.creation.Create
import dev.treset.treelauncher.generic.*
import dev.treset.treelauncher.generic.Text
import dev.treset.treelauncher.instances.Instances
import dev.treset.treelauncher.localization.language
import dev.treset.treelauncher.localization.strings
import dev.treset.treelauncher.login.LoginContext
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

data class AppContextData(
    val files: LauncherFiles,
    val runningInstance: InstanceData?,
    val setRunningInstance: (InstanceData?) -> Unit,
    val globalPopup: PopupData?,
    val addNotification: (NotificationData) -> Unit,
    val dismissNotification: (NotificationData) -> Unit,
    val setGlobalPopup: (PopupData?) -> Unit,
    val openNews: () -> Unit,
    val error: (Exception) -> Unit,
    val severeError: (Exception) -> Unit,
    val silentError: (Exception) -> Unit,
    val errorIfOnline: (Exception) -> Unit,
    val resetWindowSize: () -> Unit,
    val discord: DiscordIntegration,
    var recheckData: () -> Unit = {}
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

    val discord = remember { DiscordIntegration() }

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
            globalPopup = popupData,
            setGlobalPopup = { popupData = it },
            addNotification = {
                notifications += NotificationBannerData(
                    visible = false,
                    data = it
                )
            },
            dismissNotification = {toRemove ->
                notifications.firstOrNull { it.data === toRemove && it.visible }?.visible = false
                notificationsChanged++
            },
            openNews = {
                openNews++
            },
            error = {e ->
                LOGGER.warn(e) { "An error occurred!" }
                AppContext.addNotification(
                    NotificationData(
                        color = colors.extensions.warning,
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
            },
            silentError = {
                LOGGER.error(it) { "An error occurred!" }
            },
            errorIfOnline = {
                if(LoginContext.isOffline()) {
                    AppContext.silentError(it)
                } else {
                    AppContext.error(it)
                }
            },
            resetWindowSize = ::resetWindow,
            discord = discord
        )
    }

    LauncherTheme(
        colors = colors,
        typography = typography()
    ) {
        ScalingProvider {
            CompositionLocalProvider(
                LocalAppContext provides AppContext
            ) {
                ProvideTextStyle(
                    MaterialTheme.typography.bodyMedium
                ) {
                    Scaffold {
                        DataPatcher { recheckData ->
                            AppContext.recheckData = recheckData

                            Column(
                                Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                notificationsChanged.let {
                                    for (notification in notifications) {
                                        LaunchedEffect(Unit) {
                                            notification.visible = true
                                            notificationsChanged++
                                        }
                                        NotificationBanner(
                                            visible = notification.visible,
                                            onDismissed = {
                                                //Strange behavior when removing, downstream notifications get dismissed too, so keep them in the list
                                                //notifications -= notification
                                            },
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

        language().appLanguage = AppSettings.language.value
    }

    private fun configureVersionLoader() {
        ModsDL.setModrinthUserAgent(appConfig().modrinthUserAgent)
        ModsDL.setCurseforgeApiKey(appConfig().curseforgeApiKey)
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

internal data class NotificationBannerData(
    var visible: Boolean,
    val data: NotificationData,
)

private val LOGGER = KotlinLogging.logger {  }

expect fun getUpdaterProcess(updaterArgs: String): ProcessBuilder

expect fun resetWindow()
