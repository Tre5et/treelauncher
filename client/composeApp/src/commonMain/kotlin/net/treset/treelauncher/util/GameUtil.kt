package net.treset.treelauncher.util

import androidx.compose.material3.MaterialTheme
import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.launching.GameLauncher
import net.treset.treelauncher.backend.util.exception.GameLaunchException
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.generic.*
import net.treset.treelauncher.localization.strings

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
                titleRow = { Text(strings().selector.instance.game.preparingTitle()) },
                content =  { Text(strings().selector.instance.game.preparingMessage()) },
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
        AppContext.setRunningInstance(launcher.instance)
        notification = NotificationData(
            content = {
                Text(strings().selector.instance.game.runningNotification(launcher.instance))
            },
        ).also { AppContext.addNotification(it) }
        AppContext.setGlobalPopup(null)
    }

    private fun onLaunchFailed(
        e: Exception
    ) {
        AppContext.setGlobalPopup(
            PopupData(
                type = PopupType.ERROR,
                titleRow = { Text(strings().selector.instance.game.errorTitle()) },
                content =  { Text(strings().selector.instance.game.errorMessage(e.toString())) },
                buttonRow = { Button(
                    onClick = { AppContext.setGlobalPopup(null) },
                    content = { Text(strings().selector.instance.game.crashClose()) }
                ) }
            )
        )
        LOGGER.error(e) { "Failed to launch game!" }
        onExit()
    }

    private fun onGameExit() {
        AppContext.setGlobalPopup(
            PopupData(
                titleRow = { Text(strings().selector.instance.game.exitingTitle()) },
                content =  { Text(strings().selector.instance.game.exitingMessage()) },
            )
        )
        AppContext.setRunningInstance(null)
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
                titleRow = { Text(strings().selector.instance.game.cleanupFailTitle()) },
                content =  { Text(strings().selector.instance.game.cleanupFailMessage()) },
                buttonRow = {
                    Button(
                        onClick = { callback(false) },
                        content = { Text(strings().selector.instance.game.cleanupFailCancel()) },
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(
                        onClick = { callback(true) },
                        content = { Text(strings().selector.instance.game.cleanupFailRetry()) },
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
                titleRow = { Text(strings().selector.instance.game.crashTitle()) },
                content =  { Text(strings().selector.instance.game.crashMessage(error)) },
                buttonRow = {
                    Button(
                        onClick = { AppContext.setGlobalPopup(null) },
                        content = { Text(strings().selector.instance.game.crashClose()) }
                    )
                    Button(
                        onClick = { LauncherFile.of(launcher.instance.instance.first.directory, appConfig().includedFilesDirName, "crash-reports").open() },
                        content = { Text(strings().selector.instance.game.crashReports()) }
                    )
                }
            )
        )
        LOGGER.warn(GameLaunchException(error)) { "Game crashed!" }
        onExit()
    }
}