package net.treset.treelauncher.util

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.launching.GameLauncher
import net.treset.treelauncher.backend.util.exception.GameLaunchException
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.generic.Button
import net.treset.treelauncher.generic.PopupData
import net.treset.treelauncher.generic.PopupType
import net.treset.treelauncher.generic.Text
import net.treset.treelauncher.localization.strings

fun launchGame(
    launcher: GameLauncher,
    setPopup: (PopupData?) -> Unit,
    onExit: () -> Unit
) {
    GameLaunchHelper(
        launcher,
        setPopup,
        onExit
    )
}

private val LOGGER = KotlinLogging.logger { }

class GameLaunchHelper(
    val launcher: GameLauncher,
    val setPopup: (PopupData?) -> Unit,
    val onExit: () -> Unit
) {
    init {
        onPrep()
        launcher.exitCallbacks = arrayOf({ onGameExit(it) })
        try {
            launcher.launch(false) { onLaunchDone(it) }
        } catch(e: GameLaunchException) {
            onLaunchFailed(e)
        }
    }

    private fun onPrep() {
        setPopup(
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
        setPopup(
            PopupData(
                titleRow = { Text(strings().selector.instance.game.runningTitle()) },
                content =  { Text(strings().selector.instance.game.runningMessage()) },
            )
        )
    }

    private fun onLaunchFailed(
        e: Exception
    ) {
        setPopup(
            PopupData(
                type = PopupType.ERROR,
                titleRow = { Text(strings().selector.instance.game.errorTitle()) },
                content =  { Text(strings().selector.instance.game.errorMessage(e.toString())) },
                buttonRow = { Button(
                    onClick = { setPopup(null) },
                    content = { Text(strings().selector.instance.game.crashClose()) }
                ) }
            )
        )
        LOGGER.error(e) { "Failed to launch game!" }
        onExit()
    }

    private fun onGameExit(
        error: String?
    ) {
        error?.let {
            onCrash(it)
            return
        }
        setPopup(null)
        LOGGER.info { "Game exited normally!" }
        onExit()
    }

    private fun onCrash(
        error: String
    ) {
        setPopup(
            PopupData(
                type = PopupType.WARNING,
                titleRow = { Text(strings().selector.instance.game.crashTitle()) },
                content =  { Text(strings().selector.instance.game.crashMessage(error)) },
                buttonRow = {
                    Button(
                        onClick = { setPopup(null) },
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