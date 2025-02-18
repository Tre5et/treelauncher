package dev.treset.treelauncher

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import dev.treset.treelauncher.backend.data.LauncherFiles
import dev.treset.treelauncher.backend.data.manifest.InstanceComponent
import dev.treset.treelauncher.backend.discord.DiscordIntegration
import dev.treset.treelauncher.generic.*
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.login.LoginContext
import dev.treset.treelauncher.style.ColorScheme
import io.github.oshai.kotlinlogging.KotlinLogging

object AppContext {
    var runningInstance: InstanceComponent? by mutableStateOf(null)
    val files = LauncherFiles()
    var discord = DiscordIntegration()
    var resetWindowSize: () -> Unit = { }
    var minimizeWindow: (Boolean) -> Unit = { }
    var recheckData: () -> Unit = { }
    var notifications = mutableStateListOf<NotificationData>()
        private set
    var popupData: PopupData? by mutableStateOf(null)
        private set
    var newsIndex: Int by mutableStateOf(0)
        private set
    var severeExceptions = mutableStateListOf<Exception>()
        private set

    fun addNotification(notification: NotificationData) {
        notifications += notification
    }

    fun dismissNotification(notification: NotificationData) {
        notifications.find { it == notification }?.dismiss?.let { it() }
    }

    fun setGlobalPopup(popup: PopupData?) {
        popupData = popup
    }

    fun openNews() {
        newsIndex++
    }

    fun error(e: Exception) {
        LOGGER.warn(e) { "An error occurred!" }
        addNotification(
            NotificationData(
                color = ColorScheme.extensions.warning,
                onClick = {
                    it.dismiss()
                },
                content = {
                    Text(
                        Strings.error.notification(e),
                        softWrap = true
                    )
                }
            )
        )
    }

    fun severeError(e: Exception) {
        LOGGER.error(e) { "A severe error occurred!" }
        severeExceptions += e
    }

    fun silentError(e: Exception) {
        LOGGER.warn(e) { "A silent error occurred!" }
    }

    fun errorIfOnline(e: Exception) {
        if(LoginContext.isOffline()) {
            silentError(e)
        } else {
            error(e)
        }
    }
}

@Composable
fun ContextProvider(
    content: @Composable () -> Unit
) {
    LaunchedEffect(Unit) {
        AppContext.resetWindowSize = ::resetWindow
        AppContext.minimizeWindow = ::minimizeWindow
    }

    AppContext.popupData?.let {
        PopupOverlay(it)
    }

    AppContext.severeExceptions.forEach { e ->
        AlertDialog(
            onDismissRequest = {},
            title = { Text(Strings.error.severeTitle()) },
            text = {
                Text(
                    Strings.error.severeMessage(e),
                    textAlign = TextAlign.Start
                )
            },
            containerColor = MaterialTheme.colorScheme.errorContainer,
            confirmButton = {
                Button(
                    onClick = { app().exit(force = true) },
                    color = MaterialTheme.colorScheme.error
                ) {
                    Text(Strings.error.severeClose())
                }
            }
        )
    }

    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppContext.notifications.forEach { notification ->
            NotificationBanner(
                onDismissed = {
                    //Strange behavior when removing, downstream notifications get dismissed too, so keep them in the list
                    //AppContext.notifications -= it
                },
                data = notification
            )
        }

        content()
    }
}

private val LOGGER = KotlinLogging.logger {  }
