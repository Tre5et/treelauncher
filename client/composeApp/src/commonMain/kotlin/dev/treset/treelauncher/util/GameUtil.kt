package dev.treset.treelauncher.util

import androidx.compose.material3.MaterialTheme
import io.github.oshai.kotlinlogging.KotlinLogging
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.config.AppSettings
import dev.treset.treelauncher.backend.launching.GameLauncher
import dev.treset.treelauncher.backend.util.exception.GameLaunchException
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.generic.*
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.hovered
import dev.treset.treelauncher.style.icons

fun launchGame(
    launcher: GameLauncher,
    onExit: () -> Unit
) {
    GameLaunchHelper(
        launcher,
        onExit
    )
}

private val LOGGER = KotlinLogging.logger { }

class GameLaunchHelper(
    val launcher: GameLauncher,
    val onExit: () -> Unit
) {
    private var notification: NotificationData? = null

    init {
        onPrep()
        launcher.onExit = { onGameExit() }
        launcher.onResourceCleanupFailed = this::onCleanupFail
        launcher.onExited = this::onGameExited
        try {
            launcher.launch(false) { onLaunchDone(it) }
        } catch(e: GameLaunchException) {
            onLaunchFailed(e)
        }
    }

    private fun onPrep() {
        AppContext.setGlobalPopup(
            PopupData(
                titleRow = { Text(Strings.selector.instance.game.preparingTitle()) },
                content =  { Text(Strings.selector.instance.game.preparingMessage()) },
            )
        )
    }

    private fun onLaunchDone(
        e: Exception?
    ) {
        e?.let {
            onLaunchFailed(it)
        } ?: onRunning()
    }

    private fun onRunning() {
        AppContext.runningInstance = launcher.instance
        AppContext.discord.activateActivity(launcher.instance)

        notification = NotificationData(
            content = {
                Text(Strings.selector.instance.game.runningNotification(launcher.instance))
                IconButton(
                    onClick = {
                        AppContext.files.gameDataDir.open()
                    },
                    icon = icons().folder,
                    tooltip = Strings.selector.instance.game.runningOpen(),
                    interactionTint = MaterialTheme.colorScheme.onPrimary.hovered()
                )
                IconButton(
                    onClick = {
                        launcher.stop()
                    },
                    icon = icons().close,
                    tooltip = Strings.selector.instance.game.runningStop(),
                    interactionTint = MaterialTheme.colorScheme.error.hovered()
                )
            },
        ).also { AppContext.addNotification(it) }
        AppContext.setGlobalPopup(null)
        if(AppSettings.minimizeWhileRunning.value) {
            AppContext.minimizeWindow(true)
        }
    }

    private fun onLaunchFailed(
        e: Exception
    ) {
        AppContext.setGlobalPopup(
            PopupData(
                type = PopupType.ERROR,
                titleRow = { Text(Strings.selector.instance.game.errorTitle()) },
                content =  { Text(Strings.selector.instance.game.errorMessage(e.toString())) },
                buttonRow = { Button(
                    onClick = { AppContext.setGlobalPopup(null) },
                    content = { Text(Strings.selector.instance.game.crashClose()) }
                ) }
            )
        )
        LOGGER.error(e) { "Failed to launch game!" }
        onExit()
    }

    private fun onGameExit() {
        if(AppSettings.minimizeWhileRunning.value) {
            AppContext.minimizeWindow(false)
        }
        AppContext.setGlobalPopup(
            PopupData(
                titleRow = { Text(Strings.selector.instance.game.exitingTitle()) },
                content =  { Text(Strings.selector.instance.game.exitingMessage()) },
            )
        )
        AppContext.runningInstance = null
        AppContext.discord.clearActivity()
        notification?.let { AppContext.dismissNotification(it) }
    }

    private fun onGameExited(
        error: String?
    ) {
        error?.let {
            onCrash(it)
            return
        }
        AppContext.setGlobalPopup(null)
        LOGGER.info { "Game exited normally!" }
        onExit()
    }
    
    private fun onCleanupFail(e: Exception, callback: (Boolean) -> Unit) {
        AppContext.silentError(e)
        AppContext.setGlobalPopup(
            PopupData(
                type = PopupType.ERROR,
                titleRow = { Text(Strings.selector.instance.game.cleanupFailTitle()) },
                content =  { Text(Strings.selector.instance.game.cleanupFailMessage()) },
                buttonRow = {
                    Button(
                        onClick = { callback(false) },
                        content = { Text(Strings.selector.instance.game.cleanupFailCancel()) },
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(
                        onClick = { callback(true) },
                        content = { Text(Strings.selector.instance.game.cleanupFailRetry()) },
                    )
                }
            )
        )
    }

    private fun onCrash(
        error: String
    ) {
        AppContext.setGlobalPopup(
            PopupData(
                type = PopupType.WARNING,
                titleRow = { Text(Strings.selector.instance.game.crashTitle()) },
                content =  { Text(Strings.selector.instance.game.crashMessage(error)) },
                buttonRow = {
                    Button(
                        onClick = { AppContext.setGlobalPopup(null) },
                        content = { Text(Strings.selector.instance.game.crashClose()) }
                    )
                    Button(
                        onClick = { LauncherFile.of(launcher.instance.directory, "crash-reports").open() },
                        content = { Text(Strings.selector.instance.game.crashReports()) }
                    )
                }
            )
        )
        LOGGER.warn(GameLaunchException(error)) { "Game crashed!" }
        onExit()
    }
}